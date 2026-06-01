package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.legacyScoreV1.LegacyScoreV1Api.APILegacyScoreV1CreateRequest;
import com.langfuse.api.legacyScoreV1.LegacyScoreV1Api.APILegacyScoreV1DeleteRequest;
import com.langfuse.api.model.LegacyCreateScoreResponse;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "LegacyScoreV1")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface LegacyScoreV1Api extends com.langfuse.api.legacyScoreV1.LegacyScoreV1Api {

    /**
     * Create a score (supports both trace and session scores)
     *
     * @param apiRequest {@link APILegacyScoreV1CreateRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "legacyScoreV1Create", method = "POST", path = "/api/public/scores")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/scores")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("legacy_scoreV1_create")
    @Override
    public LegacyCreateScoreResponse legacyScoreV1Create(
            APILegacyScoreV1CreateRequest apiRequest);

    /**
     * Delete a score (supports both trace and session scores)
     *
     * @param apiRequest {@link APILegacyScoreV1DeleteRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "legacyScoreV1Delete", method = "DELETE", path = "/api/public/scores/{scoreId}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/scores/{scoreId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("legacy_scoreV1_delete")
    @Override
    public void legacyScoreV1Delete(
            APILegacyScoreV1DeleteRequest apiRequest);

}
