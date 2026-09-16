package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

    /**
     * Deletes the evaluation rules with the given ids.
     *
     * <p>
     * <strong>Absence is not an error.</strong> An id matching no evaluation rule yields a
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
     * <strong>Narrower than it appears.</strong> This removes the live-ingestion rule only:
     * associated evaluators, and scores they have already produced, are preserved. Legacy trace and
     * dataset rules can also be deleted, and their evaluators and previously produced scores are
     * likewise preserved.
     *
     * <p>
     * <strong>Blocks the calling thread.</strong> Do not call this from a Vert.x I/O thread; use
     * {@link AsyncEvaluationRuleOperations#deleteById(Collection)} instead.
     *
     * @param ids the evaluation rule ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     */
    DeletionResult deleteById(Collection<String> ids);

    /**
     * Deletes the evaluation rule with the given id.
     *
     * @param id the evaluation rule id to delete, must not be {@code null} or blank
     * @return the outcome for that id
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @see #deleteById(Collection)
     */
    default DeletionResult deleteById(String id) {
        return deleteById(Collections.singletonList(id));
    }

    /**
     * Deletes the evaluation rules with the given ids.
     *
     * @param ids the evaluation rule ids to delete, must not be {@code null} and must not contain
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
     * Deletes the evaluation rules with the given exact names.
     *
     * <p>
     * <strong>Absence is not an error.</strong> A name matching no evaluation rule yields a
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
     * <p>
     * <strong>Blocks the calling thread.</strong> Do not call this from a Vert.x I/O thread; use
     * {@link AsyncEvaluationRuleOperations#deleteByName(Collection)} instead.
     *
     * @param ruleNames the evaluation rule names to delete, must not be {@code null} and must not
     *        contain {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct name, keyed by the name supplied rather than by the resolved id
     * @throws IllegalArgumentException if {@code ruleNames} is {@code null}, or if any element is
     *         {@code null} or blank
     */
    DeletionResult deleteByName(Collection<String> ruleNames);

    /**
     * Deletes the evaluation rule with the given exact name.
     *
     * @param ruleName the evaluation rule name to delete, must not be {@code null} or blank
     * @return the outcome for that name
     * @throws IllegalArgumentException if {@code ruleName} is {@code null} or blank
     * @see #deleteByName(Collection)
     */
    default DeletionResult deleteByName(String ruleName) {
        return deleteByName(Collections.singletonList(ruleName));
    }

    /**
     * Deletes the evaluation rules with the given exact names.
     *
     * @param ruleNames the evaluation rule names to delete, must not be {@code null} and must not
     *        contain {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct name
     * @throws IllegalArgumentException if {@code ruleNames} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteByName(Collection)
     */
    default DeletionResult deleteByName(String... ruleNames) {
        return deleteByName((ruleNames == null) ? null : Arrays.asList(ruleNames));
    }
}
