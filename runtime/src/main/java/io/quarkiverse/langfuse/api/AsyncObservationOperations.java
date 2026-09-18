package io.quarkiverse.langfuse.api;

import com.langfuse.api.model.ObservationV2;

/**
 * Higher-level operations over Langfuse observations, returning Mutiny types.
 *
 * <p>
 * Obtained from {@link AsyncLangfuseOperations#observations()}. The asynchronous counterpart of
 * {@link ObservationOperations}; the two behave identically, and since this domain is read-only and
 * has no lookup returning "maybe one thing", the absence convention that separates the two trees
 * elsewhere never comes into play here.
 *
 * <p>
 * <strong>This is the only real-time read path.</strong> Together with OpenTelemetry ingestion and
 * the Metrics v2 API, observations v2 serves tracing data as soon as it is ingested; the other
 * public Langfuse read endpoints can lag by about ten minutes.
 *
 * <p>
 * <strong>Read-only, and with no {@code findById}.</strong> Langfuse exposes only a listing on
 * {@code /api/public/v2/observations}, and the single-observation lookup exists only on the
 * deprecated v1 API, which this layer does not build on. See {@link ObservationOperations} for the
 * full reasoning.
 *
 * @see ObservationOperations
 */
public sealed interface AsyncObservationOperations extends AsyncCursorOperations<ObservationV2>
        permits DefaultAsyncObservationOperations {

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
     * @return a view of this collection restricted to the matching observations. Never {@code null}
     * @throws IllegalArgumentException if {@code filter} is {@code null}. Thrown from this call rather
     *         than emitted as a failure
     */
    AsyncObservationOperations matching(ObservationFilter filter);
}
