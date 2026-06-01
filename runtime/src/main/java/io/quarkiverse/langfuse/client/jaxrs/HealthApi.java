package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.model.HealthResponse;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Health")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface HealthApi extends com.langfuse.api.health.HealthApi {

    /**
     * Check health of API and database
     *
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "", openApiSpecId = "langfuse_yml", operationId = "healthHealth", method = "GET", path = "/api/public/health")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/health")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("health_health")
    @Override
    public HealthResponse healthHealth();

}
