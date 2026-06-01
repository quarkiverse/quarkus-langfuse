package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.legacyObservationsV1.LegacyObservationsV1Api.APILegacyObservationsV1GetManyRequest;
import com.langfuse.api.legacyObservationsV1.LegacyObservationsV1Api.APILegacyObservationsV1GetRequest;
import com.langfuse.api.model.LegacyObservationsViews;
import com.langfuse.api.model.ObservationsView;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "LegacyObservationsV1")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface LegacyObservationsV1Api extends com.langfuse.api.legacyObservationsV1.LegacyObservationsV1Api {

    /**
     * Get a observation
     *
     * @param apiRequest {@link APILegacyObservationsV1GetRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "legacyObservationsV1Get", method = "GET", path = "/api/public/observations/{observationId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/observations/{observationId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("legacy_observationsV1_get")
    @Override
    public ObservationsView legacyObservationsV1Get(
            APILegacyObservationsV1GetRequest apiRequest);

    /**
     * Get a list of observations.
     *
     * @param apiRequest {@link APILegacyObservationsV1GetManyRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "legacyObservationsV1GetMany", method = "GET", path = "/api/public/observations")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/observations")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("legacy_observationsV1_getMany")
    @Override
    public LegacyObservationsViews legacyObservationsV1GetMany(
            APILegacyObservationsV1GetManyRequest apiRequest);

}
