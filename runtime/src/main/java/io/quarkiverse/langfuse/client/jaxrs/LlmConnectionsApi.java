package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsDeleteRequest;
import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsListRequest;
import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsUpsertRequest;
import com.langfuse.api.model.DeleteLlmConnectionResponse;
import com.langfuse.api.model.LlmConnection;
import com.langfuse.api.model.PaginatedLlmConnections;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "LlmConnections")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface LlmConnectionsApi extends com.langfuse.api.llmConnections.LlmConnectionsApi {

    /**
     * Delete an LLM connection by id. Evaluators that depend on the deleted connection are automatically paused.
     *
     * @param apiRequest {@link APILlmConnectionsDeleteRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "llmConnectionsDelete", method = "DELETE", path = "/api/public/llm-connections/{id}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/llm-connections/{id}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("llmConnections_delete")
    @Override
    public DeleteLlmConnectionResponse llmConnectionsDelete(
            APILlmConnectionsDeleteRequest apiRequest);

    /**
     * Get all LLM connections in a project
     *
     * @param apiRequest {@link APILlmConnectionsListRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "llmConnectionsList", method = "GET", path = "/api/public/llm-connections")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/llm-connections")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("llmConnections_list")
    @Override
    public PaginatedLlmConnections llmConnectionsList(
            APILlmConnectionsListRequest apiRequest);

    /**
     * Create or update an LLM connection. The connection is upserted on provider.
     *
     * @param apiRequest {@link APILlmConnectionsUpsertRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "llmConnectionsUpsert", method = "PUT", path = "/api/public/llm-connections")
    @jakarta.ws.rs.PUT
    @jakarta.ws.rs.Path("/api/public/llm-connections")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("llmConnections_upsert")
    @Override
    public LlmConnection llmConnectionsUpsert(
            APILlmConnectionsUpsertRequest apiRequest);

}
