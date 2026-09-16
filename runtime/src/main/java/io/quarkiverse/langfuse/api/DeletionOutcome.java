package io.quarkiverse.langfuse.api;

/**
 * The terminal state of deleting one identifier.
 *
 * <p>
 * Every delete operation in this layer reports per identifier rather than throwing, because no
 * Langfuse endpoint deletes more than one resource at a time: a batch delete is client-side
 * iteration, so it can partially apply. One of these is produced for each distinct identifier
 * passed to a delete operation, and they are collected into a {@link DeletionResult}.
 *
 * <p>
 * The hierarchy is sealed and its three branches are exhaustive, so an application running on Java
 * 21 or later can switch over it without a {@code default} branch, and adding a branch here would
 * become a compile error at every such call site:
 *
 * <pre>{@code
 * import io.quarkiverse.langfuse.api.DeletionOutcome.Deleted;
 * import io.quarkiverse.langfuse.api.DeletionOutcome.Failed;
 * import io.quarkiverse.langfuse.api.DeletionOutcome.NotFound;
 *
 * var message = switch (outcome) {
 *     case Deleted d -> "deleted %s".formatted(d.identifier());
 *     case NotFound n -> "absent %s".formatted(n.identifier());
 *     case Failed f -> "failed %s: %s".formatted(f.identifier(), f.cause().getMessage());
 * };
 * }</pre>
 *
 * On Java 17 an {@code instanceof} chain achieves the same, without the exhaustiveness check.
 *
 * <p>
 * The branches are nested types, so they may equally be written {@code DeletionOutcome.Deleted} at
 * the use site rather than imported.
 *
 * <p>
 * Only {@link Failed} carries a cause. That is the reason this is a sealed hierarchy rather than an
 * enum paired with a nullable {@code cause} field: a cause simply cannot be read off a success.
 *
 * @see DeletionResult
 */
public sealed interface DeletionOutcome permits DeletionOutcome.Deleted, DeletionOutcome.NotFound, DeletionOutcome.Failed {

    /**
     * Creates an outcome recording that the resource was deleted.
     *
     * @param identifier the identifier the delete was requested with, must not be {@code null} or
     *        blank
     * @return the outcome
     * @throws IllegalArgumentException if {@code identifier} is {@code null} or blank
     */
    static Deleted deleted(String identifier) {
        return new DefaultDeleted(identifier);
    }

    /**
     * Creates an outcome recording that no resource matched the identifier.
     *
     * @param identifier the identifier the delete was requested with, must not be {@code null} or
     *        blank
     * @return the outcome
     * @throws IllegalArgumentException if {@code identifier} is {@code null} or blank
     */
    static NotFound notFound(String identifier) {
        return new DefaultNotFound(identifier);
    }

    /**
     * Creates an outcome recording that the delete could not be completed.
     *
     * @param identifier the identifier the delete was requested with, must not be {@code null} or
     *        blank
     * @param cause what went wrong, must not be {@code null}
     * @return the outcome
     * @throws IllegalArgumentException if {@code identifier} is {@code null} or blank, or if
     *         {@code cause} is {@code null}
     */
    static Failed failed(String identifier, Throwable cause) {
        return new DefaultFailed(identifier, cause);
    }

    /**
     * The identifier this outcome describes.
     *
     * <p>
     * Whatever the delete operation was keyed on: an id for {@code deleteById}, a name for
     * {@code deleteByName}, a provider for {@code deleteByProvider}. It is the same value the caller
     * supplied, so it can be matched back to the input.
     *
     * @return the identifier, never {@code null} or blank
     */
    String identifier();

    /**
     * The resource existed and was deleted.
     */
    sealed interface Deleted extends DeletionOutcome permits DefaultDeleted {
    }

    /**
     * No resource matched the identifier, so there was nothing to delete.
     *
     * <p>
     * <strong>This is an expected outcome, not a failure.</strong> Deleting something that is already
     * absent is the normal result of an idempotent teardown, so it is reported as data rather than as
     * an error.
     *
     * <p>
     * Do not confuse this with
     * {@link io.quarkiverse.langfuse.client.LangfuseNotFoundException}, which is the wire-level
     * exception this outcome is mapped <em>from</em>. That exception never reaches the caller of a
     * delete operation; it is caught and turned into one of these. It is also the <em>only</em>
     * exception treated as absence - every other failure becomes a {@link Failed}, so a rejected
     * credential or a timeout can never be misread as a missing resource.
     */
    sealed interface NotFound extends DeletionOutcome permits DefaultNotFound {
    }

    /**
     * The delete could not be completed.
     *
     * <p>
     * One failed identifier never prevents the others from being attempted: delete operations
     * accumulate every outcome rather than stopping at the first problem.
     */
    sealed interface Failed extends DeletionOutcome permits DefaultFailed {

        /**
         * What went wrong.
         *
         * @return the cause, never {@code null}
         */
        Throwable cause();
    }
}
