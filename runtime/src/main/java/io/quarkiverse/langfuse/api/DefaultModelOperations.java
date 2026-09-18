package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.Optional;

import com.langfuse.api.model.CreateModelRequest;
import com.langfuse.api.model.Model;
import com.langfuse.api.models.ModelsApi;
import com.langfuse.api.models.ModelsApi.APIModelsCreateRequest;
import com.langfuse.api.models.ModelsApi.APIModelsDeleteRequest;
import com.langfuse.api.models.ModelsApi.APIModelsGetRequest;
import com.langfuse.api.models.ModelsApi.APIModelsListRequest;

import io.quarkiverse.langfuse.api.deletion.DeletionResult;
import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;

final class DefaultModelOperations extends AbstractPagedOperations<Model> implements ModelOperations {
    private final ModelsApi modelsApi;

    DefaultModelOperations(ModelsApi modelsApi, LangfuseConfig config) {
        super(page -> fetch(modelsApi, page), config);
        this.modelsApi = modelsApi;
    }

    @Override
    public Optional<Model> findById(String id) {
        var modelId = ValidationUtils.ensureNotBlank(id, "Model id");

        // Direct GET, so no scan: the server resolves the id. Only LangfuseNotFoundException is caught -
        // catching LangfuseApiException would report a 401 or a 500 as "absent", which is the one
        // mistake this layer must never make.
        try {
            return Optional.of(this.modelsApi.modelsGet(APIModelsGetRequest.newBuilder()
                    .id(modelId)
                    .build()));
        } catch (LangfuseNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Model> findByName(String modelName) {
        return scanForName(ValidationUtils.ensureNotBlank(modelName, "Model name"), Model::getModelName);
    }

    @Override
    public Model createIfAbsent(CreateModelRequest request) {
        return findByName(request.getModelName())
                .orElseGet(() -> this.modelsApi.modelsCreate(APIModelsCreateRequest.newBuilder()
                        .createModelRequest(request)
                        .build()));
    }

    @Override
    public DeletionResult deleteById(Collection<String> ids) {
        return Deletions.deleteAll(ids, "Model id", Optional::of, this::delete, deleteConcurrency());
    }

    @Override
    public DeletionResult deleteByName(Collection<String> modelNames) {
        return Deletions.deleteAll(modelNames, "Model name", this::resolveByName, this::delete, deleteConcurrency());
    }

    private Optional<String> resolveByName(String modelName) {
        return findByName(modelName)
                .map(Model::getId);
    }

    private void delete(String id) {
        this.modelsApi.modelsDelete(APIModelsDeleteRequest.newBuilder()
                .id(id)
                .build());
    }

    private static PagedResult<Model> fetch(ModelsApi modelsApi, Page page) {
        var response = modelsApi.modelsList(APIModelsListRequest.newBuilder()
                .page(page.index())
                .limit(page.size())
                .build());

        return PagedResults.from(page, response.getData(), response.getMeta());
    }
}
