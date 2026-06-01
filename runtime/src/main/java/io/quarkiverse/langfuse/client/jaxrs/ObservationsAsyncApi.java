package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.model.ObservationsV2Response;
import com.langfuse.api.observations.ObservationsApi.APIObservationsGetManyRequest;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Observations")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface ObservationsAsyncApi extends com.langfuse.api.observations.async.ObservationsApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "observationsGetMany", method = "GET", path = "/api/public/v2/observations")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/v2/observations")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("observations_getMany")
    @Override
    public CompletionStage<ObservationsV2Response> observationsGetMany(
            APIObservationsGetManyRequest apiRequest);

}
