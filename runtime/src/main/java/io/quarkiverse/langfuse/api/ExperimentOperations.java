package io.quarkiverse.langfuse.api;

import com.langfuse.api.model.Experiment;

import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorSelection;

/**
 * Higher-level operations over Langfuse experiments.
 *
 * <p>
 * Obtained from {@link ExperimentTimeWindow}, which is what
 * {@link LangfuseOperations#experiments()} returns. Experiments are a
 * <strong>cursor-addressed</strong> collection - Langfuse reports only an opaque next cursor, not a
 * page index or totals - so {@link #stream}, {@link #streamBatches} and {@link #findBatch} -
 * inherited from {@link CursorOperations} - accept a {@link CursorSelection} or a {@link Cursor}
 * rather than their page-addressed equivalents.
 *
 * <p>
 * <strong>Results are ordered by latest experiment activity, descending.</strong> That ordering
 * decides what a partial walk returns: the first batch holds the most recently active experiments,
 * so {@code find(CursorSelection.first(1))} is "the most recent experiments", not an arbitrary
 * sample.
 *
 * <p>
 * <strong>Langfuse requires a lower bound on the time range</strong>, so that the query stays fast
 * on large projects: the listing endpoint answers HTTP 400 without one. That bound is not an
 * optional criterion here - it is supplied when this view is obtained, through
 * {@link ExperimentTimeWindow#since(java.time.OffsetDateTime) since} or
 * {@link ExperimentTimeWindow#between(java.time.OffsetDateTime, java.time.OffsetDateTime) between},
 * and every instance of this type therefore carries one. Every inherited operation is consequently
 * usable as it stands, rather than being a call that is guaranteed to fail.
 *
 * <p>
 * <strong>Read-only.</strong> Langfuse exposes only the two listings on
 * {@code /api/public/experiments} and {@code /api/public/experiment-items}, so this domain offers no
 * create, no update and no delete. Experiments are produced by running a dataset, not by this API.
 *
 * <p>
 * <strong>There is neither {@code findById} nor {@code findByName}.</strong> Not an oversight: there
 * is no GET-by-id endpoint, and the {@code id} and {@code name} listing parameters are
 * <em>comma-separated lists</em> rather than unique keys - neither selects at most one experiment,
 * so neither can back a lookup returning "maybe one thing" without lying about it. A lookup would
 * also have to invent the mandatory time bound described above. Select by id or name with a
 * {@link ExperimentFilter filter} instead, which says plainly that the answer is a collection.
 *
 * <pre>{@code
 * var lastWeek = langfuse.experiments().since(OffsetDateTime.now().minusDays(7));
 *
 * lastWeek.findAll();
 * lastWeek.matching(ExperimentFilter.builder().name("nightly-eval").build()).findAll();
 * }</pre>
 *
 * @see AsyncExperimentOperations
 */
public sealed interface ExperimentOperations extends CursorOperations<Experiment>
        permits DefaultExperimentOperations {

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
     * @return a view of this collection restricted to the matching experiments
     * @throws IllegalArgumentException if {@code filter} is {@code null}
     */
    ExperimentOperations matching(ExperimentFilter filter);
}
