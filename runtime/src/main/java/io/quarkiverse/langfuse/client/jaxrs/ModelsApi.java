package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.model.Model;
import com.langfuse.api.model.PaginatedModels;
import com.langfuse.api.models.ModelsApi.APIModelsCreateRequest;
import com.langfuse.api.models.ModelsApi.APIModelsDeleteRequest;
import com.langfuse.api.models.ModelsApi.APIModelsGetRequest;
import com.langfuse.api.models.ModelsApi.APIModelsListRequest;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Models")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface ModelsApi extends com.langfuse.api.models.ModelsApi {

    /**
     * Create a model
     *
     * @param apiRequest {@link APIModelsCreateRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "modelsCreate", method = "POST", path = "/api/public/models")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/models")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("models_create")
    @Override
    public Model modelsCreate(
            APIModelsCreateRequest apiRequest);

    /**
     * Delete a model.
     *
     * @param apiRequest {@link APIModelsDeleteRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "modelsDelete", method = "DELETE", path = "/api/public/models/{id}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/models/{id}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("models_delete")
    @Override
    public void modelsDelete(
            APIModelsDeleteRequest apiRequest);

    /**
     * Get a model
     *
     * @param apiRequest {@link APIModelsGetRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "modelsGet", method = "GET", path = "/api/public/models/{id}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/models/{id}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("models_get")
    @Override
    public Model modelsGet(
            APIModelsGetRequest apiRequest);

    /**
     * Get all models
     *
     * @param apiRequest {@link APIModelsListRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "modelsList", method = "GET", path = "/api/public/models")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/models")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("models_list")
    @Override
    public PaginatedModels modelsList(
            APIModelsListRequest apiRequest);

}
