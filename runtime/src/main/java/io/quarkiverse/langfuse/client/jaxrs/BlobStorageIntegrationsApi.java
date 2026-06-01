package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.blobStorageIntegrations.BlobStorageIntegrationsApi.APIBlobStorageIntegrationsDeleteBlobStorageIntegrationRequest;
import com.langfuse.api.blobStorageIntegrations.BlobStorageIntegrationsApi.APIBlobStorageIntegrationsGetBlobStorageIntegrationStatusRequest;
import com.langfuse.api.blobStorageIntegrations.BlobStorageIntegrationsApi.APIBlobStorageIntegrationsUpsertBlobStorageIntegrationRequest;
import com.langfuse.api.model.BlobStorageIntegrationDeletionResponse;
import com.langfuse.api.model.BlobStorageIntegrationResponse;
import com.langfuse.api.model.BlobStorageIntegrationStatusResponse;
import com.langfuse.api.model.BlobStorageIntegrationsResponse;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "BlobStorageIntegrations")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface BlobStorageIntegrationsApi extends com.langfuse.api.blobStorageIntegrations.BlobStorageIntegrationsApi {

    /**
     * Delete a blob storage integration by ID
     *
     * @param apiRequest {@link APIBlobStorageIntegrationsDeleteBlobStorageIntegrationRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "blobStorageIntegrationsDeleteBlobStorageIntegration", method = "DELETE", path = "/api/public/integrations/blob-storage/{id}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/integrations/blob-storage/{id}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("blobStorageIntegrations_deleteBlobStorageIntegration")
    @Override
    public BlobStorageIntegrationDeletionResponse blobStorageIntegrationsDeleteBlobStorageIntegration(
            APIBlobStorageIntegrationsDeleteBlobStorageIntegrationRequest apiRequest);

    /**
     * Get the sync status of a blob storage integration by integration ID
     *
     * @param apiRequest {@link APIBlobStorageIntegrationsGetBlobStorageIntegrationStatusRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "blobStorageIntegrationsGetBlobStorageIntegrationStatus", method = "GET", path = "/api/public/integrations/blob-storage/{id}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/integrations/blob-storage/{id}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("blobStorageIntegrations_getBlobStorageIntegrationStatus")
    @Override
    public BlobStorageIntegrationStatusResponse blobStorageIntegrationsGetBlobStorageIntegrationStatus(
            APIBlobStorageIntegrationsGetBlobStorageIntegrationStatusRequest apiRequest);

    /**
     * Get all blob storage integrations for the organization
     *
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "blobStorageIntegrationsGetBlobStorageIntegrations", method = "GET", path = "/api/public/integrations/blob-storage")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/integrations/blob-storage")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("blobStorageIntegrations_getBlobStorageIntegrations")
    @Override
    public BlobStorageIntegrationsResponse blobStorageIntegrationsGetBlobStorageIntegrations();

    /**
     * Create or update a blob storage integration for a specific project.
     *
     * @param apiRequest {@link APIBlobStorageIntegrationsUpsertBlobStorageIntegrationRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "blobStorageIntegrationsUpsertBlobStorageIntegration", method = "PUT", path = "/api/public/integrations/blob-storage")
    @jakarta.ws.rs.PUT
    @jakarta.ws.rs.Path("/api/public/integrations/blob-storage")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("blobStorageIntegrations_upsertBlobStorageIntegration")
    @Override
    public BlobStorageIntegrationResponse blobStorageIntegrationsUpsertBlobStorageIntegration(
            APIBlobStorageIntegrationsUpsertBlobStorageIntegrationRequest apiRequest);

}
