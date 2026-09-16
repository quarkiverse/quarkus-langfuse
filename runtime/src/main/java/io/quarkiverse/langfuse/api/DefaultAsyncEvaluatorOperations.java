package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.function.Function;

import com.langfuse.api.evaluators.EvaluatorsApi.APIEvaluatorsCreateRequest;
import com.langfuse.api.evaluators.EvaluatorsApi.APIEvaluatorsDeleteRequest;
import com.langfuse.api.evaluators.EvaluatorsApi.APIEvaluatorsListRequest;
import com.langfuse.api.evaluators.async.EvaluatorsApi;
import com.langfuse.api.model.CreateEvaluatorRequest;
import com.langfuse.api.model.Evaluator;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;
import io.smallrye.mutiny.Uni;

final class DefaultAsyncEvaluatorOperations extends AbstractAsyncCursorOperations<Evaluator>
        implements AsyncEvaluatorOperations {
    private final EvaluatorsApi evaluatorsApi;

    DefaultAsyncEvaluatorOperations(EvaluatorsApi evaluatorsApi, LangfuseConfig config) {
        super(cursor -> fetch(evaluatorsApi, cursor), config);
        this.evaluatorsApi = evaluatorsApi;
    }

    @Override
    public Uni<Evaluator> findByName(String evaluatorName) {
        return scanForName(ValidationUtils.ensureNotBlank(evaluatorName, "Evaluator name"), EvaluatorNames::of);
    }

    @Override
    public Uni<Evaluator> createIfAbsent(CreateEvaluatorRequest request) {
        var name = ValidationUtils.ensureNotBlank(EvaluatorNames.of(request), "Evaluator name");
        return findByName(name)
                .flatMap(existing -> (existing != null)
                        ? Uni.createFrom().item(existing)
                        : Uni.createFrom().completionStage(() -> this.evaluatorsApi.evaluatorsCreate(
                                APIEvaluatorsCreateRequest.newBuilder()
                                        .createEvaluatorRequest(request)
                                        .build())));
    }

    @Override
    public Uni<DeletionResult> deleteById(Collection<String> ids) {
        return deferDeleteAll(ids, "Evaluator id", Uni.createFrom()::item);
    }

    @Override
    public Uni<DeletionResult> deleteByName(Collection<String> evaluatorNames) {
        return deferDeleteAll(evaluatorNames, "Evaluator name", this::resolveByName);
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

    // Emits null for absent, which the engine reads as NotFound. That covers both an evaluator no scan
    // matched and an Evaluator subtype this layer does not recognize, whose id cannot be extracted -
    // the latter would otherwise reach the delete with a null path parameter.
    private Uni<String> resolveByName(String evaluatorName) {
        return findByName(evaluatorName)
                .map(evaluator -> (evaluator == null) ? null : EvaluatorIds.of(evaluator));
    }

    private Uni<?> delete(String id) {
        return Uni.createFrom()
                .completionStage(() -> this.evaluatorsApi.evaluatorsDelete(APIEvaluatorsDeleteRequest.newBuilder()
                        .evaluatorId(id)
                        .build()));
    }

    private static Uni<CursorResult<Evaluator>> fetch(EvaluatorsApi evaluatorsApi, Cursor cursor) {
        return Uni.createFrom()
                .completionStage(() -> evaluatorsApi.evaluatorsList(APIEvaluatorsListRequest.newBuilder()
                        .limit(cursor.limit())
                        .cursor(cursor.value().orElse(null))
                        .build()))
                .map(response -> CursorResults.from(cursor, response.getData(), response.getMeta()));
    }
}
