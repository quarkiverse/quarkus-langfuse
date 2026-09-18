package io.quarkiverse.langfuse.api;

import java.util.Collection;

import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesCreateQueueItemRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesDeleteQueueItemRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesGetQueueItemRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesListQueueItemsRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesUpdateQueueItemRequest;
import com.langfuse.api.annotationQueues.async.AnnotationQueuesApi;
import com.langfuse.api.model.AnnotationQueueItem;
import com.langfuse.api.model.CreateAnnotationQueueItemRequest;
import com.langfuse.api.model.UpdateAnnotationQueueItemRequest;

import io.quarkiverse.langfuse.api.deletion.DeletionResult;
import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;
import io.smallrye.mutiny.Uni;

final class DefaultAsyncAnnotationQueueItemOperations extends AbstractAsyncPagedOperations<AnnotationQueueItem>
        implements AsyncAnnotationQueueItemOperations {
    private final AnnotationQueuesApi annotationQueuesApi;
    private final String queueId;

    // The parent queue id is captured in the fetcher lambda handed to the engine, which is why a
    // parent-scoped collection needs neither a widened AsyncPageFetcher nor a change to AsyncPagination:
    // this view is just the same class constructed over a different fetcher. The id is already validated
    // by the accessor that builds this view.
    DefaultAsyncAnnotationQueueItemOperations(AnnotationQueuesApi annotationQueuesApi, LangfuseConfig config,
            String queueId) {
        super(page -> fetch(annotationQueuesApi, queueId, page), config);
        this.annotationQueuesApi = annotationQueuesApi;
        this.queueId = queueId;
    }

    @Override
    public Uni<AnnotationQueueItem> findById(String itemId) {
        // Validated here rather than inside the completionStage supplier: a throw in there becomes a
        // failure event, and blank input must surface as a thrown IllegalArgumentException.
        var id = ValidationUtils.ensureNotBlank(itemId, "Queue item id");

        // Direct GET, so no scan. recoverWithNull is scoped to LangfuseNotFoundException alone: every
        // other failure, a 401 included, must still fail the Uni rather than read as absence.
        return Uni.createFrom()
                .completionStage(() -> this.annotationQueuesApi
                        .annotationQueuesGetQueueItem(APIAnnotationQueuesGetQueueItemRequest.newBuilder()
                                .queueId(this.queueId)
                                .itemId(id)
                                .build()))
                .onFailure(LangfuseNotFoundException.class)
                .recoverWithNull();
    }

    @Override
    public Uni<AnnotationQueueItem> create(CreateAnnotationQueueItemRequest request) {
        ValidationUtils.ensureNotNull(request, "request");

        return Uni.createFrom()
                .completionStage(() -> this.annotationQueuesApi
                        .annotationQueuesCreateQueueItem(APIAnnotationQueuesCreateQueueItemRequest.newBuilder()
                                .queueId(this.queueId)
                                .createAnnotationQueueItemRequest(request)
                                .build()));
    }

    @Override
    public Uni<AnnotationQueueItem> update(String itemId, UpdateAnnotationQueueItemRequest request) {
        var id = ValidationUtils.ensureNotBlank(itemId, "Queue item id");
        ValidationUtils.ensureNotNull(request, "request");

        return Uni.createFrom()
                .completionStage(() -> this.annotationQueuesApi
                        .annotationQueuesUpdateQueueItem(APIAnnotationQueuesUpdateQueueItemRequest.newBuilder()
                                .queueId(this.queueId)
                                .itemId(id)
                                .updateAnnotationQueueItemRequest(request)
                                .build()));
    }

    @Override
    public Uni<DeletionResult> deleteById(Collection<String> itemIds) {
        // Validation runs before the deferred wrapper so malformed input throws from the call, as the
        // rest of this tree does. Inside the supplier it would surface as a failed Uni at subscription
        // instead. deleteConcurrency() stays inside, so the config is read per subscription.
        //
        // The engine stays keyed on a single String even though the endpoint needs two identifiers: the
        // queue id is fixed by this view, so the varying part is the item id alone and the delete
        // function closes over the parent.
        DeletionIdentifiers.validated(itemIds, "Queue item id");

        return Uni.createFrom()
                .deferred(() -> AsyncDeletions.deleteAll(itemIds, "Queue item id", Uni.createFrom()::item, this::delete,
                        deleteConcurrency()));
    }

    private Uni<?> delete(String itemId) {
        return Uni.createFrom()
                .completionStage(() -> this.annotationQueuesApi
                        .annotationQueuesDeleteQueueItem(APIAnnotationQueuesDeleteQueueItemRequest.newBuilder()
                                .queueId(this.queueId)
                                .itemId(itemId)
                                .build()));
    }

    private static Uni<PagedResult<AnnotationQueueItem>> fetch(AnnotationQueuesApi annotationQueuesApi, String queueId,
            Page page) {
        return Uni.createFrom()
                .completionStage(() -> annotationQueuesApi
                        .annotationQueuesListQueueItems(APIAnnotationQueuesListQueueItemsRequest.newBuilder()
                                .queueId(queueId)
                                .page(page.index())
                                .limit(page.size())
                                .build()))
                .map(response -> PagedResults.from(page, response.getData(), response.getMeta()));
    }
}
