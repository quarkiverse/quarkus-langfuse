package io.quarkiverse.langfuse.api;

import java.time.OffsetDateTime;

/**
 * The entry point to the Langfuse experiments collection on the asynchronous tree, which exists to
 * make the mandatory lower time bound unavoidable.
 *
 * <p>
 * Obtained from {@link AsyncLangfuseOperations#experiments()}. The asynchronous counterpart of
 * {@link ExperimentTimeWindow}; it is not an {@link AsyncExperimentOperations} and carries none of
 * its operations, so there is no way to reach a listing without having supplied a bound.
 *
 * <p>
 * <strong>Why the bound is part of reaching the collection at all.</strong> Langfuse documents
 * {@code fromStartTime} as required on {@code /api/public/experiments} and answers HTTP 400 when it
 * is missing. See {@link ExperimentTimeWindow} for the full reasoning.
 *
 * @see ExperimentTimeWindow
 */
public sealed interface AsyncExperimentTimeWindow permits DefaultAsyncExperimentTimeWindow {

    /**
     * The experiments active at or after the given instant, with no upper bound.
     *
     * @param fromStartTime the inclusive lower bound on experiment activity, must not be {@code null}
     * @return the operations over the experiments within that window. Never {@code null}
     * @throws IllegalArgumentException if {@code fromStartTime} is {@code null}. Thrown from this call
     *         rather than emitted as a failure
     */
    AsyncExperimentOperations since(OffsetDateTime fromStartTime);

    /**
     * The experiments active within the given window.
     *
     * @param fromStartTime the inclusive lower bound on experiment activity, must not be {@code null}
     * @param toStartTime the exclusive upper bound on experiment activity, must not be {@code null}
     *        and must not precede {@code fromStartTime}
     * @return the operations over the experiments within that window. Never {@code null}
     * @throws IllegalArgumentException if either bound is {@code null}, or if {@code toStartTime}
     *         precedes {@code fromStartTime}. Thrown from this call rather than emitted as a failure
     */
    AsyncExperimentOperations between(OffsetDateTime fromStartTime, OffsetDateTime toStartTime);
}
