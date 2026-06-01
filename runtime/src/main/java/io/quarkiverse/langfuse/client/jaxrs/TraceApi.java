package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.model.DeleteTraceResponse;
import com.langfuse.api.model.TraceWithFullDetails;
import com.langfuse.api.model.Traces;
import com.langfuse.api.trace.TraceApi.APITraceDeleteMultipleRequest;
import com.langfuse.api.trace.TraceApi.APITraceDeleteRequest;
import com.langfuse.api.trace.TraceApi.APITraceGetRequest;
import com.langfuse.api.trace.TraceApi.APITraceListRequest;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Trace")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface TraceApi extends com.langfuse.api.trace.TraceApi {

    /**
     * Delete a specific trace
     *
     * @param apiRequest {@link APITraceDeleteRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "traceDelete", method = "DELETE", path = "/api/public/traces/{traceId}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/traces/{traceId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("trace_delete")
    @Override
    public DeleteTraceResponse traceDelete(
            APITraceDeleteRequest apiRequest);

    /**
     * Delete multiple traces
     *
     * @param apiRequest {@link APITraceDeleteMultipleRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "traceDeleteMultiple", method = "DELETE", path = "/api/public/traces")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/traces")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("trace_deleteMultiple")
    @Override
    public DeleteTraceResponse traceDeleteMultiple(
            APITraceDeleteMultipleRequest apiRequest);

    /**
     * Get a specific trace
     *
     * @param apiRequest {@link APITraceGetRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "traceGet", method = "GET", path = "/api/public/traces/{traceId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/traces/{traceId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("trace_get")
    @Override
    public TraceWithFullDetails traceGet(
            APITraceGetRequest apiRequest);

    /**
     * Get list of traces
     *
     * @param apiRequest {@link APITraceListRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "traceList", method = "GET", path = "/api/public/traces")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/traces")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("trace_list")
    @Override
    public Traces traceList(
            APITraceListRequest apiRequest);

}
