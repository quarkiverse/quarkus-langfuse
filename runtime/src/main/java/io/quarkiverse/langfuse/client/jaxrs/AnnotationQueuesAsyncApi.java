package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesCreateQueueAssignmentRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesCreateQueueItemRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesCreateQueueRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesDeleteQueueAssignmentRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesDeleteQueueItemRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesGetQueueItemRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesGetQueueRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesListQueueItemsRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesListQueuesRequest;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi.APIAnnotationQueuesUpdateQueueItemRequest;
import com.langfuse.api.model.AnnotationQueue;
import com.langfuse.api.model.AnnotationQueueItem;
import com.langfuse.api.model.CreateAnnotationQueueAssignmentResponse;
import com.langfuse.api.model.DeleteAnnotationQueueAssignmentResponse;
import com.langfuse.api.model.DeleteAnnotationQueueItemResponse;
import com.langfuse.api.model.PaginatedAnnotationQueueItems;
import com.langfuse.api.model.PaginatedAnnotationQueues;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "AnnotationQueues")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface AnnotationQueuesAsyncApi extends com.langfuse.api.annotationQueues.async.AnnotationQueuesApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesCreateQueue", method = "POST", path = "/api/public/annotation-queues")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/annotation-queues")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_createQueue")
    @Override
    public CompletionStage<AnnotationQueue> annotationQueuesCreateQueue(
            APIAnnotationQueuesCreateQueueRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesCreateQueueAssignment", method = "POST", path = "/api/public/annotation-queues/{queueId}/assignments")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/annotation-queues/{queueId}/assignments")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_createQueueAssignment")
    @Override
    public CompletionStage<CreateAnnotationQueueAssignmentResponse> annotationQueuesCreateQueueAssignment(
            APIAnnotationQueuesCreateQueueAssignmentRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesCreateQueueItem", method = "POST", path = "/api/public/annotation-queues/{queueId}/items")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/annotation-queues/{queueId}/items")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_createQueueItem")
    @Override
    public CompletionStage<AnnotationQueueItem> annotationQueuesCreateQueueItem(
            APIAnnotationQueuesCreateQueueItemRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesDeleteQueueAssignment", method = "DELETE", path = "/api/public/annotation-queues/{queueId}/assignments")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/annotation-queues/{queueId}/assignments")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_deleteQueueAssignment")
    @Override
    public CompletionStage<DeleteAnnotationQueueAssignmentResponse> annotationQueuesDeleteQueueAssignment(
            APIAnnotationQueuesDeleteQueueAssignmentRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesDeleteQueueItem", method = "DELETE", path = "/api/public/annotation-queues/{queueId}/items/{itemId}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/annotation-queues/{queueId}/items/{itemId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_deleteQueueItem")
    @Override
    public CompletionStage<DeleteAnnotationQueueItemResponse> annotationQueuesDeleteQueueItem(
            APIAnnotationQueuesDeleteQueueItemRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesGetQueue", method = "GET", path = "/api/public/annotation-queues/{queueId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/annotation-queues/{queueId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_getQueue")
    @Override
    public CompletionStage<AnnotationQueue> annotationQueuesGetQueue(
            APIAnnotationQueuesGetQueueRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesGetQueueItem", method = "GET", path = "/api/public/annotation-queues/{queueId}/items/{itemId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/annotation-queues/{queueId}/items/{itemId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_getQueueItem")
    @Override
    public CompletionStage<AnnotationQueueItem> annotationQueuesGetQueueItem(
            APIAnnotationQueuesGetQueueItemRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesListQueueItems", method = "GET", path = "/api/public/annotation-queues/{queueId}/items")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/annotation-queues/{queueId}/items")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_listQueueItems")
    @Override
    public CompletionStage<PaginatedAnnotationQueueItems> annotationQueuesListQueueItems(
            APIAnnotationQueuesListQueueItemsRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesListQueues", method = "GET", path = "/api/public/annotation-queues")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/annotation-queues")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_listQueues")
    @Override
    public CompletionStage<PaginatedAnnotationQueues> annotationQueuesListQueues(
            APIAnnotationQueuesListQueuesRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesUpdateQueueItem", method = "PATCH", path = "/api/public/annotation-queues/{queueId}/items/{itemId}")
    @jakarta.ws.rs.PATCH
    @jakarta.ws.rs.Path("/api/public/annotation-queues/{queueId}/items/{itemId}")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_updateQueueItem")
    @Override
    public CompletionStage<AnnotationQueueItem> annotationQueuesUpdateQueueItem(
            APIAnnotationQueuesUpdateQueueItemRequest apiRequest);

}
