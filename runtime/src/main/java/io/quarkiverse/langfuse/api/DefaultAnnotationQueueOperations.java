package io.quarkiverse.langfuse.api;

import java.util.Optional;

import com.langfuse.api.annotationQueues.AnnotationQueuesApi;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesCreateQueueRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesGetQueueRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesListQueuesRequest;
import com.langfuse.api.model.AnnotationQueue;
import com.langfuse.api.model.CreateAnnotationQueueRequest;

import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;

final class DefaultAnnotationQueueOperations extends AbstractPagedOperations<AnnotationQueue>
        implements AnnotationQueueOperations {
    private final AnnotationQueuesApi annotationQueuesApi;
    private final LangfuseConfig config;

    DefaultAnnotationQueueOperations(AnnotationQueuesApi annotationQueuesApi, LangfuseConfig config) {
        super(page -> fetch(annotationQueuesApi, page), config);
        this.annotationQueuesApi = annotationQueuesApi;
        this.config = config;
    }

    @Override
    public AnnotationQueueItemOperations items(String queueId) {
        // Validated here rather than in the item operations, so an invalid parent fails at items("")
        // instead of later at the first traversal.
        return new DefaultAnnotationQueueItemOperations(this.annotationQueuesApi, this.config,
                ValidationUtils.ensureNotBlank(queueId, "Queue id"));
    }

    @Override
    public Optional<AnnotationQueue> findById(String id) {
        var queueId = ValidationUtils.ensureNotBlank(id, "Annotation queue id");

        // Direct GET, so no scan: the server resolves the id. Only LangfuseNotFoundException is caught -
        // catching LangfuseApiException would report a 401 or a 500 as "absent", which is the one
        // mistake this layer must never make.
        try {
            return Optional.of(this.annotationQueuesApi.annotationQueuesGetQueue(APIAnnotationQueuesGetQueueRequest
                    .newBuilder()
                    .queueId(queueId)
                    .build()));
        } catch (LangfuseNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<AnnotationQueue> findByName(String queueName) {
        return scanForName(ValidationUtils.ensureNotBlank(queueName, "Annotation queue name"), AnnotationQueue::getName);
    }

    @Override
    public AnnotationQueue createIfAbsent(CreateAnnotationQueueRequest request) {
        return findByName(request.getName())
                .orElseGet(() -> this.annotationQueuesApi
                        .annotationQueuesCreateQueue(APIAnnotationQueuesCreateQueueRequest.newBuilder()
                                .createAnnotationQueueRequest(request)
                                .build()));
    }

    private static PagedResult<AnnotationQueue> fetch(AnnotationQueuesApi annotationQueuesApi, Page page) {
        var response = annotationQueuesApi.annotationQueuesListQueues(APIAnnotationQueuesListQueuesRequest.newBuilder()
                .page(page.index())
                .limit(page.size())
                .build());

        return PagedResults.from(page, response.getData(), response.getMeta());
    }
}
