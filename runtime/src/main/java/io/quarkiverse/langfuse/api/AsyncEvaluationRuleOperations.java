package io.quarkiverse.langfuse.api;

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
}
