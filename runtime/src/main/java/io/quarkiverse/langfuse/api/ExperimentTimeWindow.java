package io.quarkiverse.langfuse.api;

import java.time.OffsetDateTime;

/**
 * The entry point to the Langfuse experiments collection, which exists to make the mandatory lower
 * time bound unavoidable.
 *
 * <p>
 * Obtained from {@link LangfuseOperations#experiments()}. It deliberately offers <em>nothing but</em>
 * the two ways of choosing a time window: it is not an {@link ExperimentOperations} and carries none
 * of its operations, so there is no way to reach a listing without having supplied a bound.
 *
 * <p>
 * <strong>Why the bound is part of reaching the collection at all.</strong> Langfuse documents
 * {@code fromStartTime} as required on {@code /api/public/experiments} - a lower bound keeps the
 * query fast on large projects - and answers HTTP 400 when it is missing. Modelling it as an
 * ordinary optional criterion would leave every zero-argument operation on the collection guaranteed
 * to fail, so the bound is taken here instead, exactly where the collection is reached.
 *
 * <pre>{@code
 * langfuse.experiments().since(OffsetDateTime.now().minusDays(7)).findAll();
 * langfuse.experiments().between(startOfMonth, endOfMonth).findAll();
 * }</pre>
 *
 * @see AsyncExperimentTimeWindow
 */
public sealed interface ExperimentTimeWindow permits DefaultExperimentTimeWindow {

    /**
     * The experiments active at or after the given instant, with no upper bound.
     *
     * @param fromStartTime the inclusive lower bound on experiment activity, must not be {@code null}
     * @return the operations over the experiments within that window
     * @throws IllegalArgumentException if {@code fromStartTime} is {@code null}
     */
    ExperimentOperations since(OffsetDateTime fromStartTime);

    /**
     * The experiments active within the given window.
     *
     * @param fromStartTime the inclusive lower bound on experiment activity, must not be {@code null}
     * @param toStartTime the exclusive upper bound on experiment activity, must not be {@code null}
     *        and must not precede {@code fromStartTime}
     * @return the operations over the experiments within that window
     * @throws IllegalArgumentException if either bound is {@code null}, or if {@code toStartTime}
     *         precedes {@code fromStartTime}
     */
    ExperimentOperations between(OffsetDateTime fromStartTime, OffsetDateTime toStartTime);
}
