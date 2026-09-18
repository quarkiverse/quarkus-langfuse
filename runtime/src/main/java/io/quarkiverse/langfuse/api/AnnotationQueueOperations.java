package io.quarkiverse.langfuse.api;

import java.util.Optional;

import com.langfuse.api.model.AnnotationQueue;
import com.langfuse.api.model.CreateAnnotationQueueRequest;

import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PageSelection;

/**
 * Higher-level operations over Langfuse annotation queues.
 *
 * <p>
 * Obtained from {@link LangfuseOperations#annotationQueues()}. Annotation queues are a
 * page-addressed collection, so {@link #stream}, {@link #streamPages} and {@link #findPage} -
 * inherited from {@link PagedOperations} - accept a {@link PageSelection} or a {@link Page}
 * directly.
 *
 * <p>
 * <strong>Queues cannot be deleted through this layer.</strong> Langfuse offers no delete for the
 * queues themselves; only the items inside a queue and its assignments can be removed. That is the
 * API's shape, not an omission here.
 *
 * @see AsyncAnnotationQueueOperations
 */
public sealed interface AnnotationQueueOperations extends PagedOperations<AnnotationQueue>
        permits DefaultAnnotationQueueOperations {

    /**
     * Finds an annotation queue by its id.
     *
     * <p>
     * Unlike {@link #findByName(String)}, this is a <strong>direct lookup</strong>: Langfuse resolves
     * the id server-side, so it costs a single request whatever the size of the collection. Prefer it
     * wherever the id is already known.
     *
     * @param id the annotation queue id to look for, must not be {@code null} or blank
     * @return the matching annotation queue, or empty if no queue has that id
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         annotation queue not existing
     */
    Optional<AnnotationQueue> findById(String id);

    /**
     * Finds an annotation queue by its exact name.
     *
     * <p>
     * Names are matched exactly, as Langfuse stores them. Every {@code findByName} operation in this
     * layer matches the same way, whether the match is performed by the server or while walking the
     * collection here.
     *
     * <p>
     * Langfuse offers no name filter for annotation queues, so this walks the collection and stops at
     * the first match: a queue found in the first page costs a single request.
     *
     * @param queueName the annotation queue name to look for, must not be {@code null} or blank
     * @return the matching annotation queue, or empty if no queue has that name
     * @throws IllegalArgumentException if {@code queueName} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         annotation queue not existing
     */
    Optional<AnnotationQueue> findByName(String queueName);

    /**
     * Whether an annotation queue with the given exact name exists.
     *
     * @param queueName the annotation queue name to look for, must not be {@code null} or blank
     * @return {@code true} if a queue with that name exists
     * @throws IllegalArgumentException if {@code queueName} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         annotation queue not existing
     */
    default boolean exists(String queueName) {
        return findByName(queueName).isPresent();
    }

    /**
     * Returns the annotation queue with the requested name, creating it if no queue has that name.
     *
     * <p>
     * <strong>Not atomic.</strong> Langfuse offers no create-or-update-by-name operation for
     * annotation queues, so this performs a lookup followed by a create. Concurrent callers may
     * therefore both observe the queue as absent and both create it.
     *
     * @param request the annotation queue to create if it is missing
     * @return the existing or newly created annotation queue
     */
    AnnotationQueue createIfAbsent(CreateAnnotationQueueRequest request);

    /**
     * The items of the queue with the given id.
     *
     * <p>
     * Queue items are addressed relative to their queue, so they are reached through this view rather
     * than as a top-level collection. Every operation on the returned view applies to that queue alone,
     * and {@code findAll()} on it means every item <strong>of that queue</strong>.
     *
     * <p>
     * <strong>The queue id is validated here</strong>, not on first use, so a blank id fails from this
     * call rather than later from a traversal. The queue is not looked up: a view over a queue that
     * does not exist is created happily and reports the absence when it is used.
     *
     * @param queueId the id of the queue whose items to operate on, must not be {@code null} or blank
     * @return the operations over that queue's items
     * @throws IllegalArgumentException if {@code queueId} is {@code null} or blank
     */
    AnnotationQueueItemOperations items(String queueId);
}
