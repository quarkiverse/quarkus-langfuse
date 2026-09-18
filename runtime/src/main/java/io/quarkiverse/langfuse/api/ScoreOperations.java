package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.langfuse.api.model.CreateScoreRequest;
import com.langfuse.api.model.CreateScoreResponse;
import com.langfuse.api.model.ScoreV3;

import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorSelection;
import io.quarkiverse.langfuse.api.deletion.DeletionOutcome;
import io.quarkiverse.langfuse.api.deletion.DeletionResult;

/**
 * Higher-level operations over Langfuse scores.
 *
 * <p>
 * Obtained from {@link LangfuseOperations#scores()}. Scores are a <strong>cursor-addressed</strong>
 * collection - Langfuse reports only an opaque next cursor, not a page index or totals - so
 * {@link #stream}, {@link #streamBatches} and {@link #findBatch} - inherited from
 * {@link CursorOperations} - accept a {@link CursorSelection} or a {@link Cursor} directly.
 *
 * <p>
 * <strong>There is no lookup by id.</strong> The only Langfuse endpoint fetching a single score by
 * id belongs to the deprecated v3 API and is scheduled for removal, so this layer does not build on
 * it. A score is reached instead through the {@link ScoreFilter#id() id} criterion on
 * {@link #matching(ScoreFilter)}, which is a filtered listing rather than a direct lookup and is
 * named as such.
 *
 * <p>
 * <strong>There is no lookup by name either.</strong> A score has a name, but several scores can
 * share one, so a name identifies a group rather than a score. Narrow by name with
 * {@link ScoreFilter#name()}.
 *
 * <p>
 * <strong>Three Langfuse endpoints back this domain</strong>, and they do not share a version: the
 * listing is {@code GET /api/public/v3/scores}, the create is {@code POST /api/public/scores}, and
 * the delete is {@code DELETE /api/public/scores/{scoreId}}. That is invisible to callers and is
 * noted only because it explains why the listing carries filters the other two know nothing about.
 *
 * <pre>{@code
 * langfuse.scores().findAll();
 * langfuse.scores().matching(filter).findAll();
 * langfuse.scores().deleteById("score-1");
 * }</pre>
 *
 * @see AsyncScoreOperations
 */
public sealed interface ScoreOperations extends CursorOperations<ScoreV3> permits DefaultScoreOperations {

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
     * @return a view of this collection restricted to the matching scores
     * @throws IllegalArgumentException if {@code filter} is {@code null}
     */
    ScoreOperations matching(ScoreFilter filter);

    /**
     * Creates a score.
     *
     * <p>
     * What the score is attached to - a trace, an observation, a session or an experiment - is named
     * on the request itself, and Langfuse answers with the id it assigned rather than the stored
     * score.
     *
     * @param request the score to create, must not be {@code null}
     * @return the id Langfuse assigned to the created score
     * @throws IllegalArgumentException if {@code request} is {@code null}
     * @throws com.langfuse.api.LangfuseApiException if the request fails
     */
    CreateScoreResponse create(CreateScoreRequest request);

    /**
     * Deletes the scores with the given ids.
     *
     * <p>
     * <strong>Absence is not an error.</strong> An id matching no score yields a
     * {@link DeletionOutcome.NotFound} outcome rather than throwing, so deleting something that is
     * already gone is a normal result rather than a failure to handle.
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
     * <p>
     * <strong>Blocks the calling thread.</strong> Do not call this from a Vert.x I/O thread; use
     * {@link AsyncScoreOperations#deleteById(Collection)} instead.
     *
     * @param ids the score ids to delete, must not be {@code null} and must not contain {@code null}
     *        or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     */
    DeletionResult deleteById(Collection<String> ids);

    /**
     * Deletes the score with the given id.
     *
     * @param id the score id to delete, must not be {@code null} or blank
     * @return the outcome for that id
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @see #deleteById(Collection)
     */
    default DeletionResult deleteById(String id) {
        return deleteById(Collections.singletonList(id));
    }

    /**
     * Deletes the scores with the given ids.
     *
     * @param ids the score ids to delete, must not be {@code null} and must not contain {@code null}
     *        or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteById(Collection)
     */
    default DeletionResult deleteById(String... ids) {
        return deleteById((ids == null) ? null : Arrays.asList(ids));
    }
}
