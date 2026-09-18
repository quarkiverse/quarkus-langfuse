package io.quarkiverse.langfuse.api;

import java.time.OffsetDateTime;

/**
 * The entry point to the Langfuse experiment items collection, which exists to make the mandatory
 * lower time bound unavoidable.
 *
 * <p>
 * Obtained from {@link LangfuseOperations#experimentItems()}. It deliberately offers <em>nothing but
 * </em> the two ways of choosing a time window: it is not an {@link ExperimentItemOperations} and
 * carries none of its operations, so there is no way to reach a listing without having supplied a
 * bound.
 *
 * <p>
 * <strong>Why the bound is part of reaching the collection at all.</strong> Langfuse documents
 * {@code fromStartTime} as required on {@code /api/public/experiment-items} - a lower bound keeps
 * the query fast on large projects - and answers HTTP 400 when it is missing. Modelling it as an
 * ordinary optional criterion would leave every zero-argument operation on the collection guaranteed
 * to fail, so the bound is taken here instead, exactly where the collection is reached.
 *
 * <pre>{@code
 * langfuse.experimentItems().since(OffsetDateTime.now().minusDays(7)).findAll();
 * langfuse.experimentItems().between(startOfMonth, endOfMonth).findAll();
 * }</pre>
 *
 * @see AsyncExperimentItemTimeWindow
 */
public sealed interface ExperimentItemTimeWindow permits DefaultExperimentItemTimeWindow {

    /**
     * The experiment items started at or after the given instant, with no upper bound.
     *
     * @param fromStartTime the inclusive lower bound on the item start time, must not be {@code null}
     * @return the operations over the experiment items within that window
     * @throws IllegalArgumentException if {@code fromStartTime} is {@code null}
     */
    ExperimentItemOperations since(OffsetDateTime fromStartTime);

    /**
     * The experiment items started within the given window.
     *
     * @param fromStartTime the inclusive lower bound on the item start time, must not be {@code null}
     * @param toStartTime the exclusive upper bound on the item start time, must not be {@code null}
     *        and must not precede {@code fromStartTime}
     * @return the operations over the experiment items within that window
     * @throws IllegalArgumentException if either bound is {@code null}, or if {@code toStartTime}
     *         precedes {@code fromStartTime}
     */
    ExperimentItemOperations between(OffsetDateTime fromStartTime, OffsetDateTime toStartTime);
}
