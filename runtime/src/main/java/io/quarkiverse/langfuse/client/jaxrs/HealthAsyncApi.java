package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.model.HealthResponse;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Health")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface HealthAsyncApi extends com.langfuse.api.health.async.HealthApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "", openApiSpecId = "langfuse_yml", operationId = "healthHealth", method = "GET", path = "/api/public/health")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/health")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("health_health")
    @Override
    public CompletionStage<HealthResponse> healthHealth();

}