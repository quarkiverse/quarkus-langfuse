package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.langfuse.api.model.BlobStorageIntegrationResponse;
import com.langfuse.api.model.BlobStorageIntegrationStatusResponse;
import com.langfuse.api.model.CreateBlobStorageIntegrationRequest;

import io.quarkiverse.langfuse.api.deletion.DeletionOutcome;
import io.quarkiverse.langfuse.api.deletion.DeletionResult;
import io.smallrye.mutiny.Uni;

/**
 * Higher-level operations over Langfuse blob storage integrations, returning Mutiny types.
 *
 * <p>
 * Obtained from {@link AsyncLangfuseOperations#blobStorageIntegrations()}. The asynchronous
 * counterpart of {@link BlobStorageIntegrationOperations}; the two behave identically apart from how
 * absence is represented, which follows each style's own convention: {@link java.util.Optional} for
 * the synchronous tree, a {@code null} item for the asynchronous one.
 *
 * <p>
 * <strong>This collection is not paginated.</strong> Langfuse returns every blob storage integration
 * in a single response carrying no pagination metadata at all, so this interface extends neither the
 * page-addressed nor the cursor-addressed operations and {@link #findAll()} is one request.
 *
 * <p>
 * <strong>There is no {@code findById}.</strong> Langfuse's by-id endpoint returns a
 * {@link BlobStorageIntegrationStatusResponse} - the integration's export <em>sync status</em>, not
 * the integration itself - so it is exposed as {@link #findStatusById(String)}, named for what it
 * actually returns.
 *
 * @see BlobStorageIntegrationOperations
 */
public sealed interface AsyncBlobStorageIntegrationOperations permits DefaultAsyncBlobStorageIntegrationOperations {

    /**
     * Every blob storage integration configured for the organization.
     *
     * <p>
     * <strong>A single request.</strong> Langfuse returns the whole collection at once, so unlike the
     * paginated collections in this layer there is no traversal here and no batch size to configure.
     *
     * @return every integration, empty if none is configured. Never {@code null}
     */
    Uni<List<BlobStorageIntegrationResponse>> findAll();

    /**
     * Finds the export sync status of the integration with the given id.
     *
     * <p>
     * <strong>Emits {@code null} if no integration has that id.</strong>
     *
     * <p>
     * <strong>This returns a status, not the integration.</strong> Langfuse's by-id endpoint reports
     * whether the integration's last export succeeded and when it ran; it does not echo the
     * integration's configuration. Read {@link #findAll()} for the integration itself.
     *
     * <p>
     * This is a <strong>direct lookup</strong>: Langfuse resolves the id server-side, so it costs a
     * single request.
     *
     * @param id the integration id to look for, must not be {@code null} or blank
     * @return the matching integration's status, or {@code null} if no integration has that id
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank. Thrown from this call
     *         rather than emitted as a failure
     */
    Uni<BlobStorageIntegrationStatusResponse> findStatusById(String id);

    /**
     * Creates the integration for its project if none exists, or replaces it if one does.
     *
     * <p>
     * <strong>Atomic.</strong> Backed directly by Langfuse's own create-or-update operation, so - unlike
     * the {@code createIfAbsent} operations on the other domains - this is safe under concurrent callers.
     *
     * @param request the integration to create or replace
     * @return the resulting integration
     */
    Uni<BlobStorageIntegrationResponse> upsert(CreateBlobStorageIntegrationRequest request);

    /**
     * Deletes the blob storage integrations with the given ids.
     *
     * <p>
     * <strong>Absence is not an error.</strong> An id matching no integration yields a
     * {@link DeletionOutcome.NotFound} outcome rather than failing the {@link Uni}, so deleting
     * something that is already gone is a normal result rather than a failure to handle.
     *
     * <p>
     * <strong>Never fails fast.</strong> Every id is attempted regardless of what happened to the
     * others, and each is reported separately: one failure neither hides the successes nor prevents
     * the remaining work.
     *
     * <p>
     * <strong>Not atomic.</strong> Langfuse offers no bulk delete, so this iterates client-side and
     * can partially apply. Inspect the returned {@link DeletionResult} rather than assuming
     * all-or-nothing.
     *
     * @param ids the integration ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id. Never {@code null}
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank. Thrown from this call rather than emitted as a failure
     */
    Uni<DeletionResult> deleteById(Collection<String> ids);

    /**
     * Deletes the blob storage integration with the given id.
     *
     * @param id the integration id to delete, must not be {@code null} or blank
     * @return the outcome for that id. Never {@code null}
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @see #deleteById(Collection)
     */
    default Uni<DeletionResult> deleteById(String id) {
        return deleteById(Collections.singletonList(id));
    }

    /**
     * Deletes the blob storage integrations with the given ids.
     *
     * @param ids the integration ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id. Never {@code null}
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteById(Collection)
     */
    default Uni<DeletionResult> deleteById(String... ids) {
        return deleteById((ids == null) ? null : Arrays.asList(ids));
    }
}
