package io.quarkiverse.langfuse.api;

import com.langfuse.api.model.ExperimentItem;

import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorSelection;

/**
 * Higher-level operations over Langfuse experiment items.
 *
 * <p>
 * Obtained from {@link ExperimentItemTimeWindow}, which is what
 * {@link LangfuseOperations#experimentItems()} returns. Experiment items are a
 * <strong>cursor-addressed</strong> collection - Langfuse reports only an opaque next cursor, not a
 * page index or totals - so {@link #stream}, {@link #streamBatches} and {@link #findBatch} -
 * inherited from {@link CursorOperations} - accept a {@link CursorSelection} or a {@link Cursor}
 * rather than their page-addressed equivalents.
 *
 * <p>
 * <strong>This is a top-level collection, not a sub-collection of experiments.</strong> It is
 * reached from {@link LangfuseOperations#experimentItems()} rather than from
 * {@link ExperimentOperations}, because Langfuse serves it from its own path and takes the parent
 * experiment as a <em>query criterion</em> rather than as a path segment. Narrowing to one
 * experiment is therefore a filter - {@link ExperimentItemFilter.Builder#experimentId(String)} or
 * {@link ExperimentItemFilter.Builder#experimentName(String)} - and walking every experiment's items
 * at once is a supported query rather than a missing parent.
 *
 * <p>
 * <strong>Results are ordered by time, descending.</strong> That ordering decides what a partial
 * walk returns: the first batch holds the most recent items, so
 * {@code find(CursorSelection.first(1))} is "the latest items", not an arbitrary sample.
 *
 * <p>
 * <strong>Langfuse requires a lower bound on the time range</strong>, so that the query stays fast
 * on large projects: the listing endpoint answers HTTP 400 without one. That bound is not an
 * optional criterion here - it is supplied when this view is obtained, through
 * {@link ExperimentItemTimeWindow#since(java.time.OffsetDateTime) since} or
 * {@link ExperimentItemTimeWindow#between(java.time.OffsetDateTime, java.time.OffsetDateTime)
 * between}, and every instance of this type therefore carries one. Every inherited operation is
 * consequently usable as it stands, rather than being a call that is guaranteed to fail.
 *
 * <p>
 * <strong>Read-only.</strong> Langfuse exposes only a listing on
 * {@code /api/public/experiment-items}, so this domain offers no create, no update and no delete.
 * Items are produced by running an experiment, not by this API.
 *
 * <p>
 * <strong>There is neither {@code findById} nor {@code findByName}.</strong> Not an oversight: an
 * experiment item has no name of its own - the {@code experimentName} it carries names its
 * <em>parent</em> - and there is no GET-by-id endpoint, with the {@code experimentItemId} listing
 * parameter being a comma-separated list rather than a unique key. Select one with a
 * {@link ExperimentItemFilter filter} instead, which says plainly that the answer is a collection.
 *
 * <pre>{@code
 * var lastWeek = langfuse.experimentItems().since(OffsetDateTime.now().minusDays(7));
 *
 * lastWeek.findAll();
 * lastWeek.matching(ExperimentItemFilter.builder().experimentName("nightly-eval").build()).findAll();
 * }</pre>
 *
 * @see AsyncExperimentItemOperations
 */
public sealed interface ExperimentItemOperations extends CursorOperations<ExperimentItem>
        permits DefaultExperimentItemOperations {

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
     * @return a view of this collection restricted to the matching experiment items
     * @throws IllegalArgumentException if {@code filter} is {@code null}
     */
    ExperimentItemOperations matching(ExperimentItemFilter filter);
}
