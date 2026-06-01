package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsDeleteRequest;
import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsListRequest;
import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsUpsertRequest;
import com.langfuse.api.model.DeleteLlmConnectionResponse;
import com.langfuse.api.model.LlmConnection;
import com.langfuse.api.model.PaginatedLlmConnections;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "LlmConnections")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface LlmConnectionsAsyncApi extends com.langfuse.api.llmConnections.async.LlmConnectionsApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "llmConnectionsDelete", method = "DELETE", path = "/api/public/llm-connections/{id}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/llm-connections/{id}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("llmConnections_delete")
    @Override
    public CompletionStage<DeleteLlmConnectionResponse> llmConnectionsDelete(
            APILlmConnectionsDeleteRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "llmConnectionsList", method = "GET", path = "/api/public/llm-connections")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/llm-connections")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("llmConnections_list")
    @Override
    public CompletionStage<PaginatedLlmConnections> llmConnectionsList(
            APILlmConnectionsListRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "llmConnectionsUpsert", method = "PUT", path = "/api/public/llm-connections")
    @jakarta.ws.rs.PUT
    @jakarta.ws.rs.Path("/api/public/llm-connections")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("llmConnections_upsert")
    @Override
    public CompletionStage<LlmConnection> llmConnectionsUpsert(
            APILlmConnectionsUpsertRequest apiRequest);

}
