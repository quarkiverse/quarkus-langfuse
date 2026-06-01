package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.model.AnnotationQueue;
import com.langfuse.api.model.AnnotationQueueItem;
import com.langfuse.api.model.CreateAnnotationQueueAssignmentResponse;
import com.langfuse.api.model.DeleteAnnotationQueueAssignmentResponse;
import com.langfuse.api.model.DeleteAnnotationQueueItemResponse;
import com.langfuse.api.model.PaginatedAnnotationQueueItems;
import com.langfuse.api.model.PaginatedAnnotationQueues;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "AnnotationQueues")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface AnnotationQueuesApi extends com.langfuse.api.annotationQueues.AnnotationQueuesApi {

    /**
     * Create an annotation queue
     *
     * @param apiRequest {@link APIAnnotationQueuesCreateQueueRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesCreateQueue", method = "POST", path = "/api/public/annotation-queues")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/annotation-queues")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_createQueue")
    @Override
    public AnnotationQueue annotationQueuesCreateQueue(
            APIAnnotationQueuesCreateQueueRequest apiRequest);

    /**
     * Create an assignment for a user to an annotation queue
     *
     * @param apiRequest {@link APIAnnotationQueuesCreateQueueAssignmentRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesCreateQueueAssignment", method = "POST", path = "/api/public/annotation-queues/{queueId}/assignments")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/annotation-queues/{queueId}/assignments")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_createQueueAssignment")
    @Override
    public CreateAnnotationQueueAssignmentResponse annotationQueuesCreateQueueAssignment(
            APIAnnotationQueuesCreateQueueAssignmentRequest apiRequest);

    /**
     * Add an item to an annotation queue
     *
     * @param apiRequest {@link APIAnnotationQueuesCreateQueueItemRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesCreateQueueItem", method = "POST", path = "/api/public/annotation-queues/{queueId}/items")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/annotation-queues/{queueId}/items")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_createQueueItem")
    @Override
    public AnnotationQueueItem annotationQueuesCreateQueueItem(
            APIAnnotationQueuesCreateQueueItemRequest apiRequest);

    /**
     * Delete an assignment for a user to an annotation queue
     *
     * @param apiRequest {@link APIAnnotationQueuesDeleteQueueAssignmentRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesDeleteQueueAssignment", method = "DELETE", path = "/api/public/annotation-queues/{queueId}/assignments")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/annotation-queues/{queueId}/assignments")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_deleteQueueAssignment")
    @Override
    public DeleteAnnotationQueueAssignmentResponse annotationQueuesDeleteQueueAssignment(
            APIAnnotationQueuesDeleteQueueAssignmentRequest apiRequest);

    /**
     * Remove an item from an annotation queue
     *
     * @param apiRequest {@link APIAnnotationQueuesDeleteQueueItemRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesDeleteQueueItem", method = "DELETE", path = "/api/public/annotation-queues/{queueId}/items/{itemId}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/annotation-queues/{queueId}/items/{itemId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_deleteQueueItem")
    @Override
    public DeleteAnnotationQueueItemResponse annotationQueuesDeleteQueueItem(
            APIAnnotationQueuesDeleteQueueItemRequest apiRequest);

    /**
     * Get an annotation queue by ID
     *
     * @param apiRequest {@link APIAnnotationQueuesGetQueueRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesGetQueue", method = "GET", path = "/api/public/annotation-queues/{queueId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/annotation-queues/{queueId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_getQueue")
    @Override
    public AnnotationQueue annotationQueuesGetQueue(
            APIAnnotationQueuesGetQueueRequest apiRequest);

    /**
     * Get a specific item from an annotation queue
     *
     * @param apiRequest {@link APIAnnotationQueuesGetQueueItemRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesGetQueueItem", method = "GET", path = "/api/public/annotation-queues/{queueId}/items/{itemId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/annotation-queues/{queueId}/items/{itemId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_getQueueItem")
    @Override
    public AnnotationQueueItem annotationQueuesGetQueueItem(
            APIAnnotationQueuesGetQueueItemRequest apiRequest);

    /**
     * Get items for a specific annotation queue
     *
     * @param apiRequest {@link APIAnnotationQueuesListQueueItemsRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesListQueueItems", method = "GET", path = "/api/public/annotation-queues/{queueId}/items")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/annotation-queues/{queueId}/items")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_listQueueItems")
    @Override
    public PaginatedAnnotationQueueItems annotationQueuesListQueueItems(
            APIAnnotationQueuesListQueueItemsRequest apiRequest);

    /**
     * Get all annotation queues
     *
     * @param apiRequest {@link APIAnnotationQueuesListQueuesRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesListQueues", method = "GET", path = "/api/public/annotation-queues")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/annotation-queues")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_listQueues")
    @Override
    public PaginatedAnnotationQueues annotationQueuesListQueues(
            APIAnnotationQueuesListQueuesRequest apiRequest);

    /**
     * Update an annotation queue item
     *
     * @param apiRequest {@link APIAnnotationQueuesUpdateQueueItemRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "annotationQueuesUpdateQueueItem", method = "PATCH", path = "/api/public/annotation-queues/{queueId}/items/{itemId}")
    @jakarta.ws.rs.PATCH
    @jakarta.ws.rs.Path("/api/public/annotation-queues/{queueId}/items/{itemId}")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("annotationQueues_updateQueueItem")
    @Override
    public AnnotationQueueItem annotationQueuesUpdateQueueItem(
            APIAnnotationQueuesUpdateQueueItemRequest apiRequest);

}
