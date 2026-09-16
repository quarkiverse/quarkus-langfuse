package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.smallrye.mutiny.Uni;

/**
 * Executes delete work, without blocking.
 *
 * <p>
 * The asynchronous counterpart of {@link Deletions}, structured identically so the two cannot drift
 * apart. Nothing here blocks, and nothing in {@link Deletions} is reused by delegation - the two are
 * independent implementations over the synchronous and asynchronous halves of the generated client,
 * exactly as {@link AsyncPagination} is to {@link Pagination}.
 *
 * @see Deletions the blocking counterpart of this class
 */
final class AsyncDeletions {

    private AsyncDeletions() {
    }

    /**
     * Deletes every distinct identifier, accumulating one {@link DeletionOutcome} per identifier.
     *
     * <p>
     * <strong>Never fails fast, and the returned {@link Uni} never fails.</strong> Every identifier is
     * attempted regardless of what happened to the ones before it.
     *
     * @param identifiers the caller-supplied identifiers, validated and deduplicated here before any
     *        request is issued
     * @param label what to call an identifier in a validation error message
     * @param resolve maps an identifier to the id to delete, emitting {@code null} if nothing matched
     * @param delete issues the delete for a resolved id
     * @param concurrency the maximum number of deletes to run at once
     * @return one outcome per distinct identifier
     * @throws IllegalArgumentException if {@code identifiers} is {@code null}, or if any element is
     *         {@code null} or blank
     */
    static Uni<DeletionResult> deleteAll(Collection<String> identifiers, String label,
            Function<String, Uni<String>> resolve, Function<String, Uni<?>> delete, int concurrency) {
        var distinct = DeletionIdentifiers.validated(identifiers, label);
        var units = distinct.stream()
                .map(identifier -> deleteOne(identifier, resolve, delete))
                .toList();

        // usingConcurrencyOf(n) subscribes to at most n units at a time and starts the next one as each
        // completes, so the in-flight count stays at n without any queue or thread of our own. The
        // combined list is positional rather than completion-ordered: each unit's outcome lands at the
        // index of the identifier it came from, which is what keeps this tree's result order identical
        // to the blocking one however the server interleaves its responses.
        //
        // An empty list is short-circuited because combine() subscribes nothing when there is nothing to
        // subscribe to, leaving a Uni that never emits.
        return units.isEmpty()
                ? Uni.createFrom().item(DeletionResult.of(List.of()))
                : Uni.combine()
                        .all()
                        .unis(units)
                        .usingConcurrencyOf(Math.max(1, concurrency))
                        .with(DeletionOutcome.class, DeletionResult::of);
    }

    /**
     * Resolves {@code identifier} to the id the delete endpoint wants, deletes it, and reports the
     * terminal state as a {@link DeletionOutcome}.
     *
     * <p>
     * <strong>The returned {@link Uni} never fails.</strong> Every terminal state - deleted, absent,
     * failed - is emitted as an item.
     *
     * @param identifier the caller-supplied identifier, already validated as non-blank
     * @param resolve maps the identifier to the id to delete, emitting {@code null} if nothing matched
     * @param delete issues the delete for a resolved id
     * @return the outcome for this identifier, always as a successful {@link Uni}
     */
    static Uni<DeletionOutcome> deleteOne(String identifier, Function<String, Uni<String>> resolve,
            Function<String, Uni<?>> delete) {
        // Two stages, resolve then delete, collapsed into one Uni that always emits an outcome. The
        // recovery pair is ordered narrowest first: a LangfuseNotFoundException from either stage is
        // converted to an item, so the catch-all below never sees it and only genuine failures reach
        // Failed. deferred() delays resolve.apply until subscription, which keeps a synchronous throw
        // there inside the pipeline where the recoveries can catch it.
        return Uni.createFrom().deferred(() -> resolve.apply(identifier))
                .flatMap(resolvedId -> (resolvedId == null)
                        ? Uni.createFrom().<DeletionOutcome> item(DeletionOutcome.notFound(identifier))
                        : delete.apply(resolvedId).replaceWith(() -> DeletionOutcome.deleted(identifier)))
                .onFailure(LangfuseNotFoundException.class)
                .recoverWithItem(() -> DeletionOutcome.notFound(identifier))
                .onFailure().recoverWithItem(t -> DeletionOutcome.failed(identifier, t));
    }
}
