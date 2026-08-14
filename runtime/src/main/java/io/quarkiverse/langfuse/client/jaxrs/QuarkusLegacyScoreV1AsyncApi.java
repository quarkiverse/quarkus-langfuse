package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.legacyScoreV1.LegacyScoreV1Api.APILegacyScoreV1DeleteRequest;

/**
 * Langfuse LegacyScoreV1 Async API
 */
public interface QuarkusLegacyScoreV1AsyncApi extends com.langfuse.api.legacyScoreV1.async.LegacyScoreV1Api {

    /**
     * Delete a score (supports both trace and session scores)
     */
    @DELETE
    @Path("/api/public/scores/{scoreId}")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<Void> legacyScoreV1Delete(
            @PathParam("scoreId") String scoreId);

    /**
     * Delete a score (supports both trace and session scores)
     */
    @Override
    default CompletionStage<Void> legacyScoreV1Delete(APILegacyScoreV1DeleteRequest apiRequest) {
        return legacyScoreV1Delete(apiRequest.scoreId());
    }

}
