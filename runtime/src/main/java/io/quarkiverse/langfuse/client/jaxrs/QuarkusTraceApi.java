package io.quarkiverse.langfuse.client.jaxrs;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.model.DeleteTraceResponse;
import com.langfuse.api.model.TraceDeleteMultipleRequest;
import com.langfuse.api.trace.TraceApi.APITraceDeleteMultipleRequest;
import com.langfuse.api.trace.TraceApi.APITraceDeleteRequest;

/**
 * Langfuse Trace API
 */
public interface QuarkusTraceApi extends com.langfuse.api.trace.TraceApi {

    /**
     * Delete a specific trace
     */
    @DELETE
    @Path("/api/public/traces/{traceId}")
    @Produces(MediaType.APPLICATION_JSON)
    DeleteTraceResponse traceDelete(
            @PathParam("traceId") String traceId);

    /**
     * Delete a specific trace
     */
    @Override
    default DeleteTraceResponse traceDelete(APITraceDeleteRequest apiRequest) {
        return traceDelete(apiRequest.traceId());
    }

    /**
     * Delete multiple traces
     */
    @DELETE
    @Path("/api/public/traces")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    DeleteTraceResponse traceDeleteMultiple(
            TraceDeleteMultipleRequest traceDeleteMultipleRequest);

    /**
     * Delete multiple traces
     */
    @Override
    default DeleteTraceResponse traceDeleteMultiple(APITraceDeleteMultipleRequest apiRequest) {
        return traceDeleteMultiple(apiRequest.traceDeleteMultipleRequest());
    }

}
