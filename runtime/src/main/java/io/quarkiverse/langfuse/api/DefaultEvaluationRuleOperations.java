package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.Optional;

import com.langfuse.api.evaluationRules.EvaluationRulesApi;
import com.langfuse.api.evaluationRules.EvaluationRulesApi.APIEvaluationRulesCreateRequest;
import com.langfuse.api.evaluationRules.EvaluationRulesApi.APIEvaluationRulesDeleteRequest;
import com.langfuse.api.evaluationRules.EvaluationRulesApi.APIEvaluationRulesListRequest;
import com.langfuse.api.model.CreateEvaluationRuleRequest;
import com.langfuse.api.model.EvaluationRule;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;

final class DefaultEvaluationRuleOperations extends AbstractCursorOperations<EvaluationRule>
        implements EvaluationRuleOperations {
    private final EvaluationRulesApi evaluationRulesApi;

    DefaultEvaluationRuleOperations(EvaluationRulesApi evaluationRulesApi, LangfuseConfig config) {
        super(cursor -> fetch(evaluationRulesApi, cursor), config);
        this.evaluationRulesApi = evaluationRulesApi;
    }

    @Override
    public Optional<EvaluationRule> findByName(String ruleName) {
        return scanForName(ValidationUtils.ensureNotBlank(ruleName, "Evaluation rule name"), EvaluationRule::getName);
    }

    @Override
    public EvaluationRule createIfAbsent(CreateEvaluationRuleRequest request) {
        return findByName(request.getName())
                .orElseGet(() -> this.evaluationRulesApi.evaluationRulesCreate(APIEvaluationRulesCreateRequest.newBuilder()
                        .createEvaluationRuleRequest(request)
                        .build()));
    }

    @Override
    public DeletionResult deleteById(Collection<String> ids) {
        return Deletions.deleteAll(ids, "Evaluation rule id", Optional::of, this::delete, deleteConcurrency());
    }

    @Override
    public DeletionResult deleteByName(Collection<String> ruleNames) {
        return Deletions.deleteAll(ruleNames, "Evaluation rule name", this::resolveByName, this::delete,
                deleteConcurrency());
    }

    private Optional<String> resolveByName(String ruleName) {
        return findByName(ruleName)
                .map(EvaluationRule::getId);
    }

    private void delete(String id) {
        this.evaluationRulesApi.evaluationRulesDelete(APIEvaluationRulesDeleteRequest.newBuilder()
                .evaluationRuleId(id)
                .build());
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
