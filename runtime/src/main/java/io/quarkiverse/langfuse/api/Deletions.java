package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.smallrye.mutiny.infrastructure.Infrastructure;

/**
 * Executes delete work, blocking.
 *
 * <p>
 * The synchronous half of the delete engine, mirroring what {@link Pagination} is to traversal:
 * package-private, stateless, and holding no configuration of its own. {@link AsyncDeletions} is the
 * non-blocking counterpart; nothing here is reused by it, so the two trees stay independent.
 */
final class Deletions {

    private Deletions() {
    }

    /**
     * Deletes every distinct identifier, accumulating one {@link DeletionOutcome} per identifier.
     *
     * <p>
     * <strong>Never fails fast.</strong> Every identifier is attempted regardless of what happened to
     * the ones before it, so a single failure neither hides the successes nor prevents the remaining
     * work.
     *
     * @param identifiers the caller-supplied identifiers, validated and deduplicated here before any
     *        request is issued
     * @param label what to call an identifier in a validation error message
     * @param resolve maps an identifier to the id to delete, or empty if nothing matched
     * @param delete issues the delete for a resolved id
     * @param concurrency the maximum number of deletes to run at once
     * @return one outcome per distinct identifier
     * @throws IllegalArgumentException if {@code identifiers} is {@code null}, or if any element is
     *         {@code null} or blank
     */
    static DeletionResult deleteAll(Collection<String> identifiers, String label,
            Function<String, Optional<String>> resolve, Consumer<String> delete, int concurrency) {
        var distinct = DeletionIdentifiers.validated(identifiers, label);
        var runners = Math.min(Math.max(1, concurrency), distinct.size());

        return DeletionResult.of((runners <= 1)
                ? runSerially(distinct, resolve, delete)
                : runConcurrently(distinct, runners, resolve, delete));
    }

    private static List<DeletionOutcome> runSerially(List<String> identifiers,
            Function<String, Optional<String>> resolve, Consumer<String> delete) {
        return identifiers.stream()
                .map(identifier -> deleteOne(identifier, resolve, delete))
                .toList();
    }

    private static List<DeletionOutcome> runConcurrently(List<String> identifiers, int runners,
            Function<String, Optional<String>> resolve, Consumer<String> delete) {
        // Work-stealing drain. One shared cursor hands out identifier indexes, and each runner
        // repeatedly claims the next one and deletes it until the indexes run out. Runners therefore
        // self-balance: one that draws a batch of fast deletes moves on to the slow runner's remaining
        // work instead of idling, which a fixed split of the identifiers across runners cannot do.
        //
        // Each outcome is written to the array slot matching its own index, so the results come back in
        // input order regardless of the order the runners finished in, and no runner ever appends to a
        // shared list.
        var cursor = new AtomicInteger();
        var outcomes = new AtomicReferenceArray<DeletionOutcome>(identifiers.size());
        Runnable drain = () -> drainInto(cursor, identifiers, outcomes, resolve, delete);

        // Submits one fewer runner than requested and drains on the calling thread too, so the batch
        // still progresses when the shared worker pool is saturated.
        var executor = Infrastructure.getDefaultExecutor();
        var submitted = IntStream.range(0, runners - 1)
                .mapToObj(runner -> CompletableFuture.runAsync(drain, executor))
                .toArray(CompletableFuture[]::new);

        drain.run();

        join(submitted);

        return IntStream.range(0, outcomes.length())
                .mapToObj(outcomes::get)
                .toList();
    }

    // Claims indexes one at a time rather than in blocks: getAndIncrement hands each index to exactly
    // one runner, and a runner stops once the index it drew is past the end of the list.
    private static void drainInto(AtomicInteger cursor, List<String> identifiers,
            AtomicReferenceArray<DeletionOutcome> outcomes, Function<String, Optional<String>> resolve,
            Consumer<String> delete) {
        for (var index = cursor.getAndIncrement(); index < identifiers.size(); index = cursor.getAndIncrement()) {
            outcomes.set(index, deleteOne(identifiers.get(index), resolve, delete));
        }
    }

    // allOf completes only once every submitted runner has drained, and the caller has already drained
    // its own share by this point, so the outcome array is fully populated when this returns. deleteOne
    // never throws, so an ExecutionException here is a runner fault rather than a failed delete.
    private static void join(CompletableFuture<?>... submitted) {
        try {
            CompletableFuture.allOf(submitted).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException("Interrupted while awaiting delete completion", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("A delete unit failed to produce an outcome", e.getCause());
        }
    }

    /**
     * Resolves {@code identifier} to the id the delete endpoint wants, deletes it, and reports the
     * terminal state as a {@link DeletionOutcome}.
     *
     * <p>
     * <strong>Never throws.</strong> Every terminal state - deleted, absent, failed - comes back as a
     * value.
     *
     * @param identifier the caller-supplied identifier, already validated as non-blank
     * @param resolve maps the identifier to the id to delete, or empty if nothing matched
     * @param delete issues the delete for a resolved id
     * @return the outcome for this identifier
     */
    static DeletionOutcome deleteOne(String identifier, Function<String, Optional<String>> resolve,
            Consumer<String> delete) {

        try {
            return resolve.apply(identifier)
                    .map(resolvedId -> deleteResolved(identifier, resolvedId, delete))
                    .orElseGet(() -> DeletionOutcome.notFound(identifier));
        } catch (LangfuseNotFoundException e) {
            return DeletionOutcome.notFound(identifier);
        } catch (Throwable t) {
            // Throwable, not Exception, so an Error also becomes a Failed outcome.
            return DeletionOutcome.failed(identifier, t);
        }
    }

    private static DeletionOutcome deleteResolved(String identifier, String resolvedId, Consumer<String> delete) {
        delete.accept(resolvedId);

        return DeletionOutcome.deleted(identifier);
    }
}
