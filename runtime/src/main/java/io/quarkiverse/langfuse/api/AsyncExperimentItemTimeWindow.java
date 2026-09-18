package io.quarkiverse.langfuse.api;

import java.time.OffsetDateTime;

/**
 * The entry point to the Langfuse experiment items collection on the asynchronous tree, which exists
 * to make the mandatory lower time bound unavoidable.
 *
 * <p>
 * Obtained from {@link AsyncLangfuseOperations#experimentItems()}. The asynchronous counterpart of
 * {@link ExperimentItemTimeWindow}; it is not an {@link AsyncExperimentItemOperations} and carries
 * none of its operations, so there is no way to reach a listing without having supplied a bound.
 *
 * <p>
 * <strong>Why the bound is part of reaching the collection at all.</strong> Langfuse documents
 * {@code fromStartTime} as required on {@code /api/public/experiment-items} and answers HTTP 400
 * when it is missing. See {@link ExperimentItemTimeWindow} for the full reasoning.
 *
 * @see ExperimentItemTimeWindow
 */
public sealed interface AsyncExperimentItemTimeWindow permits DefaultAsyncExperimentItemTimeWindow {

    /**
     * The experiment items started at or after the given instant, with no upper bound.
     *
     * @param fromStartTime the inclusive lower bound on the item start time, must not be {@code null}
     * @return the operations over the experiment items within that window. Never {@code null}
     * @throws IllegalArgumentException if {@code fromStartTime} is {@code null}. Thrown from this call
     *         rather than emitted as a failure
     */
    AsyncExperimentItemOperations since(OffsetDateTime fromStartTime);

    /**
     * The experiment items started within the given window.
     *
     * @param fromStartTime the inclusive lower bound on the item start time, must not be {@code null}
     * @param toStartTime the exclusive upper bound on the item start time, must not be {@code null}
     *        and must not precede {@code fromStartTime}
     * @return the operations over the experiment items within that window. Never {@code null}
     * @throws IllegalArgumentException if either bound is {@code null}, or if {@code toStartTime}
     *         precedes {@code fromStartTime}. Thrown from this call rather than emitted as a failure
     */
    AsyncExperimentItemOperations between(OffsetDateTime fromStartTime, OffsetDateTime toStartTime);
}
