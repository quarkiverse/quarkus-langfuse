package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.model.Model;
import com.langfuse.api.model.PaginatedModels;
import com.langfuse.api.models.ModelsApi.APIModelsCreateRequest;
import com.langfuse.api.models.ModelsApi.APIModelsDeleteRequest;
import com.langfuse.api.models.ModelsApi.APIModelsGetRequest;
import com.langfuse.api.models.ModelsApi.APIModelsListRequest;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Models")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface ModelsAsyncApi extends com.langfuse.api.models.async.ModelsApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "modelsCreate", method = "POST", path = "/api/public/models")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/models")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("models_create")
    @Override
    public CompletionStage<Model> modelsCreate(
            APIModelsCreateRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "modelsDelete", method = "DELETE", path = "/api/public/models/{id}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/models/{id}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("models_delete")
    @Override
    public CompletionStage<Void> modelsDelete(
            APIModelsDeleteRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "modelsGet", method = "GET", path = "/api/public/models/{id}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/models/{id}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("models_get")
    @Override
    public CompletionStage<Model> modelsGet(
            APIModelsGetRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "modelsList", method = "GET", path = "/api/public/models")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/models")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("models_list")
    @Override
    public CompletionStage<PaginatedModels> modelsList(
            APIModelsListRequest apiRequest);

}
