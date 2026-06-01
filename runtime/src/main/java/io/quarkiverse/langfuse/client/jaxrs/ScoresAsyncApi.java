package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.model.GetScoresResponse;
import com.langfuse.api.model.Score;
import com.langfuse.api.scores.ScoresApi.APIScoresGetByIdRequest;
import com.langfuse.api.scores.ScoresApi.APIScoresGetManyRequest;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Scores")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface ScoresAsyncApi extends com.langfuse.api.scores.async.ScoresApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scoresGetById", method = "GET", path = "/api/public/v2/scores/{scoreId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/v2/scores/{scoreId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scores_get-by-id")
    @Override
    public CompletionStage<Score> scoresGetById(
            APIScoresGetByIdRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scoresGetMany", method = "GET", path = "/api/public/v2/scores")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/v2/scores")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scores_get-many")
    @Override
    public CompletionStage<GetScoresResponse> scoresGetMany(
            APIScoresGetManyRequest apiRequest);

}
