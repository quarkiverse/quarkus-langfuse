package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.model.GetScoresResponse;
import com.langfuse.api.model.Score;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Scores")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface ScoresApi extends com.langfuse.api.scores.ScoresApi {

    /**
     * Get a score (supports both trace and session scores)
     *
     * @param apiRequest {@link APIScoresGetByIdRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scoresGetById", method = "GET", path = "/api/public/v2/scores/{scoreId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/v2/scores/{scoreId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scores_get-by-id")
    @Override
    public Score scoresGetById(
            APIScoresGetByIdRequest apiRequest);

    /**
     * Get a list of scores (supports both trace and session scores)
     *
     * @param apiRequest {@link APIScoresGetManyRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scoresGetMany", method = "GET", path = "/api/public/v2/scores")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/v2/scores")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scores_get-many")
    @Override
    public GetScoresResponse scoresGetMany(
            APIScoresGetManyRequest apiRequest);

}
