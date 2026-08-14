package io.quarkiverse.langfuse.client.jaxrs;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.model.CreateScoreRequest;
import com.langfuse.api.model.CreateScoreResponse;
import com.langfuse.api.model.GetScoresResponse;
import com.langfuse.api.model.Score;
import com.langfuse.api.model.ScoreDataType;
import com.langfuse.api.model.ScoreSource;

/**
 * Langfuse Scores API
 */
public interface QuarkusScoresApi extends com.langfuse.api.scores.ScoresApi {

    /**
     * Create a score (supports trace, observation, session, and dataset run scores)
     */
    @POST
    @Path("/api/public/scores")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CreateScoreResponse scoresCreate(
            CreateScoreRequest createScoreRequest);

    /**
     * Create a score (supports trace, observation, session, and dataset run scores)
     */
    @Override
    default CreateScoreResponse scoresCreate(APIScoresCreateRequest apiRequest) {
        return scoresCreate(apiRequest.createScoreRequest());
    }

    /**
     * Get a score (supports both trace and session scores)
     */
    @GET
    @Path("/api/public/v2/scores/{scoreId}")
    @Produces(MediaType.APPLICATION_JSON)
    Score scoresGetById(
            @PathParam("scoreId") String scoreId);

    /**
     * Get a score (supports both trace and session scores)
     */
    @Override
    default Score scoresGetById(APIScoresGetByIdRequest apiRequest) {
        return scoresGetById(apiRequest.scoreId());
    }

    /**
     * Get a list of scores (supports both trace and session scores)
     */
    @GET
    @Path("/api/public/v2/scores")
    @Produces(MediaType.APPLICATION_JSON)
    GetScoresResponse scoresGetMany(
            @QueryParam("page") Integer page,
            @QueryParam("limit") Integer limit,
            @QueryParam("userId") String userId,
            @QueryParam("name") String name,
            @QueryParam("fromTimestamp") OffsetDateTime fromTimestamp,
            @QueryParam("toTimestamp") OffsetDateTime toTimestamp,
            @QueryParam("environment") List<String> environment,
            @QueryParam("source") ScoreSource source,
            @QueryParam("operator") String operator,
            @QueryParam("value") Double value,
            @QueryParam("scoreIds") String scoreIds,
            @QueryParam("configId") String configId,
            @QueryParam("sessionId") String sessionId,
            @QueryParam("datasetRunId") String datasetRunId,
            @QueryParam("traceId") String traceId,
            @QueryParam("observationId") String observationId,
            @QueryParam("queueId") String queueId,
            @QueryParam("dataType") ScoreDataType dataType,
            @QueryParam("traceTags") List<String> traceTags,
            @QueryParam("fields") String fields,
            @QueryParam("filter") String filter);

    /**
     * Get a list of scores (supports both trace and session scores)
     */
    @Override
    default GetScoresResponse scoresGetMany(APIScoresGetManyRequest apiRequest) {
        return scoresGetMany(apiRequest.page(), apiRequest.limit(), apiRequest.userId(), apiRequest.name(),
                apiRequest.fromTimestamp(), apiRequest.toTimestamp(), apiRequest.environment(), apiRequest.source(),
                apiRequest.operator(), apiRequest.value(), apiRequest.scoreIds(), apiRequest.configId(),
                apiRequest.sessionId(), apiRequest.datasetRunId(), apiRequest.traceId(), apiRequest.observationId(),
                apiRequest.queueId(), apiRequest.dataType(), apiRequest.traceTags(), apiRequest.fields(),
                apiRequest.filter());
    }

}
