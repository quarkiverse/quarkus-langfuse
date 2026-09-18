package io.quarkiverse.langfuse.api;

import com.langfuse.api.model.ExperimentItem;

/**
 * Higher-level operations over Langfuse experiment items, returning Mutiny types.
 *
 * <p>
 * Obtained from {@link AsyncExperimentItemTimeWindow}, which is what
 * {@link AsyncLangfuseOperations#experimentItems()} returns. The asynchronous counterpart of
 * {@link ExperimentItemOperations}; the two behave identically, and since this domain is read-only
 * and has no lookup returning "maybe one thing", the absence convention that separates the two trees
 * elsewhere never comes into play here.
 *
 * <p>
 * <strong>This is a top-level collection, not a sub-collection of experiments.</strong> The parent
 * experiment is a query criterion rather than a path segment, so narrowing to one experiment is a
 * filter. See {@link ExperimentItemOperations}.
 *
 * <p>
 * <strong>Results are ordered by time, descending</strong>, so the first batch of a walk holds the
 * most recent items.
 *
 * <p>
 * <strong>Langfuse requires a lower bound on the time range</strong>; a listing without one is
 * answered with HTTP 400. The bound is supplied when this view is obtained, through
 * {@link AsyncExperimentItemTimeWindow#since(java.time.OffsetDateTime) since} or
 * {@link AsyncExperimentItemTimeWindow#between(java.time.OffsetDateTime, java.time.OffsetDateTime)
 * between}, so every instance of this type carries one and every inherited operation is usable as it
 * stands.
 *
 * <p>
 * <strong>Read-only, and with neither {@code findById} nor {@code findByName}.</strong> An item has
 * no name of its own and there is no GET-by-id endpoint. See {@link ExperimentItemOperations} for
 * the full reasoning.
 *
 * @see ExperimentItemOperations
 */
public sealed interface AsyncExperimentItemOperations extends AsyncCursorOperations<ExperimentItem>
        permits DefaultAsyncExperimentItemOperations {

    /**
     * A view of this collection restricted to the experiment items matching {@code filter}.
     *
     * <p>
     * <strong>Replaces any filter already applied rather than combining with it.</strong>
     * {@code experimentItems().matching(a).matching(b)} is filtered by {@code b} alone. On the
     * returned view, every inherited operation is scoped to the view: {@link #findAll()} means "every
     * experiment item <em>of this view</em>", not every experiment item in the project.
     *
     * <p>
     * The view is a new instance; this one is unaffected and stays usable.
     *
     * The time window this view was obtained with is kept: a filter carries no time criteria.
     *
     * @param filter the criteria to restrict the collection to, must not be {@code null}; use
     *        {@link ExperimentItemFilter#none()} for a view restricted by nothing but its time window
     * @return a view of this collection restricted to the matching experiment items. Never
     *         {@code null}
     * @throws IllegalArgumentException if {@code filter} is {@code null}. Thrown from this call rather
     *         than emitted as a failure
     */
    AsyncExperimentItemOperations matching(ExperimentItemFilter filter);
}
