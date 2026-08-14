package io.quarkiverse.langfuse.client.jaxrs;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.model.GetScoresV3Response;
import com.langfuse.api.scoresV3.ScoresV3Api.APIScoresV3GetManyV3Request;

public interface QuarkusScoresV3AsyncApi extends com.langfuse.api.scoresV3.async.ScoresV3Api {

    /**
     * Get a list of scores (v3).
     */
    @GET
    @Path("/api/public/v3/scores")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<GetScoresV3Response> scoresV3GetManyV3(
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
     * Get a list of scores (v3).
     */
    @Override
    default CompletionStage<GetScoresV3Response> scoresV3GetManyV3(APIScoresV3GetManyV3Request apiRequest) {
        return scoresV3GetManyV3(apiRequest.limit(), apiRequest.cursor(), apiRequest.fields(), apiRequest.id(),
                apiRequest.name(), apiRequest.source(), apiRequest.dataType(), apiRequest.environment(),
                apiRequest.configId(), apiRequest.queueId(), apiRequest.authorUserId(), apiRequest.value(),
                apiRequest.valueMin(), apiRequest.valueMax(), apiRequest.traceId(), apiRequest.sessionId(),
                apiRequest.observationId(), apiRequest.experimentId(), apiRequest.fromTimestamp(),
                apiRequest.toTimestamp());
    }

}
