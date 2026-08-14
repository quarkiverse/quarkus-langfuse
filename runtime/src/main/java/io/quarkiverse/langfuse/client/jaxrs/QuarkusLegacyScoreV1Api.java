package io.quarkiverse.langfuse.client.jaxrs;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Langfuse LegacyScoreV1 API
 */
public interface QuarkusLegacyScoreV1Api extends com.langfuse.api.legacyScoreV1.LegacyScoreV1Api {

    /**
     * Delete a score (supports both trace and session scores)
     */
    @DELETE
    @Path("/api/public/scores/{scoreId}")
    @Produces(MediaType.APPLICATION_JSON)
    void legacyScoreV1Delete(
            @PathParam("scoreId") String scoreId);

    /**
     * Delete a score (supports both trace and session scores)
     */
    @Override
    default void legacyScoreV1Delete(APILegacyScoreV1DeleteRequest apiRequest) {
        legacyScoreV1Delete(apiRequest.scoreId());
    }

}
