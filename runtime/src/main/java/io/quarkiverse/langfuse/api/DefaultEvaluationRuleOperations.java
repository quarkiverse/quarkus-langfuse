package io.quarkiverse.langfuse.api;

import java.util.Optional;

import com.langfuse.api.evaluationRules.EvaluationRulesApi;
import com.langfuse.api.evaluationRules.EvaluationRulesApi.APIEvaluationRulesCreateRequest;
import com.langfuse.api.evaluationRules.EvaluationRulesApi.APIEvaluationRulesListRequest;
import com.langfuse.api.model.CreateEvaluationRuleRequest;
import com.langfuse.api.model.EvaluationRule;

import io.quarkiverse.langfuse.config.LangfuseConfig;

final class DefaultEvaluationRuleOperations extends AbstractCursorOperations<EvaluationRule>
        implements EvaluationRuleOperations {
    private final EvaluationRulesApi evaluationRulesApi;

    DefaultEvaluationRuleOperations(EvaluationRulesApi evaluationRulesApi, LangfuseConfig config) {
        super(cursor -> fetch(evaluationRulesApi, cursor), config);
        this.evaluationRulesApi = evaluationRulesApi;
    }

    @Override
    public Optional<EvaluationRule> findByName(String ruleName) {
        return scanForName(Names.require(ruleName, "Evaluation rule name"), EvaluationRule::getName);
    }

    @Override
    public EvaluationRule createIfAbsent(CreateEvaluationRuleRequest request) {
        return findByName(request.getName())
                .orElseGet(() -> this.evaluationRulesApi.evaluationRulesCreate(APIEvaluationRulesCreateRequest.newBuilder()
                        .createEvaluationRuleRequest(request)
                        .build()));
    }

    // cursor.value().orElse(null): the "cursor" query parameter is absent for the first request (the
    // start of the collection) and set to whatever the previous response reported otherwise - see
    // Cursor and CursorResults.from, which is where that value is read back on the way in.
    private static CursorResult<EvaluationRule> fetch(EvaluationRulesApi evaluationRulesApi, Cursor cursor) {
        var response = evaluationRulesApi.evaluationRulesList(APIEvaluationRulesListRequest.newBuilder()
                .limit(cursor.limit())
                .cursor(cursor.value().orElse(null))
                .build());

        return CursorResults.from(cursor, response.getData(), response.getMeta());
    }
}
