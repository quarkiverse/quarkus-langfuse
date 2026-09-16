package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.Optional;

import com.langfuse.api.model.CreateModelRequest;
import com.langfuse.api.model.Model;
import com.langfuse.api.models.ModelsApi;
import com.langfuse.api.models.ModelsApi.APIModelsCreateRequest;
import com.langfuse.api.models.ModelsApi.APIModelsDeleteRequest;
import com.langfuse.api.models.ModelsApi.APIModelsListRequest;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;

final class DefaultModelOperations extends AbstractPagedOperations<Model> implements ModelOperations {
    private final ModelsApi modelsApi;

    DefaultModelOperations(ModelsApi modelsApi, LangfuseConfig config) {
        super(page -> fetch(modelsApi, page), config);
        this.modelsApi = modelsApi;
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
