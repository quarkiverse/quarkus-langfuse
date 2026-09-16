package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import com.langfuse.api.model.CreateEvaluationRuleRequest;
import com.langfuse.api.model.EvaluationRule;

import io.smallrye.mutiny.Uni;

/**
 * Higher-level operations over Langfuse evaluation rules, returning Mutiny types.
 *
 * <p>
 * Obtained from {@link AsyncLangfuseOperations#evaluationRules()}. The asynchronous counterpart of
 * {@link EvaluationRuleOperations}; the two behave identically apart from how absence is represented,
 * which follows each style's own convention: {@link java.util.Optional} for the synchronous tree, a
 * {@code null} item for the asynchronous one.
 *
 * @see EvaluationRuleOperations
 */
public sealed interface AsyncEvaluationRuleOperations extends AsyncCursorOperations<EvaluationRule>
        permits DefaultAsyncEvaluationRuleOperations {

    /**
     * Finds an evaluation rule by its exact name.
     *
     * <p>
     * <strong>Emits {@code null} if no rule has that name.</strong>
     *
     * @param ruleName the rule name to look for, must not be {@code null} or blank
     * @return the matching rule, or {@code null} if no rule has that name
     * @throws IllegalArgumentException if {@code ruleName} is {@code null} or blank
     */
    Uni<EvaluationRule> findByName(String ruleName);

    /**
     * Whether an evaluation rule with the given exact name exists.
     *
     * @param ruleName the rule name to look for, must not be {@code null} or blank
     * @return {@code true} if a rule with that name exists. Never {@code null}
     * @throws IllegalArgumentException if {@code ruleName} is {@code null} or blank
     */
    default Uni<Boolean> exists(String ruleName) {
        return findByName(ruleName)
                .map(Objects::nonNull);
    }

    /**
     * Returns the evaluation rule with the requested name, creating it if no rule has that name.
     *
     * <p>
     * <strong>Not atomic.</strong> Langfuse offers no create-or-update-by-name operation for evaluation
     * rules, so this performs a lookup followed by a create. Concurrent callers may therefore both
     * observe the rule as absent and both create it.
     *
     * @param request the evaluation rule to create if it is missing
     * @return the existing or newly created evaluation rule. Never {@code null}
     */
    Uni<EvaluationRule> createIfAbsent(CreateEvaluationRuleRequest request);

    /**
     * Deletes the evaluation rules with the given ids.
     *
     * <p>
     * <strong>Absence is not an error.</strong> An id matching no evaluation rule yields a
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
     * <p>
     * <strong>Narrower than it appears.</strong> This removes the live-ingestion rule only:
     * associated evaluators, and scores they have already produced, are preserved. Legacy trace and
     * dataset rules can also be deleted, and their evaluators and previously produced scores are
     * likewise preserved.
     *
     * @param ids the evaluation rule ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id. Never {@code null}
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank. Thrown from this call rather than emitted as a failure
     */
    Uni<DeletionResult> deleteById(Collection<String> ids);

    /**
     * Deletes the evaluation rule with the given id.
     *
     * @param id the evaluation rule id to delete, must not be {@code null} or blank
     * @return the outcome for that id. Never {@code null}
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @see #deleteById(Collection)
     */
    default Uni<DeletionResult> deleteById(String id) {
        return deleteById(Collections.singletonList(id));
    }

    /**
     * Deletes the evaluation rules with the given ids.
     *
     * @param ids the evaluation rule ids to delete, must not be {@code null} and must not contain
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
     * Deletes the evaluation rules with the given exact names.
     *
     * <p>
     * <strong>Absence is not an error.</strong> A name matching no evaluation rule yields a
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
     * <strong>Narrower than it appears.</strong> This removes the live-ingestion rule only:
     * associated evaluators, and scores they have already produced, are preserved. Legacy trace and
     * dataset rules can also be deleted, and their evaluators and previously produced scores are
     * likewise preserved.
     *
     * <p>
     * Each name is resolved to its id first, which walks the collection the way {@link #findByName}
     * does. A name that matches stops the scan early, but an <strong>absent</strong> name costs a
     * full traversal, so deleting many absent names is markedly more expensive than deleting the
     * same number of ids. Prefer {@link #deleteById(Collection)} where ids are already known.
     *
     * @param ruleNames the evaluation rule names to delete, must not be {@code null} and must not
     *        contain {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct name, keyed by the name supplied rather than by the resolved
     *         id. Never {@code null}
     * @throws IllegalArgumentException if {@code ruleNames} is {@code null}, or if any element is
     *         {@code null} or blank. Thrown from this call rather than emitted as a failure
     */
    Uni<DeletionResult> deleteByName(Collection<String> ruleNames);

    /**
     * Deletes the evaluation rule with the given exact name.
     *
     * @param ruleName the evaluation rule name to delete, must not be {@code null} or blank
     * @return the outcome for that name. Never {@code null}
     * @throws IllegalArgumentException if {@code ruleName} is {@code null} or blank
     * @see #deleteByName(Collection)
     */
    default Uni<DeletionResult> deleteByName(String ruleName) {
        return deleteByName(Collections.singletonList(ruleName));
    }

    /**
     * Deletes the evaluation rules with the given exact names.
     *
     * @param ruleNames the evaluation rule names to delete, must not be {@code null} and must not
     *        contain {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct name. Never {@code null}
     * @throws IllegalArgumentException if {@code ruleNames} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteByName(Collection)
     */
    default Uni<DeletionResult> deleteByName(String... ruleNames) {
        return deleteByName((ruleNames == null) ? null : Arrays.asList(ruleNames));
    }
}
