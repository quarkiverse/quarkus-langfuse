package io.quarkiverse.langfuse.client.jaxrs;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.model.CreateScoreRequest;
import com.langfuse.api.model.CreateScoresResponse;
import com.langfuse.api.model.GetScoresV3Response;

/**
 * Langfuse Scores API
 */
public interface QuarkusScoresApi extends com.langfuse.api.scores.ScoresApi {

    /**
     * Create scores asynchronously from a list of scores.
     */
    @POST
    @Path("/api/public/scores")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CreateScoresResponse scoresCreate(
            List<CreateScoreRequest> createScoreRequest);

    /**
     * Create scores asynchronously from a list of scores.
     */
    @Override
    default CreateScoresResponse scoresCreate(APIScoresCreateRequest apiRequest) {
        return scoresCreate(apiRequest.createScoreRequest());
    }

    /**
     * Delete a score (supports both trace and session scores)
     */
    @DELETE
    @Path("/api/public/scores/{scoreId}")
    @Produces(MediaType.APPLICATION_JSON)
    void scoresDelete(
            @PathParam("scoreId") String scoreId);

    /**
     * Delete a score (supports both trace and session scores)
     */
    @Override
    default void scoresDelete(APIScoresDeleteRequest apiRequest) {
        scoresDelete(apiRequest.scoreId());
    }

    /**
     * Get a list of scores with a polymorphic {@code value} field (v3).
     */
    @GET
    @Path("/api/public/v3/scores")
    @Produces(MediaType.APPLICATION_JSON)
    GetScoresV3Response scoresGetMany(
            @QueryParam("limit") Integer limit,
            @QueryParam("cursor") String cursor,
            @QueryParam("fields") String fields,
            @QueryParam("id") String id,
            @QueryParam("name") String name,
            @QueryParam("source") String source,
            @QueryParam("dataType") String dataType,
            @QueryParam("environment") String environment,
            @QueryParam("configId") String configId,
            @QueryParam("queueId") String queueId,
            @QueryParam("authorUserId") String authorUserId,
            @QueryParam("value") String value,
            @QueryParam("valueMin") Double valueMin,
            @QueryParam("valueMax") Double valueMax,
            @QueryParam("traceId") String traceId,
            @QueryParam("sessionId") String sessionId,
            @QueryParam("observationId") String observationId,
            @QueryParam("experimentId") String experimentId,
            @QueryParam("fromTimestamp") OffsetDateTime fromTimestamp,
            @QueryParam("toTimestamp") OffsetDateTime toTimestamp);

    /**
     * Get a list of scores with a polymorphic {@code value} field (v3).
     */
    @Override
    default GetScoresV3Response scoresGetMany(APIScoresGetManyRequest apiRequest) {
        return scoresGetMany(apiRequest.limit(), apiRequest.cursor(), apiRequest.fields(), apiRequest.id(),
                apiRequest.name(), apiRequest.source(), apiRequest.dataType(), apiRequest.environment(),
                apiRequest.configId(), apiRequest.queueId(), apiRequest.authorUserId(), apiRequest.value(),
                apiRequest.valueMin(), apiRequest.valueMax(), apiRequest.traceId(), apiRequest.sessionId(),
                apiRequest.observationId(), apiRequest.experimentId(), apiRequest.fromTimestamp(),
                apiRequest.toTimestamp());
    }

}
