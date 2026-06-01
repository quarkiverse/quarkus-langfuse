package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.model.ScoreConfig;
import com.langfuse.api.model.ScoreConfigs;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi.APIScoreConfigsCreateRequest;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi.APIScoreConfigsGetByIdRequest;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi.APIScoreConfigsGetRequest;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi.APIScoreConfigsUpdateRequest;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "ScoreConfigs")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface ScoreConfigsApi extends com.langfuse.api.scoreConfigs.ScoreConfigsApi {

    /**
     * Create a score configuration (config).
     *
     * @param apiRequest {@link APIScoreConfigsCreateRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scoreConfigsCreate", method = "POST", path = "/api/public/score-configs")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/score-configs")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scoreConfigs_create")
    @Override
    public ScoreConfig scoreConfigsCreate(
            APIScoreConfigsCreateRequest apiRequest);

    /**
     * Get all score configs
     *
     * @param apiRequest {@link APIScoreConfigsGetRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scoreConfigsGet", method = "GET", path = "/api/public/score-configs")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/score-configs")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scoreConfigs_get")
    @Override
    public ScoreConfigs scoreConfigsGet(
            APIScoreConfigsGetRequest apiRequest);

    /**
     * Get a score config
     *
     * @param apiRequest {@link APIScoreConfigsGetByIdRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scoreConfigsGetById", method = "GET", path = "/api/public/score-configs/{configId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/score-configs/{configId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scoreConfigs_get-by-id")
    @Override
    public ScoreConfig scoreConfigsGetById(
            APIScoreConfigsGetByIdRequest apiRequest);

    /**
     * Update a score config
     *
     * @param apiRequest {@link APIScoreConfigsUpdateRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scoreConfigsUpdate", method = "PATCH", path = "/api/public/score-configs/{configId}")
    @jakarta.ws.rs.PATCH
    @jakarta.ws.rs.Path("/api/public/score-configs/{configId}")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scoreConfigs_update")
    @Override
    public ScoreConfig scoreConfigsUpdate(
            APIScoreConfigsUpdateRequest apiRequest);

}
