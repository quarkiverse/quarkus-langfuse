package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.Optional;

import com.langfuse.api.annotationQueues.AnnotationQueuesApi;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesCreateQueueItemRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesDeleteQueueItemRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesGetQueueItemRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesListQueueItemsRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesUpdateQueueItemRequest;
import com.langfuse.api.model.AnnotationQueueItem;
import com.langfuse.api.model.CreateAnnotationQueueItemRequest;
import com.langfuse.api.model.UpdateAnnotationQueueItemRequest;

import io.quarkiverse.langfuse.api.deletion.DeletionResult;
import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;

final class DefaultAnnotationQueueItemOperations extends AbstractPagedOperations<AnnotationQueueItem>
        implements AnnotationQueueItemOperations {
    private final AnnotationQueuesApi annotationQueuesApi;
    private final String queueId;

    // The parent queue id is captured in the fetcher lambda handed to the engine, which is why a
    // parent-scoped collection needs neither a widened PageFetcher nor a change to Pagination: this
    // view is just the same class constructed over a different fetcher. The id is already validated by
    // the accessor that builds this view.
    DefaultAnnotationQueueItemOperations(AnnotationQueuesApi annotationQueuesApi, LangfuseConfig config, String queueId) {
        super(page -> fetch(annotationQueuesApi, queueId, page), config);
        this.annotationQueuesApi = annotationQueuesApi;
        this.queueId = queueId;
    }

    @Override
    public Optional<AnnotationQueueItem> findById(String itemId) {
        var id = ValidationUtils.ensureNotBlank(itemId, "Queue item id");

        // Direct GET, so no scan: the server resolves the id within the parent queue. Only
        // LangfuseNotFoundException is caught - catching LangfuseApiException would report a 401 or a
        // 500 as "absent", which is the one mistake this layer must never make.
        try {
            return Optional.of(this.annotationQueuesApi.annotationQueuesGetQueueItem(APIAnnotationQueuesGetQueueItemRequest
                    .newBuilder()
                    .queueId(this.queueId)
                    .itemId(id)
                    .build()));
        } catch (LangfuseNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public AnnotationQueueItem create(CreateAnnotationQueueItemRequest request) {
        ValidationUtils.ensureNotNull(request, "request");

        return this.annotationQueuesApi.annotationQueuesCreateQueueItem(APIAnnotationQueuesCreateQueueItemRequest
                .newBuilder()
                .queueId(this.queueId)
                .createAnnotationQueueItemRequest(request)
                .build());
    }

    @Override
    public AnnotationQueueItem update(String itemId, UpdateAnnotationQueueItemRequest request) {
        var id = ValidationUtils.ensureNotBlank(itemId, "Queue item id");
        ValidationUtils.ensureNotNull(request, "request");

        return this.annotationQueuesApi.annotationQueuesUpdateQueueItem(APIAnnotationQueuesUpdateQueueItemRequest
                .newBuilder()
                .queueId(this.queueId)
                .itemId(id)
                .updateAnnotationQueueItemRequest(request)
                .build());
    }

    @Override
    public DeletionResult deleteById(Collection<String> itemIds) {
        // The engine stays keyed on a single String even though the endpoint needs two identifiers: the
        // queue id is fixed by this view, so the varying part is the item id alone and the delete
        // consumer closes over the parent. Widening the engine to multi-part identifiers would impose
        // this domain's shape on every other one.
        return Deletions.deleteAll(itemIds, "Queue item id", Optional::of, this::delete, deleteConcurrency());
    }

    private void delete(String itemId) {
        this.annotationQueuesApi.annotationQueuesDeleteQueueItem(APIAnnotationQueuesDeleteQueueItemRequest.newBuilder()
                .queueId(this.queueId)
                .itemId(itemId)
                .build());
    }

    private static PagedResult<AnnotationQueueItem> fetch(AnnotationQueuesApi annotationQueuesApi, String queueId, Page page) {
        var response = annotationQueuesApi.annotationQueuesListQueueItems(APIAnnotationQueuesListQueueItemsRequest
                .newBuilder()
                .queueId(queueId)
                .page(page.index())
                .limit(page.size())
                .build());

        return PagedResults.from(page, response.getData(), response.getMeta());
    }
}
