package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.ingestion.IngestionApi.APIIngestionBatchRequest;
import com.langfuse.api.model.IngestionResponse;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Ingestion")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface IngestionApi extends com.langfuse.api.ingestion.IngestionApi {

    /**
     * **Legacy endpoint for batch ingestion for Langfuse Observability.** -> Please use the OpenTelemetry endpoint
     * (`/api/public/otel/v1/traces`). Learn more: https://langfuse.com/integrations/native/opentelemetry Within each batch,
     * there can be multiple events. Each event has a type, an id, a timestamp, metadata and a body. Internally, we refer to
     * this as the "event envelope" as it tells us something about the event but not the trace. We use the event id within this
     * envelope to deduplicate messages to avoid processing the same event twice, i.e. the event id should be unique per
     * request. The event.body.id is the ID of the actual trace and will be used for updates and will be visible within the
     * Langfuse App. I.e. if you want to update a trace, you'd use the same body id, but separate event IDs. Notes: -
     * Introduction to data model: https://langfuse.com/docs/observability/data-model - Batch sizes are limited to 3.5 MB in
     * total. You need to adjust the number of events per batch accordingly. - The API does not return a 4xx status code for
     * input errors. Instead, it responds with a 207 status code, which includes a list of the encountered errors.
     *
     * @param apiRequest {@link APIIngestionBatchRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "ingestionBatch", method = "POST", path = "/api/public/ingestion")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/ingestion")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("ingestion_batch")
    @Override
    public IngestionResponse ingestionBatch(
            APIIngestionBatchRequest apiRequest);

}
