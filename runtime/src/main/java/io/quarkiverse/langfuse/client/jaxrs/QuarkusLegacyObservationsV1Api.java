package io.quarkiverse.langfuse.client.jaxrs;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.model.LegacyObservationsViews;
import com.langfuse.api.model.ObservationLevel;
import com.langfuse.api.model.ObservationsViewSingle;

/**
 * Langfuse LegacyObservationsV1 API
 */
public interface QuarkusLegacyObservationsV1Api extends com.langfuse.api.legacyObservationsV1.LegacyObservationsV1Api {

    /**
     * Get a observation
     */
    @GET
    @Path("/api/public/observations/{observationId}")
    @Produces(MediaType.APPLICATION_JSON)
    ObservationsViewSingle legacyObservationsV1Get(
            @PathParam("observationId") String observationId);

    /**
     * Get a observation
     */
    @Override
    default ObservationsViewSingle legacyObservationsV1Get(APILegacyObservationsV1GetRequest apiRequest) {
        return legacyObservationsV1Get(apiRequest.observationId());
    }

    /**
     * Get a list of observations.
     */
    @GET
    @Path("/api/public/observations")
    @Produces(MediaType.APPLICATION_JSON)
    LegacyObservationsViews legacyObservationsV1GetMany(
            @QueryParam("page") Integer page,
            @QueryParam("limit") Integer limit,
            @QueryParam("name") String name,
            @QueryParam("userId") String userId,
            @QueryParam("type") String type,
            @QueryParam("traceId") String traceId,
            @QueryParam("level") ObservationLevel level,
            @QueryParam("parentObservationId") String parentObservationId,
            @QueryParam("environment") List<String> environment,
            @QueryParam("fromStartTime") OffsetDateTime fromStartTime,
            @QueryParam("toStartTime") OffsetDateTime toStartTime,
            @QueryParam("version") String version,
            @QueryParam("filter") String filter);

    /**
     * Get a list of observations.
     */
    @Override
    default LegacyObservationsViews legacyObservationsV1GetMany(APILegacyObservationsV1GetManyRequest apiRequest) {
        return legacyObservationsV1GetMany(apiRequest.page(), apiRequest.limit(), apiRequest.name(),
                apiRequest.userId(), apiRequest.type(), apiRequest.traceId(), apiRequest.level(),
                apiRequest.parentObservationId(), apiRequest.environment(), apiRequest.fromStartTime(),
                apiRequest.toStartTime(), apiRequest.version(), apiRequest.filter());
    }

}
