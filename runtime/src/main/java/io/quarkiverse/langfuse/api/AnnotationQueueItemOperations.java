package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.langfuse.api.model.AnnotationQueueItem;
import com.langfuse.api.model.CreateAnnotationQueueItemRequest;
import com.langfuse.api.model.UpdateAnnotationQueueItemRequest;

import io.quarkiverse.langfuse.api.deletion.DeletionOutcome;
import io.quarkiverse.langfuse.api.deletion.DeletionResult;
import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PageSelection;

/**
 * Higher-level operations over the items of a single Langfuse annotation queue.
 *
 * <p>
 * Obtained from {@link AnnotationQueueOperations#items(String)}. Unlike the other collections in
 * this layer, queue items are <strong>parent-scoped</strong>: every operation here is addressed
 * relative to the queue the view was opened on, so no operation takes a queue id. Items are a
 * page-addressed collection, so {@link #stream}, {@link #streamPages} and {@link #findPage} -
 * inherited from {@link PagedOperations} - accept a {@link PageSelection} or a {@link Page}
 * directly.
 *
 * <p>
 * <strong>Items have no name.</strong> Langfuse identifies a queue item by its id and the object it
 * points at, never by a name, so there is no {@code findByName} and no {@code createIfAbsent} here.
 * That is the API's shape, not an omission.
 *
 * @see AsyncAnnotationQueueItemOperations
 */
public sealed interface AnnotationQueueItemOperations extends PagedOperations<AnnotationQueueItem>
        permits DefaultAnnotationQueueItemOperations {

    /**
     * Finds an item of this queue by its id.
     *
     * <p>
     * This is a <strong>direct lookup</strong>: Langfuse resolves the id server-side against the queue
     * this view was opened on, so it costs a single request whatever the size of the queue and never
     * walks the collection.
     *
     * @param itemId the queue item id to look for, must not be {@code null} or blank
     * @return the matching item, or empty if this queue has no item with that id
     * @throws IllegalArgumentException if {@code itemId} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         item not existing
     */
    Optional<AnnotationQueueItem> findById(String itemId);

    /**
     * Adds an item to this queue.
     *
     * @param request the item to add, must not be {@code null}
     * @return the newly created item
     * @throws IllegalArgumentException if {@code request} is {@code null}
     */
    AnnotationQueueItem create(CreateAnnotationQueueItemRequest request);

    /**
     * Updates an item of this queue.
     *
     * <p>
     * <strong>A partial update.</strong> Langfuse patches only the properties the request carries and
     * leaves the rest of the item as it was, so this neither replaces the item nor creates one that is
     * missing.
     *
     * @param itemId the id of the item to update, must not be {@code null} or blank
     * @param request the properties to change, must not be {@code null}
     * @return the updated item
     * @throws IllegalArgumentException if {@code itemId} is {@code null} or blank, or if
     *         {@code request} is {@code null}
     * @throws com.langfuse.api.LangfuseApiException if the request fails, including because this queue
     *         has no item with that id
     */
    AnnotationQueueItem update(String itemId, UpdateAnnotationQueueItemRequest request);

    /**
     * Deletes the items of this queue with the given ids.
     *
     * <p>
     * <strong>Absence is not an error.</strong> An id matching no item of this queue yields a
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
     * {@link AsyncAnnotationQueueItemOperations#deleteById(Collection)} instead.
     *
     * @param itemIds the queue item ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id
     * @throws IllegalArgumentException if {@code itemIds} is {@code null}, or if any element is
     *         {@code null} or blank
     */
    DeletionResult deleteById(Collection<String> itemIds);

    /**
     * Deletes the item of this queue with the given id.
     *
     * @param itemId the queue item id to delete, must not be {@code null} or blank
     * @return the outcome for that id
     * @throws IllegalArgumentException if {@code itemId} is {@code null} or blank
     * @see #deleteById(Collection)
     */
    default DeletionResult deleteById(String itemId) {
        return deleteById(Collections.singletonList(itemId));
    }

    /**
     * Deletes the items of this queue with the given ids.
     *
     * @param itemIds the queue item ids to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct id
     * @throws IllegalArgumentException if {@code itemIds} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteById(Collection)
     */
    default DeletionResult deleteById(String... itemIds) {
        return deleteById((itemIds == null) ? null : Arrays.asList(itemIds));
    }
}
