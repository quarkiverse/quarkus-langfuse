package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.model.ScoreConfig;
import com.langfuse.api.model.ScoreConfigs;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi.APIScoreConfigsCreateRequest;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi.APIScoreConfigsGetByIdRequest;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi.APIScoreConfigsGetRequest;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi.APIScoreConfigsUpdateRequest;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "ScoreConfigs")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface ScoreConfigsAsyncApi extends com.langfuse.api.scoreConfigs.async.ScoreConfigsApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scoreConfigsCreate", method = "POST", path = "/api/public/score-configs")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/score-configs")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scoreConfigs_create")
    @Override
    public CompletionStage<ScoreConfig> scoreConfigsCreate(
            APIScoreConfigsCreateRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scoreConfigsGet", method = "GET", path = "/api/public/score-configs")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/score-configs")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scoreConfigs_get")
    @Override
    public CompletionStage<ScoreConfigs> scoreConfigsGet(
            APIScoreConfigsGetRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scoreConfigsGetById", method = "GET", path = "/api/public/score-configs/{configId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/score-configs/{configId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scoreConfigs_get-by-id")
    @Override
    public CompletionStage<ScoreConfig> scoreConfigsGetById(
            APIScoreConfigsGetByIdRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scoreConfigsUpdate", method = "PATCH", path = "/api/public/score-configs/{configId}")
    @jakarta.ws.rs.PATCH
    @jakarta.ws.rs.Path("/api/public/score-configs/{configId}")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scoreConfigs_update")
    @Override
    public CompletionStage<ScoreConfig> scoreConfigsUpdate(
            APIScoreConfigsUpdateRequest apiRequest);

}
