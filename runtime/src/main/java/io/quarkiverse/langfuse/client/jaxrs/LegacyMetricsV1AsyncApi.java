package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.legacyMetricsV1.LegacyMetricsV1Api.APILegacyMetricsV1MetricsRequest;
import com.langfuse.api.model.LegacyMetricsResponse;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "LegacyMetricsV1")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface LegacyMetricsV1AsyncApi extends com.langfuse.api.legacyMetricsV1.async.LegacyMetricsV1Api {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "legacyMetricsV1Metrics", method = "GET", path = "/api/public/metrics")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/metrics")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("legacy_metricsV1_metrics")
    @Override
    public CompletionStage<LegacyMetricsResponse> legacyMetricsV1Metrics(
            APILegacyMetricsV1MetricsRequest apiRequest);

}
