package io.quarkiverse.langfuse.api;

import com.langfuse.api.model.CreateModelRequest;
import com.langfuse.api.model.Model;
import com.langfuse.api.models.ModelsApi.APIModelsCreateRequest;
import com.langfuse.api.models.ModelsApi.APIModelsListRequest;
import com.langfuse.api.models.async.ModelsApi;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.smallrye.mutiny.Uni;

final class DefaultAsyncModelOperations extends AbstractAsyncPagedOperations<Model> implements AsyncModelOperations {
    private final ModelsApi modelsApi;

    DefaultAsyncModelOperations(ModelsApi modelsApi, LangfuseConfig config) {
        super(page -> fetch(modelsApi, page), config);
        this.modelsApi = modelsApi;
    }

    @Override
    public Uni<Model> findByName(String modelName) {
        return scanForName(Names.require(modelName, "Model name"), Model::getModelName);
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

    private static Uni<PagedResult<Model>> fetch(ModelsApi modelsApi, Page page) {
        return Uni.createFrom()
                .completionStage(() -> modelsApi.modelsList(APIModelsListRequest.newBuilder()
                        .page(page.index())
                        .limit(page.size())
                        .build()))
                .map(response -> PagedResults.from(page, response.getData(), response.getMeta()));
    }
}
