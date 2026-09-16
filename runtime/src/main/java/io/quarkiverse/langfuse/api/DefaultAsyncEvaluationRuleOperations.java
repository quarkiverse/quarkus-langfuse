package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.function.Function;

import com.langfuse.api.evaluationRules.EvaluationRulesApi.APIEvaluationRulesCreateRequest;
import com.langfuse.api.evaluationRules.EvaluationRulesApi.APIEvaluationRulesDeleteRequest;
import com.langfuse.api.evaluationRules.EvaluationRulesApi.APIEvaluationRulesListRequest;
import com.langfuse.api.evaluationRules.async.EvaluationRulesApi;
import com.langfuse.api.model.CreateEvaluationRuleRequest;
import com.langfuse.api.model.EvaluationRule;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;
import io.smallrye.mutiny.Uni;

final class DefaultAsyncEvaluationRuleOperations extends AbstractAsyncCursorOperations<EvaluationRule>
        implements AsyncEvaluationRuleOperations {
    private final EvaluationRulesApi evaluationRulesApi;

    DefaultAsyncEvaluationRuleOperations(EvaluationRulesApi evaluationRulesApi, LangfuseConfig config) {
        super(cursor -> fetch(evaluationRulesApi, cursor), config);
        this.evaluationRulesApi = evaluationRulesApi;
    }

    @Override
    public Uni<EvaluationRule> findByName(String ruleName) {
        return scanForName(ValidationUtils.ensureNotBlank(ruleName, "Evaluation rule name"), EvaluationRule::getName);
    }

    @Override
    public Uni<EvaluationRule> createIfAbsent(CreateEvaluationRuleRequest request) {
        // See DefaultAsyncModelOperations.createIfAbsent for why flatMap + ternary.
        return findByName(request.getName())
                .flatMap(existing -> (existing != null)
                        ? Uni.createFrom().item(existing)
                        : Uni.createFrom().completionStage(() -> this.evaluationRulesApi.evaluationRulesCreate(
                                APIEvaluationRulesCreateRequest.newBuilder()
                                        .createEvaluationRuleRequest(request)
                                        .build())));
    }

    @Override
    public Uni<DeletionResult> deleteById(Collection<String> ids) {
        return deferDeleteAll(ids, "Evaluation rule id", Uni.createFrom()::item);
    }

    @Override
    public Uni<DeletionResult> deleteByName(Collection<String> ruleNames) {
        return deferDeleteAll(ruleNames, "Evaluation rule name", this::resolveByName);
    }

    // Validation runs before the deferred wrapper so malformed input throws from the call, as the rest
    // of this tree does. Inside the supplier it would surface as a failed Uni at subscription instead.
    // deleteConcurrency() stays inside, so the config is read per subscription rather than once here.
    private Uni<DeletionResult> deferDeleteAll(Collection<String> identifiers, String label,
            Function<String, Uni<String>> resolve) {
        DeletionIdentifiers.validated(identifiers, label);

        return Uni.createFrom()
                .deferred(() -> AsyncDeletions.deleteAll(identifiers, label, resolve, this::delete,
                        deleteConcurrency()));
    }

    private Uni<String> resolveByName(String ruleName) {
        return findByName(ruleName)
                .map(rule -> (rule == null) ? null : rule.getId());
    }

    private Uni<?> delete(String id) {
        return Uni.createFrom()
                .completionStage(() -> this.evaluationRulesApi.evaluationRulesDelete(
                        APIEvaluationRulesDeleteRequest.newBuilder()
                                .evaluationRuleId(id)
                                .build()));
    }

    // cursor.value().orElse(null): the request's "cursor" query parameter is what starts the walk over
    // (absent for the first request) - see Cursor and DefaultEvaluationRuleOperations.fetch for the
    // synchronous mirror of this same call.
    private static Uni<CursorResult<EvaluationRule>> fetch(EvaluationRulesApi evaluationRulesApi, Cursor cursor) {
        return Uni.createFrom()
                .completionStage(() -> evaluationRulesApi.evaluationRulesList(APIEvaluationRulesListRequest.newBuilder()
                        .limit(cursor.limit())
                        .cursor(cursor.value().orElse(null))
                        .build()))
                .map(response -> CursorResults.from(cursor, response.getData(), response.getMeta()));
    }
}
