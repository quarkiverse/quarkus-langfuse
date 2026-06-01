package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.model.DeleteTraceResponse;
import com.langfuse.api.model.TraceWithFullDetails;
import com.langfuse.api.model.Traces;
import com.langfuse.api.trace.TraceApi.APITraceDeleteMultipleRequest;
import com.langfuse.api.trace.TraceApi.APITraceDeleteRequest;
import com.langfuse.api.trace.TraceApi.APITraceGetRequest;
import com.langfuse.api.trace.TraceApi.APITraceListRequest;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Trace")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface TraceAsyncApi extends com.langfuse.api.trace.async.TraceApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "traceDelete", method = "DELETE", path = "/api/public/traces/{traceId}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/traces/{traceId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("trace_delete")
    @Override
    public CompletionStage<DeleteTraceResponse> traceDelete(
            APITraceDeleteRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "traceDeleteMultiple", method = "DELETE", path = "/api/public/traces")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/traces")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("trace_deleteMultiple")
    @Override
    public CompletionStage<DeleteTraceResponse> traceDeleteMultiple(
            APITraceDeleteMultipleRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "traceGet", method = "GET", path = "/api/public/traces/{traceId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/traces/{traceId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("trace_get")
    @Override
    public CompletionStage<TraceWithFullDetails> traceGet(
            APITraceGetRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "traceList", method = "GET", path = "/api/public/traces")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/traces")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("trace_list")
    @Override
    public CompletionStage<Traces> traceList(
            APITraceListRequest apiRequest);

}
