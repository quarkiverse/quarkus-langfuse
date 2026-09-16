package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.function.Function;

import com.langfuse.api.model.CreateModelRequest;
import com.langfuse.api.model.Model;
import com.langfuse.api.models.ModelsApi.APIModelsCreateRequest;
import com.langfuse.api.models.ModelsApi.APIModelsDeleteRequest;
import com.langfuse.api.models.ModelsApi.APIModelsListRequest;
import com.langfuse.api.models.async.ModelsApi;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;
import io.smallrye.mutiny.Uni;

final class DefaultAsyncModelOperations extends AbstractAsyncPagedOperations<Model> implements AsyncModelOperations {
    private final ModelsApi modelsApi;

    DefaultAsyncModelOperations(ModelsApi modelsApi, LangfuseConfig config) {
        super(page -> fetch(modelsApi, page), config);
        this.modelsApi = modelsApi;
    }

    @Override
    public Uni<Model> findByName(String modelName) {
        return scanForName(ValidationUtils.ensureNotBlank(modelName, "Model name"), Model::getModelName);
    }

    @Override
    public Uni<Model> createIfAbsent(CreateModelRequest request) {
        // flatMap + ternary rather than onItem().ifNull().switchTo(): both express "if absent, create",
        // but the former is the plain shorthand and this repo prefers it wherever one exists. Not
        // atomic - see the Javadoc on AsyncModelOperations.createIfAbsent - since it is a lookup followed
        // by a create rather than a single request the way LlmConnectionOperations.upsert is.
        return findByName(request.getModelName())
                .flatMap(existing -> (existing != null)
                        ? Uni.createFrom().item(existing)
                        : Uni.createFrom().completionStage(() -> this.modelsApi.modelsCreate(APIModelsCreateRequest.newBuilder()
                                .createModelRequest(request)
                                .build())));
    }

    @Override
    public Uni<DeletionResult> deleteById(Collection<String> ids) {
        return deferDeleteAll(ids, "Model id", Uni.createFrom()::item);
    }

    @Override
    public Uni<DeletionResult> deleteByName(Collection<String> modelNames) {
        return deferDeleteAll(modelNames, "Model name", this::resolveByName);
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

    private Uni<String> resolveByName(String modelName) {
        return findByName(modelName)
                .map(model -> (model == null) ? null : model.getId());
    }

    private Uni<?> delete(String id) {
        return Uni.createFrom()
                .completionStage(() -> this.modelsApi.modelsDelete(APIModelsDeleteRequest.newBuilder()
                        .id(id)
                        .build()));
    }

    private static Uni<PagedResult<Model>> fetch(ModelsApi modelsApi, Page page) {
        return Uni.createFrom()
                .completionStage(() -> modelsApi.modelsList(APIModelsListRequest.newBuilder()
                        .page(page.index())
                        .limit(page.size())
                        .build()))
                .map(response -> PagedResults.from(page, response.getData(), response.getMeta()));
    }
}
