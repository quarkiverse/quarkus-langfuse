package io.quarkiverse.langfuse.api;

import com.langfuse.api.model.Experiment;

/**
 * Higher-level operations over Langfuse experiments, returning Mutiny types.
 *
 * <p>
 * Obtained from {@link AsyncExperimentTimeWindow}, which is what
 * {@link AsyncLangfuseOperations#experiments()} returns. The asynchronous counterpart of
 * {@link ExperimentOperations}; the two behave identically, and since this domain is read-only and
 * has no lookup returning "maybe one thing", the absence convention that separates the two trees
 * elsewhere never comes into play here.
 *
 * <p>
 * <strong>Results are ordered by latest experiment activity, descending</strong>, so the first batch
 * of a walk holds the most recently active experiments.
 *
 * <p>
 * <strong>Langfuse requires a lower bound on the time range</strong>; a listing without one is
 * answered with HTTP 400. The bound is supplied when this view is obtained, through
 * {@link AsyncExperimentTimeWindow#since(java.time.OffsetDateTime) since} or
 * {@link AsyncExperimentTimeWindow#between(java.time.OffsetDateTime, java.time.OffsetDateTime)
 * between}, so every instance of this type carries one and every inherited operation is usable as it
 * stands.
 *
 * <p>
 * <strong>Read-only, and with neither {@code findById} nor {@code findByName}.</strong> There is no
 * GET-by-id endpoint, and the {@code id} and {@code name} listing parameters are comma-separated
 * lists rather than unique keys. See {@link ExperimentOperations} for the full reasoning.
 *
 * @see ExperimentOperations
 */
public sealed interface AsyncExperimentOperations extends AsyncCursorOperations<Experiment>
        permits DefaultAsyncExperimentOperations {

    /**
     * A view of this collection restricted to the experiments matching {@code filter}.
     *
     * <p>
     * <strong>Replaces any filter already applied rather than combining with it.</strong>
     * {@code experiments().matching(a).matching(b)} is filtered by {@code b} alone. On the returned
     * view, every inherited operation is scoped to the view: {@link #findAll()} means "every
     * experiment <em>of this view</em>", not every experiment in the project.
     *
     * <p>
     * The view is a new instance; this one is unaffected and stays usable.
     *
     * The time window this view was obtained with is kept: a filter carries no time criteria.
     *
     * @param filter the criteria to restrict the collection to, must not be {@code null}; use
     *        {@link ExperimentFilter#none()} for a view restricted by nothing but its time window
     * @return a view of this collection restricted to the matching experiments. Never {@code null}
     * @throws IllegalArgumentException if {@code filter} is {@code null}. Thrown from this call rather
     *         than emitted as a failure
     */
    AsyncExperimentOperations matching(ExperimentFilter filter);
}
