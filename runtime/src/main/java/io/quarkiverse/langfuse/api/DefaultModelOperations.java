package io.quarkiverse.langfuse.api;

import java.util.Optional;

import com.langfuse.api.model.CreateModelRequest;
import com.langfuse.api.model.Model;
import com.langfuse.api.models.ModelsApi;
import com.langfuse.api.models.ModelsApi.APIModelsCreateRequest;
import com.langfuse.api.models.ModelsApi.APIModelsListRequest;

import io.quarkiverse.langfuse.config.LangfuseConfig;

final class DefaultModelOperations extends AbstractPagedOperations<Model> implements ModelOperations {
    private final ModelsApi modelsApi;

    DefaultModelOperations(ModelsApi modelsApi, LangfuseConfig config) {
        super(page -> fetch(modelsApi, page), config);
        this.modelsApi = modelsApi;
    }

    @Override
    public Optional<Model> findByName(String modelName) {
        return scanForName(Names.require(modelName, "Model name"), Model::getModelName);
    }

    @Override
    public Model createIfAbsent(CreateModelRequest request) {
        return findByName(request.getModelName())
                .orElseGet(() -> this.modelsApi.modelsCreate(APIModelsCreateRequest.newBuilder()
                        .createModelRequest(request)
                        .build()));
    }

    private static PagedResult<Model> fetch(ModelsApi modelsApi, Page page) {
        var response = modelsApi.modelsList(APIModelsListRequest.newBuilder()
                .page(page.index())
                .limit(page.size())
                .build());

        return PagedResults.from(page, response.getData(), response.getMeta());
    }
}
