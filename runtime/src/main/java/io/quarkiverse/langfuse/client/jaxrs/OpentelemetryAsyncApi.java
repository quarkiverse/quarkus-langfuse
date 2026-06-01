package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.opentelemetry.OpentelemetryApi.APIOpentelemetryExportTracesRequest;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Opentelemetry")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface OpentelemetryAsyncApi extends com.langfuse.api.opentelemetry.async.OpentelemetryApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "opentelemetryExportTraces", method = "POST", path = "/api/public/otel/v1/traces")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/otel/v1/traces")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("opentelemetry_exportTraces")
    @Override
    public CompletionStage<Object> opentelemetryExportTraces(
            APIOpentelemetryExportTracesRequest apiRequest);

}
