package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.Optional;

import com.langfuse.api.evaluators.EvaluatorsApi;
import com.langfuse.api.evaluators.EvaluatorsApi.APIEvaluatorsCreateRequest;
import com.langfuse.api.evaluators.EvaluatorsApi.APIEvaluatorsDeleteRequest;
import com.langfuse.api.evaluators.EvaluatorsApi.APIEvaluatorsListRequest;
import com.langfuse.api.model.CreateEvaluatorRequest;
import com.langfuse.api.model.Evaluator;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;

final class DefaultEvaluatorOperations extends AbstractCursorOperations<Evaluator>
        implements EvaluatorOperations {
    private final EvaluatorsApi evaluatorsApi;

    DefaultEvaluatorOperations(EvaluatorsApi evaluatorsApi, LangfuseConfig config) {
        super(cursor -> fetch(evaluatorsApi, cursor), config);
        this.evaluatorsApi = evaluatorsApi;
    }

    @Override
    public Optional<Evaluator> findByName(String evaluatorName) {
        return scanForName(ValidationUtils.ensureNotBlank(evaluatorName, "Evaluator name"), EvaluatorNames::of);
    }

    @Override
    public Evaluator createIfAbsent(CreateEvaluatorRequest request) {
        var name = ValidationUtils.ensureNotBlank(EvaluatorNames.of(request), "Evaluator name");
        return findByName(name)
                .orElseGet(() -> this.evaluatorsApi.evaluatorsCreate(APIEvaluatorsCreateRequest.newBuilder()
                        .createEvaluatorRequest(request)
                        .build()));
    }

    @Override
    public DeletionResult deleteById(Collection<String> ids) {
        return Deletions.deleteAll(ids, "Evaluator id", Optional::of, this::delete, deleteConcurrency());
    }

    @Override
    public DeletionResult deleteByName(Collection<String> evaluatorNames) {
        return Deletions.deleteAll(evaluatorNames, "Evaluator name", this::resolveByName, this::delete,
                deleteConcurrency());
    }

    // An Evaluator is a oneOf, so a subtype this layer does not recognize yields no id. Resolving to
    // empty turns that into a NotFound outcome instead of a delete request with a null path parameter.
    private Optional<String> resolveByName(String evaluatorName) {
        return findByName(evaluatorName)
                .map(EvaluatorIds::of);
    }

    private void delete(String id) {
        this.evaluatorsApi.evaluatorsDelete(APIEvaluatorsDeleteRequest.newBuilder()
                .evaluatorId(id)
                .build());
    }

    private static CursorResult<Evaluator> fetch(EvaluatorsApi evaluatorsApi, Cursor cursor) {
        var response = evaluatorsApi.evaluatorsList(APIEvaluatorsListRequest.newBuilder()
                .limit(cursor.limit())
                .cursor(cursor.value().orElse(null))
                .build());

        return CursorResults.from(cursor, response.getData(), response.getMeta());
    }
}
