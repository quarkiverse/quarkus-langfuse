package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.model.ObservationsV2Response;
import com.langfuse.api.observations.ObservationsApi.APIObservationsGetManyRequest;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Observations")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface ObservationsApi extends com.langfuse.api.observations.ObservationsApi {

    /**
     * Get a list of observations with cursor-based pagination and flexible field selection.
     *
     * @param apiRequest {@link APIObservationsGetManyRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "observationsGetMany", method = "GET", path = "/api/public/v2/observations")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/v2/observations")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("observations_getMany")
    @Override
    public ObservationsV2Response observationsGetMany(
            APIObservationsGetManyRequest apiRequest);

}
