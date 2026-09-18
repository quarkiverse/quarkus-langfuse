package io.quarkiverse.langfuse.api;

import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesCreateQueueRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesGetQueueRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesListQueuesRequest;
import com.langfuse.api.annotationQueues.async.AnnotationQueuesApi;
import com.langfuse.api.model.AnnotationQueue;
import com.langfuse.api.model.CreateAnnotationQueueRequest;

import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;
import io.smallrye.mutiny.Uni;

final class DefaultAsyncAnnotationQueueOperations extends AbstractAsyncPagedOperations<AnnotationQueue>
        implements AsyncAnnotationQueueOperations {
    private final AnnotationQueuesApi annotationQueuesApi;
    private final LangfuseConfig config;

    DefaultAsyncAnnotationQueueOperations(AnnotationQueuesApi annotationQueuesApi, LangfuseConfig config) {
        super(page -> fetch(annotationQueuesApi, page), config);
        this.annotationQueuesApi = annotationQueuesApi;
        this.config = config;
    }

    @Override
    public AsyncAnnotationQueueItemOperations items(String queueId) {
        // Validated here, outside any deferred supplier, so an invalid parent throws from items("")
        // rather than surfacing as a failure event on a later traversal.
        return new DefaultAsyncAnnotationQueueItemOperations(this.annotationQueuesApi, this.config,
                ValidationUtils.ensureNotBlank(queueId, "Queue id"));
    }

    @Override
    public Uni<AnnotationQueue> findById(String id) {
        // Validated here rather than inside the completionStage supplier: a throw in there becomes a
        // failure event, and blank input must surface as a thrown IllegalArgumentException.
        var queueId = ValidationUtils.ensureNotBlank(id, "Annotation queue id");

        // Direct GET, so no scan. recoverWithNull is scoped to LangfuseNotFoundException alone: every
        // other failure, a 401 included, must still fail the Uni rather than read as absence.
        return Uni.createFrom()
                .completionStage(() -> this.annotationQueuesApi
                        .annotationQueuesGetQueue(APIAnnotationQueuesGetQueueRequest.newBuilder()
                                .queueId(queueId)
                                .build()))
                .onFailure(LangfuseNotFoundException.class)
                .recoverWithNull();
    }

    @Override
    public Uni<AnnotationQueue> findByName(String queueName) {
        return scanForName(ValidationUtils.ensureNotBlank(queueName, "Annotation queue name"), AnnotationQueue::getName);
    }

    @Override
    public Uni<AnnotationQueue> createIfAbsent(CreateAnnotationQueueRequest request) {
        // See DefaultAsyncModelOperations.createIfAbsent for why flatMap + ternary rather than
        // onItem().ifNull().switchTo(). Not atomic: a lookup followed by a create, not one request.
        return findByName(request.getName())
                .flatMap(existing -> (existing != null)
                        ? Uni.createFrom().item(existing)
                        : Uni.createFrom()
                                .completionStage(() -> this.annotationQueuesApi
                                        .annotationQueuesCreateQueue(APIAnnotationQueuesCreateQueueRequest.newBuilder()
                                                .createAnnotationQueueRequest(request)
                                                .build())));
    }

    private static Uni<PagedResult<AnnotationQueue>> fetch(AnnotationQueuesApi annotationQueuesApi, Page page) {
        return Uni.createFrom()
                .completionStage(() -> annotationQueuesApi
                        .annotationQueuesListQueues(APIAnnotationQueuesListQueuesRequest.newBuilder()
                                .page(page.index())
                                .limit(page.size())
                                .build()))
                .map(response -> PagedResults.from(page, response.getData(), response.getMeta()));
    }
}
