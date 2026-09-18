package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.langfuse.api.model.CreateScoreRequest;
import com.langfuse.api.model.CreateScoreResponse;
import com.langfuse.api.model.ScoreV3;

import io.quarkiverse.langfuse.api.deletion.DeletionOutcome;
import io.quarkiverse.langfuse.api.deletion.DeletionResult;
import io.smallrye.mutiny.Uni;

/**
 * Higher-level operations over Langfuse scores, returning Mutiny types.
 *
 * <p>
 * Obtained from {@link AsyncLangfuseOperations#scores()}. The asynchronous counterpart of
 * {@link ScoreOperations}; the two behave identically apart from how absence is represented, which
 * follows each style's own convention: {@link java.util.Optional} for the synchronous tree, a
 * {@code null} item for the asynchronous one.
 *
 * <p>
 * <strong>There is no lookup by id or by name.</strong> The only by-id endpoint belongs to the
 * deprecated v3 API and is scheduled for removal, and a score name is shared by many scores rather
 * than identifying one. Both are reached instead through {@link ScoreFilter#id()} and
 * {@link ScoreFilter#name()} on {@link #matching(ScoreFilter)}, which are filtered listings.
 *
 * @see ScoreOperations
 */
public sealed interface AsyncScoreOperations extends AsyncCursorOperations<ScoreV3> permits DefaultAsyncScoreOperations {

    /**
     * A view of this collection restricted to the scores matching {@code filter}.
     *
     * <p>
     * <strong>Replaces any filter already applied rather than combining with it.</strong>
     * {@code scores().matching(a).matching(b)} is filtered by {@code b} alone. On the returned view,
     * every inherited operation is scoped to the view: {@link #findAll()} means "every score
     * <em>of this view</em>", not every score in the project.
     *
     * <p>
     * The view is a new instance; this one is unaffected and stays usable.
     *
     * <p>
     * A filter combination Langfuse rejects - see {@link ScoreFilter} for the rules it enforces -
     * surfaces when the view is traversed rather than from this call.
     *
     * @param filter the criteria to restrict the collection to, must not be {@code null}; use
     *        {@link ScoreFilter#none()} for an unrestricted view
     * @return a view of this collection restricted to the matching scores. Never {@code null}
     * @throws IllegalArgumentException if {@code filter} is {@code null}. Thrown from this call rather
     *         than emitted as a failure
     */
    AsyncScoreOperations matching(ScoreFilter filter);

    /**
     * Creates a score.
     *
     * <p>
     * What the score is attached to - a trace, an observation, a session or an experiment - is named
     * on the request itself, and Langfuse answers with the id it assigned rather than the stored
     * score.
     *
     * @param request the score to create, must not be {@code null}
     * @return the id Langfuse assigned to the created score. Never {@code null}
     * @throws IllegalArgumentException if {@code request} is {@code null}. Thrown from this call rather
     *         than emitted as a failure
     */
    Uni<CreateScoreResponse> create(CreateScoreRequest request);

    /**
     * Deletes the scores with the given ids.
     *
     * <p>
     * <strong>Absence is not an error.</strong> An id matching no score yields a
     * {@link DeletionOutcome.NotFound} outcome rather than failing the {@link Uni}, so deleting
     * something that is already gone is a normal result rather than a failure to handle.
     *
     * <p>
     * <strong>Never fails fast.</strong> Every id is attempted regardless of what happened to the
     * others, and each is reported separately: one failure neither hides the successes nor prevents
     * the remaining work.
     *
     * <p>
     * <strong>Not atomic.</strong> Langfuse offers no bulk delete, so this iterates client-side and
     * can partially apply. Inspect the returned {@link DeletionResult} rather than assuming
     * all-or-nothing.
     *
     * @param ids the score ids to delete, must not be {@code null} and must not contain {@code null}
     *        or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id. Never {@code null}
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank. Thrown from this call rather than emitted as a failure
     */
    Uni<DeletionResult> deleteById(Collection<String> ids);

    /**
     * Deletes the score with the given id.
     *
     * @param id the score id to delete, must not be {@code null} or blank
     * @return the outcome for that id. Never {@code null}
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @see #deleteById(Collection)
     */
    default Uni<DeletionResult> deleteById(String id) {
        return deleteById(Collections.singletonList(id));
    }

    /**
     * Deletes the scores with the given ids.
     *
     * @param ids the score ids to delete, must not be {@code null} and must not contain {@code null}
     *        or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id. Never {@code null}
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteById(Collection)
     */
    default Uni<DeletionResult> deleteById(String... ids) {
        return deleteById((ids == null) ? null : Arrays.asList(ids));
    }
}
