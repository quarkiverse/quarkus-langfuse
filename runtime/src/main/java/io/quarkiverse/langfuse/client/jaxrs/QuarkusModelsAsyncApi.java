package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.model.CreateModelRequest;
import com.langfuse.api.model.Model;
import com.langfuse.api.model.PaginatedModels;
import com.langfuse.api.models.ModelsApi.APIModelsCreateRequest;
import com.langfuse.api.models.ModelsApi.APIModelsDeleteRequest;
import com.langfuse.api.models.ModelsApi.APIModelsGetRequest;
import com.langfuse.api.models.ModelsApi.APIModelsListRequest;
import com.langfuse.api.models.ModelsApi.APIModelsUpsertRequest;

/**
 * Langfuse Models Async API
 */
public interface QuarkusModelsAsyncApi extends com.langfuse.api.models.async.ModelsApi {

    /**
     * Create a model
     */
    @POST
    @Path("/api/public/models")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<Model> modelsCreate(
            CreateModelRequest createModelRequest);

    /**
     * Create a model
     */
    @Override
    default CompletionStage<Model> modelsCreate(APIModelsCreateRequest apiRequest) {
        return modelsCreate(apiRequest.createModelRequest());
    }

    /**
     * Delete a model
     */
    @DELETE
    @Path("/api/public/models/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<Void> modelsDelete(
            @PathParam("id") String id);

    /**
     * Delete a model
     */
    @Override
    default CompletionStage<Void> modelsDelete(APIModelsDeleteRequest apiRequest) {
        return modelsDelete(apiRequest.id());
    }

    /**
     * Get a model
     */
    @GET
    @Path("/api/public/models/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<Model> modelsGet(
            @PathParam("id") String id);

    /**
     * Get a model
     */
    @Override
    default CompletionStage<Model> modelsGet(APIModelsGetRequest apiRequest) {
        return modelsGet(apiRequest.id());
    }

    /**
     * Get all models
     */
    @GET
    @Path("/api/public/models")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<PaginatedModels> modelsList(
            @QueryParam("page") Integer page,
            @QueryParam("limit") Integer limit);

    /**
     * Get all models
     */
    @Override
    default CompletionStage<PaginatedModels> modelsList(APIModelsListRequest apiRequest) {
        return modelsList(apiRequest.page(), apiRequest.limit());
    }

    /**
     * Create or replace a project-owned model using its id
     */
    @PUT
    @Path("/api/public/models/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<Model> modelsUpsert(
            @PathParam("id") String id,
            CreateModelRequest createModelRequest);

    /**
     * Create or replace a project-owned model using its id
     */
    @Override
    default CompletionStage<Model> modelsUpsert(APIModelsUpsertRequest apiRequest) {
        return modelsUpsert(apiRequest.id(), apiRequest.createModelRequest());
    }

}
