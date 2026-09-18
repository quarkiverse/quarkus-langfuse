package io.quarkiverse.langfuse.client.jaxrs;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.model.ObservationLevel;
import com.langfuse.api.model.ObservationsV2Response;
import com.langfuse.api.observations.ObservationsApi.APIObservationsGetManyRequest;

/**
 * Langfuse Observations API
 */
public interface QuarkusObservationsApi extends com.langfuse.api.observations.ObservationsApi {

    /**
     * Get a list of observations with cursor-based pagination and flexible field selection.
     */
    @GET
    @Path("/api/public/v2/observations")
    @Produces(MediaType.APPLICATION_JSON)
    ObservationsV2Response observationsGetMany(
            @QueryParam("fields") String fields,
            @QueryParam("expandMetadata") String expandMetadata,
            @QueryParam("limit") Integer limit,
            @QueryParam("cursor") String cursor,
            @QueryParam("parseIoAsJson") Boolean parseIoAsJson,
            @QueryParam("name") String name,
            @QueryParam("userId") String userId,
            @QueryParam("sessionId") String sessionId,
            @QueryParam("type") String type,
            @QueryParam("traceId") String traceId,
            @QueryParam("level") ObservationLevel level,
            @QueryParam("parentObservationId") String parentObservationId,
            @QueryParam("isRootObservation") Boolean isRootObservation,
            @QueryParam("environment") List<String> environment,
            @QueryParam("fromStartTime") OffsetDateTime fromStartTime,
            @QueryParam("toStartTime") OffsetDateTime toStartTime,
            @QueryParam("version") String version,
            @QueryParam("filter") String filter);

    /**
     * Get a list of observations with cursor-based pagination and flexible field selection.
     */
    @Override
    default ObservationsV2Response observationsGetMany(APIObservationsGetManyRequest apiRequest) {
        return observationsGetMany(apiRequest.fields(), apiRequest.expandMetadata(), apiRequest.limit(),
                apiRequest.cursor(), apiRequest.parseIoAsJson(), apiRequest.name(), apiRequest.userId(),
                apiRequest.sessionId(), apiRequest.type(), apiRequest.traceId(), apiRequest.level(),
                apiRequest.parentObservationId(), apiRequest.isRootObservation(), apiRequest.environment(),
                apiRequest.fromStartTime(), apiRequest.toStartTime(), apiRequest.version(), apiRequest.filter());
    }

}
