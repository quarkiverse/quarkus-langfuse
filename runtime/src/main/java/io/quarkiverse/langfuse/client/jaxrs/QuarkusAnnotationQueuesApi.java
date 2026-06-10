package io.quarkiverse.langfuse.client.jaxrs;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

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
import com.langfuse.api.model.AnnotationQueueAssignmentRequest;
import com.langfuse.api.model.AnnotationQueueItem;
import com.langfuse.api.model.AnnotationQueueStatus;
import com.langfuse.api.model.CreateAnnotationQueueAssignmentResponse;
import com.langfuse.api.model.CreateAnnotationQueueItemRequest;
import com.langfuse.api.model.CreateAnnotationQueueRequest;
import com.langfuse.api.model.DeleteAnnotationQueueAssignmentResponse;
import com.langfuse.api.model.DeleteAnnotationQueueItemResponse;
import com.langfuse.api.model.PaginatedAnnotationQueueItems;
import com.langfuse.api.model.PaginatedAnnotationQueues;
import com.langfuse.api.model.UpdateAnnotationQueueItemRequest;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - publicKey: Langfuse Public Key - secretKey: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
public interface QuarkusAnnotationQueuesApi extends com.langfuse.api.annotationQueues.AnnotationQueuesApi {

    /**
     * Create an annotation queue
     */
    @POST
    @Path("/api/public/annotation-queues")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    AnnotationQueue annotationQueuesCreateQueue(
            CreateAnnotationQueueRequest createAnnotationQueueRequest);

    /**
     * Create an annotation queue
     */
    @Override
    default AnnotationQueue annotationQueuesCreateQueue(APIAnnotationQueuesCreateQueueRequest apiRequest) {
        return annotationQueuesCreateQueue(apiRequest.createAnnotationQueueRequest());
    }

    /**
     * Create an assignment for a user to an annotation queue
     */
    @POST
    @Path("/api/public/annotation-queues/{queueId}/assignments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CreateAnnotationQueueAssignmentResponse annotationQueuesCreateQueueAssignment(
            @PathParam("queueId") String queueId,
            AnnotationQueueAssignmentRequest annotationQueueAssignmentRequest);

    /**
     * Create an assignment for a user to an annotation queue
     */
    @Override
    default CreateAnnotationQueueAssignmentResponse annotationQueuesCreateQueueAssignment(
            APIAnnotationQueuesCreateQueueAssignmentRequest apiRequest) {
        return annotationQueuesCreateQueueAssignment(apiRequest.queueId(), apiRequest.annotationQueueAssignmentRequest());
    }

    /**
     * Add an item to an annotation queue
     */
    @POST
    @Path("/api/public/annotation-queues/{queueId}/items")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    AnnotationQueueItem annotationQueuesCreateQueueItem(
            @PathParam("queueId") String queueId,
            CreateAnnotationQueueItemRequest createAnnotationQueueItemRequest);

    /**
     * Add an item to an annotation queue
     */
    @Override
    default AnnotationQueueItem annotationQueuesCreateQueueItem(APIAnnotationQueuesCreateQueueItemRequest apiRequest) {
        return annotationQueuesCreateQueueItem(apiRequest.queueId(), apiRequest.createAnnotationQueueItemRequest());
    }

    /**
     * Delete an assignment for a user to an annotation queue
     */
    @DELETE
    @Path("/api/public/annotation-queues/{queueId}/assignments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    DeleteAnnotationQueueAssignmentResponse annotationQueuesDeleteQueueAssignment(
            @PathParam("queueId") String queueId,
            AnnotationQueueAssignmentRequest annotationQueueAssignmentRequest);

    /**
     * Delete an assignment for a user to an annotation queue
     */
    @Override
    default DeleteAnnotationQueueAssignmentResponse annotationQueuesDeleteQueueAssignment(
            APIAnnotationQueuesDeleteQueueAssignmentRequest apiRequest) {
        return annotationQueuesDeleteQueueAssignment(apiRequest.queueId(), apiRequest.annotationQueueAssignmentRequest());
    }

    /**
     * Remove an item from an annotation queue
     */
    @DELETE
    @Path("/api/public/annotation-queues/{queueId}/items/{itemId}")
    @Produces(MediaType.APPLICATION_JSON)
    DeleteAnnotationQueueItemResponse annotationQueuesDeleteQueueItem(
            @PathParam("queueId") String queueId,
            @PathParam("itemId") String itemId);

    /**
     * Remove an item from an annotation queue
     */
    @Override
    default DeleteAnnotationQueueItemResponse annotationQueuesDeleteQueueItem(
            APIAnnotationQueuesDeleteQueueItemRequest apiRequest) {
        return annotationQueuesDeleteQueueItem(apiRequest.queueId(), apiRequest.itemId());
    }

    /**
     * Get an annotation queue by ID
     */
    @GET
    @Path("/api/public/annotation-queues/{queueId}")
    @Produces(MediaType.APPLICATION_JSON)
    AnnotationQueue annotationQueuesGetQueue(
            @PathParam("queueId") String queueId);

    /**
     * Get an annotation queue by ID
     */
    @Override
    default AnnotationQueue annotationQueuesGetQueue(APIAnnotationQueuesGetQueueRequest apiRequest) {
        return annotationQueuesGetQueue(apiRequest.queueId());
    }

    /**
     * Get a specific item from an annotation queue
     */
    @GET
    @Path("/api/public/annotation-queues/{queueId}/items/{itemId}")
    @Produces(MediaType.APPLICATION_JSON)
    AnnotationQueueItem annotationQueuesGetQueueItem(
            @PathParam("queueId") String queueId,
            @PathParam("itemId") String itemId);

    /**
     * Get a specific item from an annotation queue
     */
    @Override
    default AnnotationQueueItem annotationQueuesGetQueueItem(APIAnnotationQueuesGetQueueItemRequest apiRequest) {
        return annotationQueuesGetQueueItem(apiRequest.queueId(), apiRequest.itemId());
    }

    /**
     * Get items for a specific annotation queue
     */
    @GET
    @Path("/api/public/annotation-queues/{queueId}/items")
    @Produces(MediaType.APPLICATION_JSON)
    PaginatedAnnotationQueueItems annotationQueuesListQueueItems(
            @PathParam("queueId") String queueId,
            @QueryParam("status") AnnotationQueueStatus status,
            @QueryParam("page") Integer page,
            @QueryParam("limit") Integer limit);

    /**
     * Get items for a specific annotation queue
     */
    @Override
    default PaginatedAnnotationQueueItems annotationQueuesListQueueItems(
            APIAnnotationQueuesListQueueItemsRequest apiRequest) {
        return annotationQueuesListQueueItems(apiRequest.queueId(), apiRequest.status(), apiRequest.page(),
                apiRequest.limit());
    }

    /**
     * Get all annotation queues
     */
    @GET
    @Path("/api/public/annotation-queues")
    @Produces(MediaType.APPLICATION_JSON)
    PaginatedAnnotationQueues annotationQueuesListQueues(
            @QueryParam("page") Integer page,
            @QueryParam("limit") Integer limit);

    /**
     * Get all annotation queues
     */
    @Override
    default PaginatedAnnotationQueues annotationQueuesListQueues(APIAnnotationQueuesListQueuesRequest apiRequest) {
        return annotationQueuesListQueues(apiRequest.page(), apiRequest.limit());
    }

    /**
     * Update an annotation queue item
     */
    @PATCH
    @Path("/api/public/annotation-queues/{queueId}/items/{itemId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    AnnotationQueueItem annotationQueuesUpdateQueueItem(
            @PathParam("queueId") String queueId,
            @PathParam("itemId") String itemId,
            UpdateAnnotationQueueItemRequest updateAnnotationQueueItemRequest);

    /**
     * Update an annotation queue item
     */
    @Override
    default AnnotationQueueItem annotationQueuesUpdateQueueItem(APIAnnotationQueuesUpdateQueueItemRequest apiRequest) {
        return annotationQueuesUpdateQueueItem(apiRequest.queueId(), apiRequest.itemId(),
                apiRequest.updateAnnotationQueueItemRequest());
    }

}
