package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.langfuse.api.model.CreateCodeEvaluatorRequest1;
import com.langfuse.api.model.CreateEvaluatorRequest;
import com.langfuse.api.model.CreateLlmAsJudgeEvaluatorRequest1;
import com.langfuse.api.model.Evaluator;

import io.quarkiverse.langfuse.util.ValidationUtils;

/**
 * Higher-level operations over Langfuse evaluators.
 *
 * <p>
 * Obtained from {@link LangfuseOperations#evaluators()}. Like evaluation rules, evaluators are a
 * <strong>cursor-addressed</strong> collection - Langfuse reports only an opaque next cursor, not a
 * page index or totals - so {@link #stream}, {@link #streamBatches} and {@link #findBatch} - inherited
 * from {@link CursorOperations} - accept a {@link CursorSelection} or a {@link Cursor} rather than
 * their page-addressed equivalents.
 *
 * @see AsyncEvaluatorOperations
 */
public sealed interface EvaluatorOperations extends CursorOperations<Evaluator>
        permits DefaultEvaluatorOperations {

    /**
     * Finds an evaluator by its exact name.
     *
     * <p>
     * Langfuse offers no name filter for evaluators, so this walks the collection and stops at the first
     * match: an evaluator found in the first batch costs a single request.
     *
     * @param evaluatorName the evaluator name to look for, must not be {@code null} or blank
     * @return the matching evaluator, or empty if no evaluator has that name
     * @throws IllegalArgumentException if {@code evaluatorName} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         evaluator not existing
     */
    Optional<Evaluator> findByName(String evaluatorName);

    /**
     * Whether an evaluator with the given exact name exists.
     *
     * @param evaluatorName the evaluator name to look for, must not be {@code null} or blank
     * @return {@code true} if an evaluator with that name exists
     * @throws IllegalArgumentException if {@code evaluatorName} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         evaluator not existing
     */
    default boolean exists(String evaluatorName) {
        return findByName(evaluatorName).isPresent();
    }

    /**
     * Returns the evaluator with the requested name, creating it if no evaluator has that name.
     *
     * <p>
     * <strong>Not atomic.</strong> Langfuse offers no create-or-update-by-name operation for evaluators,
     * so this performs a lookup followed by a create. Concurrent callers may therefore both observe the
     * evaluator as absent and both create it.
     *
     * @param request the evaluator to create if it is missing
     * @return the existing or newly created evaluator
     */
    Evaluator createIfAbsent(CreateEvaluatorRequest request);

    /**
     * Convenience overload of {@link #createIfAbsent(CreateEvaluatorRequest)} for a code evaluator.
     *
     * @param request the code evaluator to create if it is missing
     * @return the existing or newly created evaluator
     * @throws IllegalArgumentException if {@code request} is {@code null}
     */
    default Evaluator createIfAbsent(CreateCodeEvaluatorRequest1 request) {
        ValidationUtils.ensureNotNull(request, "request");

        return createIfAbsent(new CreateEvaluatorRequest(request));
    }

    /**
     * Convenience overload of {@link #createIfAbsent(CreateEvaluatorRequest)} for an LLM-as-a-judge
     * evaluator.
     *
     * @param request the LLM-as-a-judge evaluator to create if it is missing
     * @return the existing or newly created evaluator
     * @throws IllegalArgumentException if {@code request} is {@code null}
     */
    default Evaluator createIfAbsent(CreateLlmAsJudgeEvaluatorRequest1 request) {
        ValidationUtils.ensureNotNull(request, "request");

        return createIfAbsent(new CreateEvaluatorRequest(request));
    }

    /**
     * Deletes the evaluators with the given ids.
     *
     * <p>
     * <strong>Deletes more than the evaluator itself.</strong> Langfuse removes the evaluator and all
     * of its stored versions, and drops the evaluation-rule assignments that reference it. Scores it
     * has already produced are preserved.
     *
     * <p>
     * <strong>Absence is not an error.</strong> An id matching no evaluator yields a
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
     * {@link AsyncEvaluatorOperations#deleteById(Collection)} instead.
     *
     * @param ids the evaluator ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     */
    DeletionResult deleteById(Collection<String> ids);

    /**
     * Deletes the evaluator with the given id.
     *
     * @param id the evaluator id to delete, must not be {@code null} or blank
     * @return the outcome for that id
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @see #deleteById(Collection)
     */
    default DeletionResult deleteById(String id) {
        return deleteById(Collections.singletonList(id));
    }

    /**
     * Deletes the evaluators with the given ids.
     *
     * @param ids the evaluator ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteById(Collection)
     */
    default DeletionResult deleteById(String... ids) {
        return deleteById((ids == null) ? null : Arrays.asList(ids));
    }

    /**
     * Deletes the evaluators with the given exact names.
     *
     * <p>
     * <strong>Deletes more than the evaluator itself.</strong> Langfuse removes the evaluator and all
     * of its stored versions, and drops the evaluation-rule assignments that reference it. Scores it
     * has already produced are preserved.
     *
     * <p>
     * <strong>Absence is not an error.</strong> A name matching no evaluator yields a
     * {@link DeletionOutcome.NotFound} outcome rather than throwing, so deleting something that is
     * already gone is a normal result rather than a failure to handle.
     *
     * <p>
     * <strong>Never fails fast.</strong> Every name is attempted regardless of what happened to the
     * others, and each is reported separately: one failure neither hides the successes nor prevents
     * the remaining work.
     *
     * <p>
     * <strong>Not atomic.</strong> Langfuse offers no bulk delete, so this iterates client-side and
     * can partially apply. Inspect the returned {@link DeletionResult} rather than assuming
     * all-or-nothing.
     *
     * <p>
     * Each name is resolved to its id first, which walks the collection the way {@link #findByName}
     * does. A name that matches stops the scan early, but an <strong>absent</strong> name costs a
     * full traversal, so deleting many absent names is markedly more expensive than deleting the
     * same number of ids. Prefer {@link #deleteById(Collection)} where ids are already known.
     *
     * <p>
     * <strong>Blocks the calling thread.</strong> Do not call this from a Vert.x I/O thread; use
     * {@link AsyncEvaluatorOperations#deleteByName(Collection)} instead.
     *
     * @param evaluatorNames the evaluator names to delete, must not be {@code null} and must not
     *        contain {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct name, keyed by the name supplied rather than by the resolved id
     * @throws IllegalArgumentException if {@code evaluatorNames} is {@code null}, or if any element is
     *         {@code null} or blank
     */
    DeletionResult deleteByName(Collection<String> evaluatorNames);

    /**
     * Deletes the evaluator with the given exact name.
     *
     * @param evaluatorName the evaluator name to delete, must not be {@code null} or blank
     * @return the outcome for that name
     * @throws IllegalArgumentException if {@code evaluatorName} is {@code null} or blank
     * @see #deleteByName(Collection)
     */
    default DeletionResult deleteByName(String evaluatorName) {
        return deleteByName(Collections.singletonList(evaluatorName));
    }

    /**
     * Deletes the evaluators with the given exact names.
     *
     * @param evaluatorNames the evaluator names to delete, must not be {@code null} and must not
     *        contain {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct name
     * @throws IllegalArgumentException if {@code evaluatorNames} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteByName(Collection)
     */
    default DeletionResult deleteByName(String... evaluatorNames) {
        return deleteByName((evaluatorNames == null) ? null : Arrays.asList(evaluatorNames));
    }
}
