package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.legacyScoreV1.LegacyScoreV1Api.APILegacyScoreV1CreateRequest;
import com.langfuse.api.legacyScoreV1.LegacyScoreV1Api.APILegacyScoreV1DeleteRequest;
import com.langfuse.api.model.LegacyCreateScoreResponse;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "LegacyScoreV1")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface LegacyScoreV1AsyncApi extends com.langfuse.api.legacyScoreV1.async.LegacyScoreV1Api {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "legacyScoreV1Create", method = "POST", path = "/api/public/scores")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/scores")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("legacy_scoreV1_create")
    @Override
    public CompletionStage<LegacyCreateScoreResponse> legacyScoreV1Create(
            APILegacyScoreV1CreateRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "legacyScoreV1Delete", method = "DELETE", path = "/api/public/scores/{scoreId}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/scores/{scoreId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("legacy_scoreV1_delete")
    @Override
    public CompletionStage<Void> legacyScoreV1Delete(
            APILegacyScoreV1DeleteRequest apiRequest);

}
