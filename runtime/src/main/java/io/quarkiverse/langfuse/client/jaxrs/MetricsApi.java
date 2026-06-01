package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.metrics.MetricsApi.APIMetricsMetricsRequest;
import com.langfuse.api.model.MetricsV2Response;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Metrics")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface MetricsApi extends com.langfuse.api.metrics.MetricsApi {

    /**
     * Get metrics from the Langfuse project using a query object. V2 endpoint with optimized performance.
     *
     * @param apiRequest {@link APIMetricsMetricsRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "metricsMetrics", method = "GET", path = "/api/public/v2/metrics")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/v2/metrics")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("metrics_metrics")
    @Override
    public MetricsV2Response metricsMetrics(
            APIMetricsMetricsRequest apiRequest);

}
