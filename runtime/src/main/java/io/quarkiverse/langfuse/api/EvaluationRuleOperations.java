package io.quarkiverse.langfuse.api;

import java.util.Optional;

import com.langfuse.api.model.CreateEvaluationRuleRequest;
import com.langfuse.api.model.EvaluationRule;

/**
 * Higher-level operations over Langfuse evaluation rules.
 *
 * <p>
 * Obtained from {@link LangfuseOperations#evaluationRules()}. Unlike the other domains in this layer,
 * evaluation rules are a <strong>cursor-addressed</strong> collection - Langfuse reports only an
 * opaque next cursor, not a page index or totals - so {@link #stream}, {@link #streamBatches} and
 * {@link #findBatch} - inherited from {@link CursorOperations} - accept a {@link CursorSelection} or
 * a {@link Cursor} rather than their page-addressed equivalents.
 *
 * @see AsyncEvaluationRuleOperations
 */
public sealed interface EvaluationRuleOperations extends CursorOperations<EvaluationRule>
        permits DefaultEvaluationRuleOperations {

    /**
     * Finds an evaluation rule by its exact name.
     *
     * <p>
     * Langfuse offers no name filter for evaluation rules, so this walks the collection and stops at
     * the first match: a rule found in the first batch costs a single request.
     *
     * @param ruleName the rule name to look for, must not be {@code null} or blank
     * @return the matching rule, or empty if no rule has that name
     * @throws IllegalArgumentException if {@code ruleName} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         rule not existing
     */
    Optional<EvaluationRule> findByName(String ruleName);

    /**
     * Whether an evaluation rule with the given exact name exists.
     *
     * @param ruleName the rule name to look for, must not be {@code null} or blank
     * @return {@code true} if a rule with that name exists
     * @throws IllegalArgumentException if {@code ruleName} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         rule not existing
     */
    default boolean exists(String ruleName) {
        return findByName(ruleName).isPresent();
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
     * @return the existing or newly created evaluation rule
     */
    EvaluationRule createIfAbsent(CreateEvaluationRuleRequest request);
}
