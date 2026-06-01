package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.metrics.MetricsApi.APIMetricsMetricsRequest;
import com.langfuse.api.model.MetricsV2Response;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Metrics")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface MetricsAsyncApi extends com.langfuse.api.metrics.async.MetricsApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "metricsMetrics", method = "GET", path = "/api/public/v2/metrics")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/v2/metrics")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("metrics_metrics")
    @Override
    public CompletionStage<MetricsV2Response> metricsMetrics(
            APIMetricsMetricsRequest apiRequest);

}
