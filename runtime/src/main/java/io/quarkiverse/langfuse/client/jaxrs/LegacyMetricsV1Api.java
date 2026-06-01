package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.legacyMetricsV1.LegacyMetricsV1Api.APILegacyMetricsV1MetricsRequest;
import com.langfuse.api.model.LegacyMetricsResponse;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "LegacyMetricsV1")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface LegacyMetricsV1Api extends com.langfuse.api.legacyMetricsV1.LegacyMetricsV1Api {

    /**
     * Get metrics from the Langfuse project using a query object.
     *
     * @param apiRequest {@link APILegacyMetricsV1MetricsRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "legacyMetricsV1Metrics", method = "GET", path = "/api/public/metrics")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/metrics")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("legacy_metricsV1_metrics")
    @Override
    public LegacyMetricsResponse legacyMetricsV1Metrics(
            APILegacyMetricsV1MetricsRequest apiRequest);

}
