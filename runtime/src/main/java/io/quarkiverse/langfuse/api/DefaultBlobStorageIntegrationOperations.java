package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.langfuse.api.blobStorageIntegrations.BlobStorageIntegrationsApi;
import com.langfuse.api.blobStorageIntegrations.BlobStorageIntegrationsApi.APIBlobStorageIntegrationsDeleteBlobStorageIntegrationRequest;
import com.langfuse.api.blobStorageIntegrations.BlobStorageIntegrationsApi.APIBlobStorageIntegrationsGetBlobStorageIntegrationStatusRequest;
import com.langfuse.api.blobStorageIntegrations.BlobStorageIntegrationsApi.APIBlobStorageIntegrationsUpsertBlobStorageIntegrationRequest;
import com.langfuse.api.model.BlobStorageIntegrationResponse;
import com.langfuse.api.model.BlobStorageIntegrationStatusResponse;
import com.langfuse.api.model.CreateBlobStorageIntegrationRequest;

import io.quarkiverse.langfuse.api.deletion.DeletionResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;

// Extends no Abstract*Operations base: the listing endpoint is unpaginated, so there is no fetcher to
// hand an engine and no batch size to honour. That also means deleteConcurrency() is read here rather
// than inherited - per call, never cached, so a runtime config change is picked up.
final class DefaultBlobStorageIntegrationOperations implements BlobStorageIntegrationOperations {
    private final BlobStorageIntegrationsApi blobStorageIntegrationsApi;
    private final LangfuseConfig config;

    DefaultBlobStorageIntegrationOperations(BlobStorageIntegrationsApi blobStorageIntegrationsApi,
            LangfuseConfig config) {
        this.blobStorageIntegrationsApi = blobStorageIntegrationsApi;
        this.config = config;
    }

    @Override
    public List<BlobStorageIntegrationResponse> findAll() {
        // One request, no pagination: the response carries getData() and no meta at all, so there is
        // nothing to resume from and no synthetic single-page result to fabricate.
        var data = this.blobStorageIntegrationsApi.blobStorageIntegrationsGetBlobStorageIntegrations().getData();

        return (data == null) ? List.of() : List.copyOf(data);
    }

    @Override
    public Optional<BlobStorageIntegrationStatusResponse> findStatusById(String id) {
        var integrationId = ValidationUtils.ensureNotBlank(id, "Blob storage integration id");

        // Direct GET, so no scan: the server resolves the id. Only LangfuseNotFoundException is caught -
        // catching LangfuseApiException would report a 401 or a 500 as "absent", which is the one
        // mistake this layer must never make.
        try {
            return Optional.of(this.blobStorageIntegrationsApi.blobStorageIntegrationsGetBlobStorageIntegrationStatus(
                    APIBlobStorageIntegrationsGetBlobStorageIntegrationStatusRequest.newBuilder()
                            .id(integrationId)
                            .build()));
        } catch (LangfuseNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public BlobStorageIntegrationResponse upsert(CreateBlobStorageIntegrationRequest request) {
        return this.blobStorageIntegrationsApi.blobStorageIntegrationsUpsertBlobStorageIntegration(
                APIBlobStorageIntegrationsUpsertBlobStorageIntegrationRequest.newBuilder()
                        .createBlobStorageIntegrationRequest(request)
                        .build());
    }

    @Override
    public DeletionResult deleteById(Collection<String> ids) {
        // Optional::of is the identity resolver: the delete endpoint takes the integration id directly,
        // so nothing has to be resolved and no listing request is issued.
        return Deletions.deleteAll(ids, "Blob storage integration id", Optional::of, this::delete,
                deleteConcurrency());
    }

    // Langfuse answers a delete with a BlobStorageIntegrationDeletionResponse rather than 204. The
    // engine wants a Consumer, so the response is simply not read: there is nothing in it this layer
    // reports.
    private void delete(String id) {
        this.blobStorageIntegrationsApi.blobStorageIntegrationsDeleteBlobStorageIntegration(
                APIBlobStorageIntegrationsDeleteBlobStorageIntegrationRequest.newBuilder()
                        .id(id)
                        .build());
    }

    private int deleteConcurrency() {
        return Math.max(1, this.config.api().deleteConcurrency());
    }
}
