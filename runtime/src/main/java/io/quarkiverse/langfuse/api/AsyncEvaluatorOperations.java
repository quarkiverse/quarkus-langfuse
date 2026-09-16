package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import com.langfuse.api.model.CreateCodeEvaluatorRequest1;
import com.langfuse.api.model.CreateEvaluatorRequest;
import com.langfuse.api.model.CreateLlmAsJudgeEvaluatorRequest1;
import com.langfuse.api.model.Evaluator;

import io.quarkiverse.langfuse.util.ValidationUtils;
import io.smallrye.mutiny.Uni;

/**
 * Higher-level operations over Langfuse evaluators, returning Mutiny types.
 *
 * <p>
 * Obtained from {@link AsyncLangfuseOperations#evaluators()}. The asynchronous counterpart of
 * {@link EvaluatorOperations}; the two behave identically apart from how absence is represented,
 * which follows each style's own convention: {@link java.util.Optional} for the synchronous tree, a
 * {@code null} item for the asynchronous one.
 *
 * @see EvaluatorOperations
 */
public sealed interface AsyncEvaluatorOperations extends AsyncCursorOperations<Evaluator>
        permits DefaultAsyncEvaluatorOperations {

    /**
     * Finds an evaluator by its exact name.
     *
     * <p>
     * <strong>Emits {@code null} if no evaluator has that name.</strong>
     *
     * @param evaluatorName the evaluator name to look for, must not be {@code null} or blank
     * @return the matching evaluator, or {@code null} if no evaluator has that name
     * @throws IllegalArgumentException if {@code evaluatorName} is {@code null} or blank
     */
    Uni<Evaluator> findByName(String evaluatorName);

    /**
     * Whether an evaluator with the given exact name exists.
     *
     * @param evaluatorName the evaluator name to look for, must not be {@code null} or blank
     * @return {@code true} if an evaluator with that name exists. Never {@code null}
     * @throws IllegalArgumentException if {@code evaluatorName} is {@code null} or blank
     */
    default Uni<Boolean> exists(String evaluatorName) {
        return findByName(evaluatorName)
                .map(Objects::nonNull);
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
     * @return the existing or newly created evaluator. Never {@code null}
     */
    Uni<Evaluator> createIfAbsent(CreateEvaluatorRequest request);

    /**
     * Convenience overload of {@link #createIfAbsent(CreateEvaluatorRequest)} for a code evaluator.
     *
     * @param request the code evaluator to create if it is missing
     * @return the existing or newly created evaluator. Never {@code null}
     * @throws IllegalArgumentException if {@code request} is {@code null}
     */
    default Uni<Evaluator> createIfAbsent(CreateCodeEvaluatorRequest1 request) {
        ValidationUtils.ensureNotNull(request, "request");

        return createIfAbsent(new CreateEvaluatorRequest(request));
    }

    /**
     * Convenience overload of {@link #createIfAbsent(CreateEvaluatorRequest)} for an LLM-as-a-judge
     * evaluator.
     *
     * @param request the LLM-as-a-judge evaluator to create if it is missing
     * @return the existing or newly created evaluator. Never {@code null}
     * @throws IllegalArgumentException if {@code request} is {@code null}
     */
    default Uni<Evaluator> createIfAbsent(CreateLlmAsJudgeEvaluatorRequest1 request) {
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
     * @param ids the evaluator ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id. Never {@code null}
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank. Thrown from this call rather than emitted as a failure
     */
    Uni<DeletionResult> deleteById(Collection<String> ids);

    /**
     * Deletes the evaluator with the given id.
     *
     * @param id the evaluator id to delete, must not be {@code null} or blank
     * @return the outcome for that id. Never {@code null}
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @see #deleteById(Collection)
     */
    default Uni<DeletionResult> deleteById(String id) {
        return deleteById(Collections.singletonList(id));
    }

    /**
     * Deletes the evaluators with the given ids.
     *
     * @param ids the evaluator ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id. Never {@code null}
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteById(Collection)
     */
    default Uni<DeletionResult> deleteById(String... ids) {
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
     * {@link DeletionOutcome.NotFound} outcome rather than failing the {@link Uni}, so deleting
     * something that is already gone is a normal result rather than a failure to handle.
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
     * @param evaluatorNames the evaluator names to delete, must not be {@code null} and must not
     *        contain {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct name, keyed by the name supplied rather than by the resolved
     *         id. Never {@code null}
     * @throws IllegalArgumentException if {@code evaluatorNames} is {@code null}, or if any element is
     *         {@code null} or blank. Thrown from this call rather than emitted as a failure
     */
    Uni<DeletionResult> deleteByName(Collection<String> evaluatorNames);

    /**
     * Deletes the evaluator with the given exact name.
     *
     * @param evaluatorName the evaluator name to delete, must not be {@code null} or blank
     * @return the outcome for that name. Never {@code null}
     * @throws IllegalArgumentException if {@code evaluatorName} is {@code null} or blank
     * @see #deleteByName(Collection)
     */
    default Uni<DeletionResult> deleteByName(String evaluatorName) {
        return deleteByName(Collections.singletonList(evaluatorName));
    }

    /**
     * Deletes the evaluators with the given exact names.
     *
     * @param evaluatorNames the evaluator names to delete, must not be {@code null} and must not
     *        contain {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct name. Never {@code null}
     * @throws IllegalArgumentException if {@code evaluatorNames} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteByName(Collection)
     */
    default Uni<DeletionResult> deleteByName(String... evaluatorNames) {
        return deleteByName((evaluatorNames == null) ? null : Arrays.asList(evaluatorNames));
    }
}
