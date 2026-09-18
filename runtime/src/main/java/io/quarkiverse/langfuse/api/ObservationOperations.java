package io.quarkiverse.langfuse.api;

import com.langfuse.api.model.ObservationV2;

import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorSelection;

/**
 * Higher-level operations over Langfuse observations.
 *
 * <p>
 * Obtained from {@link LangfuseOperations#observations()}. Observations are a
 * <strong>cursor-addressed</strong> collection - Langfuse reports only an opaque next cursor, not a
 * page index or totals - so {@link #stream}, {@link #streamBatches} and {@link #findBatch} -
 * inherited from {@link CursorOperations} - accept a {@link CursorSelection} or a {@link Cursor}
 * rather than their page-addressed equivalents.
 *
 * <p>
 * <strong>This is the only real-time read path.</strong> Together with OpenTelemetry ingestion and
 * the Metrics v2 API, observations v2 serves tracing data as soon as it is ingested; the other
 * public Langfuse read endpoints can lag by about ten minutes. That is the main reason to reach for
 * this domain rather than the trace endpoints.
 *
 * <p>
 * <strong>Read-only.</strong> Langfuse exposes only a listing on {@code /api/public/v2/observations},
 * so this domain offers no create, no update and no delete. Observations are written by ingestion,
 * not by this API.
 *
 * <p>
 * <strong>There is no {@code findById}.</strong> Not an oversight: the only Langfuse endpoint that
 * fetches a single observation by id belongs to the deprecated v1 observations API, which this layer
 * does not build on. Select a single observation with a {@link ObservationFilter filter} instead -
 * {@link ObservationFilter.Builder#traceId(String) traceId} and
 * {@link ObservationFilter.Builder#parentObservationId(String) parentObservationId} narrow a walk to
 * one trace or one subtree.
 *
 * <pre>{@code
 * langfuse.observations().findAll();
 * langfuse.observations().matching(filter).findAll();
 * }</pre>
 *
 * @see AsyncObservationOperations
 */
public sealed interface ObservationOperations extends CursorOperations<ObservationV2>
        permits DefaultObservationOperations {

    /**
     * A view of this collection restricted to the observations matching {@code filter}.
     *
     * <p>
     * <strong>Replaces any filter already applied rather than combining with it.</strong>
     * {@code observations().matching(a).matching(b)} is filtered by {@code b} alone. On the returned
     * view, every inherited operation is scoped to the view: {@link #findAll()} means "every
     * observation <em>of this view</em>", not every observation in the project.
     *
     * <p>
     * The view is a new instance; this one is unaffected and stays usable.
     *
     * @param filter the criteria to restrict the collection to, must not be {@code null}; use
     *        {@link ObservationFilter#none()} for an unrestricted view
     * @return a view of this collection restricted to the matching observations
     * @throws IllegalArgumentException if {@code filter} is {@code null}
     */
    ObservationOperations matching(ObservationFilter filter);
}
