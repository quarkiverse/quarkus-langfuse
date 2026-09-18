package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.langfuse.api.model.BlobStorageIntegrationResponse;
import com.langfuse.api.model.BlobStorageIntegrationStatusResponse;
import com.langfuse.api.model.CreateBlobStorageIntegrationRequest;

import io.quarkiverse.langfuse.api.deletion.DeletionOutcome;
import io.quarkiverse.langfuse.api.deletion.DeletionResult;

/**
 * Higher-level operations over Langfuse blob storage integrations.
 *
 * <p>
 * Obtained from {@link LangfuseOperations#blobStorageIntegrations()}.
 *
 * <p>
 * <strong>This collection is not paginated.</strong> Alone among the collections in this layer,
 * Langfuse returns every blob storage integration in a single response carrying no pagination
 * metadata at all - no cursor, no page index, no totals - so there is nothing to page through.
 * {@link #findAll()} is therefore one request, and this interface extends neither the
 * page-addressed nor the cursor-addressed operations: there is no {@code stream}, no
 * {@code streamPages}/{@code streamBatches} and no {@code findPage}/{@code findBatch}, because a
 * selection over a single fixed response would be a fiction.
 *
 * <p>
 * <strong>There is no {@code findById}.</strong> Langfuse's by-id endpoint returns a
 * {@link BlobStorageIntegrationStatusResponse} - the integration's export <em>sync status</em>, not
 * the integration itself - so it is exposed as {@link #findStatusById(String)}, named for what it
 * actually returns. A {@code findById} around it would promise a
 * {@link BlobStorageIntegrationResponse} it cannot produce. To obtain the integration itself, read
 * it from {@link #findAll()}.
 *
 * <p>
 * Integrations are keyed by {@code projectId} rather than by name - the model carries no name at all
 * - and Langfuse offers a genuine create-or-update operation for them, so
 * {@link #upsert(CreateBlobStorageIntegrationRequest)} - unlike the {@code createIfAbsent}
 * operations on the other domains - is atomic. For the same reason there is no {@code findByName},
 * no {@code exists} and no {@code deleteByName}.
 *
 * @see AsyncBlobStorageIntegrationOperations
 */
public sealed interface BlobStorageIntegrationOperations permits DefaultBlobStorageIntegrationOperations {

    /**
     * Every blob storage integration configured for the organization.
     *
     * <p>
     * <strong>A single request.</strong> Langfuse returns the whole collection at once, so unlike the
     * paginated collections in this layer there is no traversal here and no batch size to configure.
     *
     * <p>
     * <strong>Blocks the calling thread.</strong> Do not call this from a Vert.x I/O thread; use
     * {@link AsyncBlobStorageIntegrationOperations#findAll()} instead.
     *
     * @return every integration, empty if none is configured. Never {@code null}
     * @throws com.langfuse.api.LangfuseApiException if the request fails
     */
    List<BlobStorageIntegrationResponse> findAll();

    /**
     * Finds the export sync status of the integration with the given id.
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
     * @return the matching integration's status, or empty if no integration has that id
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         integration not existing
     */
    Optional<BlobStorageIntegrationStatusResponse> findStatusById(String id);

    /**
     * Creates the integration for its project if none exists, or replaces it if one does.
     *
     * <p>
     * <strong>Atomic.</strong> Backed directly by Langfuse's own create-or-update operation, so - unlike
     * the {@code createIfAbsent} operations on the other domains - this is safe under concurrent callers.
     *
     * @param request the integration to create or replace
     * @return the resulting integration
     * @throws com.langfuse.api.LangfuseApiException if the request fails
     */
    BlobStorageIntegrationResponse upsert(CreateBlobStorageIntegrationRequest request);

    /**
     * Deletes the blob storage integrations with the given ids.
     *
     * <p>
     * <strong>Absence is not an error.</strong> An id matching no integration yields a
     * {@link DeletionOutcome.NotFound} outcome rather than throwing, so deleting something that is
     * already gone is a normal result rather than a failure to handle.
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
     * <p>
     * <strong>Blocks the calling thread.</strong> Do not call this from a Vert.x I/O thread; use
     * {@link AsyncBlobStorageIntegrationOperations#deleteById(Collection)} instead.
     *
     * @param ids the integration ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     */
    DeletionResult deleteById(Collection<String> ids);

    /**
     * Deletes the blob storage integration with the given id.
     *
     * @param id the integration id to delete, must not be {@code null} or blank
     * @return the outcome for that id
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @see #deleteById(Collection)
     */
    default DeletionResult deleteById(String id) {
        return deleteById(Collections.singletonList(id));
    }

    /**
     * Deletes the blob storage integrations with the given ids.
     *
     * @param ids the integration ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id
     * @throws IllegalArgumentException if {@code ids} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteById(Collection)
     */
    default DeletionResult deleteById(String... ids) {
        return deleteById((ids == null) ? null : Arrays.asList(ids));
    }
}
