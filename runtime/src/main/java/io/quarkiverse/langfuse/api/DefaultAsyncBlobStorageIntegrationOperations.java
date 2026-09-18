package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.List;

import com.langfuse.api.blobStorageIntegrations.BlobStorageIntegrationsApi.APIBlobStorageIntegrationsDeleteBlobStorageIntegrationRequest;
import com.langfuse.api.blobStorageIntegrations.BlobStorageIntegrationsApi.APIBlobStorageIntegrationsGetBlobStorageIntegrationStatusRequest;
import com.langfuse.api.blobStorageIntegrations.BlobStorageIntegrationsApi.APIBlobStorageIntegrationsUpsertBlobStorageIntegrationRequest;
import com.langfuse.api.blobStorageIntegrations.async.BlobStorageIntegrationsApi;
import com.langfuse.api.model.BlobStorageIntegrationResponse;
import com.langfuse.api.model.BlobStorageIntegrationStatusResponse;
import com.langfuse.api.model.CreateBlobStorageIntegrationRequest;

import io.quarkiverse.langfuse.api.deletion.DeletionResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;
import io.smallrye.mutiny.Uni;

// Extends no Abstract*Operations base: the listing endpoint is unpaginated, so there is no fetcher to
// hand an engine and no batch size to honour. That also means deleteConcurrency() is read here rather
// than inherited - per subscription, never cached, so a runtime config change is picked up.
final class DefaultAsyncBlobStorageIntegrationOperations implements AsyncBlobStorageIntegrationOperations {
    private final BlobStorageIntegrationsApi blobStorageIntegrationsApi;
    private final LangfuseConfig config;

    DefaultAsyncBlobStorageIntegrationOperations(BlobStorageIntegrationsApi blobStorageIntegrationsApi,
            LangfuseConfig config) {
        this.blobStorageIntegrationsApi = blobStorageIntegrationsApi;
        this.config = config;
    }

    @Override
    public Uni<List<BlobStorageIntegrationResponse>> findAll() {
        // One request, no pagination: the response carries getData() and no meta at all, so there is
        // nothing to resume from and no synthetic single-page result to fabricate.
        return Uni.createFrom()
                .completionStage(this.blobStorageIntegrationsApi::blobStorageIntegrationsGetBlobStorageIntegrations)
                .map(response -> (response.getData() == null) ? List.<BlobStorageIntegrationResponse> of()
                        : List.copyOf(response.getData()));
    }

    @Override
    public Uni<BlobStorageIntegrationStatusResponse> findStatusById(String id) {
        // Validated here rather than inside the completionStage supplier: a throw in there becomes a
        // failure event, and blank input must surface as a thrown IllegalArgumentException.
        var integrationId = ValidationUtils.ensureNotBlank(id, "Blob storage integration id");

        // Direct GET, so no scan. recoverWithNull is scoped to LangfuseNotFoundException alone: every
        // other failure, a 401 included, must still fail the Uni rather than read as absence.
        return Uni.createFrom()
                .completionStage(() -> this.blobStorageIntegrationsApi
                        .blobStorageIntegrationsGetBlobStorageIntegrationStatus(
                                APIBlobStorageIntegrationsGetBlobStorageIntegrationStatusRequest.newBuilder()
                                        .id(integrationId)
                                        .build()))
                .onFailure(LangfuseNotFoundException.class)
                .recoverWithNull();
    }

    @Override
    public Uni<BlobStorageIntegrationResponse> upsert(CreateBlobStorageIntegrationRequest request) {
        return Uni.createFrom()
                .completionStage(() -> this.blobStorageIntegrationsApi.blobStorageIntegrationsUpsertBlobStorageIntegration(
                        APIBlobStorageIntegrationsUpsertBlobStorageIntegrationRequest.newBuilder()
                                .createBlobStorageIntegrationRequest(request)
                                .build()));
    }

    @Override
    public Uni<DeletionResult> deleteById(Collection<String> ids) {
        // Validation runs before the deferred wrapper so malformed input throws from the call, as the
        // rest of this tree does. Inside the supplier it would surface as a failed Uni at subscription
        // instead. deleteConcurrency() stays inside, so the config is read per subscription.
        DeletionIdentifiers.validated(ids, "Blob storage integration id");

        return Uni.createFrom()
                .deferred(() -> AsyncDeletions.deleteAll(ids, "Blob storage integration id",
                        Uni.createFrom()::item, this::delete, deleteConcurrency()));
    }

    private Uni<?> delete(String id) {
        return Uni.createFrom()
                .completionStage(() -> this.blobStorageIntegrationsApi.blobStorageIntegrationsDeleteBlobStorageIntegration(
                        APIBlobStorageIntegrationsDeleteBlobStorageIntegrationRequest.newBuilder()
                                .id(id)
                                .build()));
    }

    private int deleteConcurrency() {
        return Math.max(1, this.config.api().deleteConcurrency());
    }
}
