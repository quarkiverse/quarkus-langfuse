package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.ingestion.IngestionApi.APIIngestionBatchRequest;
import com.langfuse.api.model.IngestionResponse;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Ingestion")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface IngestionAsyncApi extends com.langfuse.api.ingestion.async.IngestionApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "ingestionBatch", method = "POST", path = "/api/public/ingestion")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/ingestion")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("ingestion_batch")
    @Override
    public CompletionStage<IngestionResponse> ingestionBatch(
            APIIngestionBatchRequest apiRequest);

}
