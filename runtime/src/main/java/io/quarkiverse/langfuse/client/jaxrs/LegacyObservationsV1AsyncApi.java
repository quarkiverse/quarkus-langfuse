package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.legacyObservationsV1.LegacyObservationsV1Api.APILegacyObservationsV1GetManyRequest;
import com.langfuse.api.legacyObservationsV1.LegacyObservationsV1Api.APILegacyObservationsV1GetRequest;
import com.langfuse.api.model.LegacyObservationsViews;
import com.langfuse.api.model.ObservationsView;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "LegacyObservationsV1")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface LegacyObservationsV1AsyncApi extends com.langfuse.api.legacyObservationsV1.async.LegacyObservationsV1Api {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "legacyObservationsV1Get", method = "GET", path = "/api/public/observations/{observationId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/observations/{observationId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("legacy_observationsV1_get")
    @Override
    public CompletionStage<ObservationsView> legacyObservationsV1Get(
            APILegacyObservationsV1GetRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "legacyObservationsV1GetMany", method = "GET", path = "/api/public/observations")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/observations")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("legacy_observationsV1_getMany")
    @Override
    public CompletionStage<LegacyObservationsViews> legacyObservationsV1GetMany(
            APILegacyObservationsV1GetManyRequest apiRequest);

}
