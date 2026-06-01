package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.blobStorageIntegrations.BlobStorageIntegrationsApi.APIBlobStorageIntegrationsDeleteBlobStorageIntegrationRequest;
import com.langfuse.api.blobStorageIntegrations.BlobStorageIntegrationsApi.APIBlobStorageIntegrationsGetBlobStorageIntegrationStatusRequest;
import com.langfuse.api.blobStorageIntegrations.BlobStorageIntegrationsApi.APIBlobStorageIntegrationsUpsertBlobStorageIntegrationRequest;
import com.langfuse.api.model.BlobStorageIntegrationDeletionResponse;
import com.langfuse.api.model.BlobStorageIntegrationResponse;
import com.langfuse.api.model.BlobStorageIntegrationStatusResponse;
import com.langfuse.api.model.BlobStorageIntegrationsResponse;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "BlobStorageIntegrations")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface BlobStorageIntegrationsAsyncApi
        extends com.langfuse.api.blobStorageIntegrations.async.BlobStorageIntegrationsApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "blobStorageIntegrationsDeleteBlobStorageIntegration", method = "DELETE", path = "/api/public/integrations/blob-storage/{id}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/integrations/blob-storage/{id}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("blobStorageIntegrations_deleteBlobStorageIntegration")
    @Override
    public CompletionStage<BlobStorageIntegrationDeletionResponse> blobStorageIntegrationsDeleteBlobStorageIntegration(
            APIBlobStorageIntegrationsDeleteBlobStorageIntegrationRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "blobStorageIntegrationsGetBlobStorageIntegrationStatus", method = "GET", path = "/api/public/integrations/blob-storage/{id}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/integrations/blob-storage/{id}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("blobStorageIntegrations_getBlobStorageIntegrationStatus")
    @Override
    public CompletionStage<BlobStorageIntegrationStatusResponse> blobStorageIntegrationsGetBlobStorageIntegrationStatus(
            APIBlobStorageIntegrationsGetBlobStorageIntegrationStatusRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "blobStorageIntegrationsGetBlobStorageIntegrations", method = "GET", path = "/api/public/integrations/blob-storage")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/integrations/blob-storage")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("blobStorageIntegrations_getBlobStorageIntegrations")
    @Override
    public CompletionStage<BlobStorageIntegrationsResponse> blobStorageIntegrationsGetBlobStorageIntegrations();

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "blobStorageIntegrationsUpsertBlobStorageIntegration", method = "PUT", path = "/api/public/integrations/blob-storage")
    @jakarta.ws.rs.PUT
    @jakarta.ws.rs.Path("/api/public/integrations/blob-storage")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("blobStorageIntegrations_upsertBlobStorageIntegration")
    @Override
    public CompletionStage<BlobStorageIntegrationResponse> blobStorageIntegrationsUpsertBlobStorageIntegration(
            APIBlobStorageIntegrationsUpsertBlobStorageIntegrationRequest apiRequest);

}
