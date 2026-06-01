package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.opentelemetry.OpentelemetryApi.APIOpentelemetryExportTracesRequest;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Opentelemetry")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface OpentelemetryApi extends com.langfuse.api.opentelemetry.OpentelemetryApi {

    /**
     * **OpenTelemetry Traces Ingestion Endpoint** This endpoint implements the OTLP/HTTP specification for trace ingestion,
     * providing native OpenTelemetry integration for Langfuse Observability. **Supported Formats:** - Binary Protobuf:
     * `Content-Type: application/x-protobuf` - JSON Protobuf: `Content-Type: application/json` - Supports gzip compression via
     * `Content-Encoding: gzip` header **Specification Compliance:** - Conforms to [OTLP/HTTP Trace
     * Export](https://opentelemetry.io/docs/specs/otlp/#otlphttp) - Implements `ExportTraceServiceRequest` message format
     * **Documentation:** - Integration guide: https://langfuse.com/integrations/native/opentelemetry - Data model:
     * https://langfuse.com/docs/observability/data-model
     *
     * @param apiRequest {@link APIOpentelemetryExportTracesRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "opentelemetryExportTraces", method = "POST", path = "/api/public/otel/v1/traces")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/otel/v1/traces")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("opentelemetry_exportTraces")
    @Override
    public Object opentelemetryExportTraces(
            APIOpentelemetryExportTracesRequest apiRequest);

}
