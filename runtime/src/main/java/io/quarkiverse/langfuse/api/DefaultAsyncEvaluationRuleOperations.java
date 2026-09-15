package io.quarkiverse.langfuse.api;

import com.langfuse.api.evaluationRules.EvaluationRulesApi.APIEvaluationRulesCreateRequest;
import com.langfuse.api.evaluationRules.EvaluationRulesApi.APIEvaluationRulesListRequest;
import com.langfuse.api.evaluationRules.async.EvaluationRulesApi;
import com.langfuse.api.model.CreateEvaluationRuleRequest;
import com.langfuse.api.model.EvaluationRule;

import io.quarkiverse.langfuse.config.LangfuseConfig;
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
        return scanForName(Names.require(ruleName, "Evaluation rule name"), EvaluationRule::getName);
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
