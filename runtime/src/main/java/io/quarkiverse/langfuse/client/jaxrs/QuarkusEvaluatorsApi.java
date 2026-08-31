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

import com.langfuse.api.model.CreateEvaluatorRequest;
import com.langfuse.api.model.DeletedEvaluator;
import com.langfuse.api.model.Evaluator;
import com.langfuse.api.model.EvaluatorVersionsPage;
import com.langfuse.api.model.EvaluatorsPage;
import com.langfuse.api.model.UpdateEvaluatorRequest;

/**
 * Langfuse Evaluators API
 */
public interface QuarkusEvaluatorsApi extends com.langfuse.api.evaluators.EvaluatorsApi {

    /**
     * Create an evaluator in the authenticated project.
     */
    @POST
    @Path("/api/public/v2/evaluators")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Evaluator evaluatorsCreate(
            CreateEvaluatorRequest createEvaluatorRequest);

    /**
     * Create an evaluator in the authenticated project.
     */
    @Override
    default Evaluator evaluatorsCreate(APIEvaluatorsCreateRequest apiRequest) {
        return evaluatorsCreate(apiRequest.createEvaluatorRequest());
    }

    /**
     * Delete an evaluator and all of its stored versions.
     */
    @DELETE
    @Path("/api/public/v2/evaluators/{evaluatorId}")
    @Produces(MediaType.APPLICATION_JSON)
    DeletedEvaluator evaluatorsDelete(
            @PathParam("evaluatorId") String evaluatorId);

    /**
     * Delete an evaluator and all of its stored versions.
     */
    @Override
    default DeletedEvaluator evaluatorsDelete(APIEvaluatorsDeleteRequest apiRequest) {
        return evaluatorsDelete(apiRequest.evaluatorId());
    }

    /**
     * Get one evaluator by its stable identifier.
     */
    @GET
    @Path("/api/public/v2/evaluators/{evaluatorId}")
    @Produces(MediaType.APPLICATION_JSON)
    Evaluator evaluatorsGet(
            @PathParam("evaluatorId") String evaluatorId);

    /**
     * Get one evaluator by its stable identifier.
     */
    @Override
    default Evaluator evaluatorsGet(APIEvaluatorsGetRequest apiRequest) {
        return evaluatorsGet(apiRequest.evaluatorId());
    }

    /**
     * List evaluators in newest-first creation order.
     */
    @GET
    @Path("/api/public/v2/evaluators")
    @Produces(MediaType.APPLICATION_JSON)
    EvaluatorsPage evaluatorsList(
            @QueryParam("limit") Integer limit,
            @QueryParam("cursor") String cursor);

    /**
     * List evaluators in newest-first creation order.
     */
    @Override
    default EvaluatorsPage evaluatorsList(APIEvaluatorsListRequest apiRequest) {
        return evaluatorsList(apiRequest.limit(), apiRequest.cursor());
    }

    /**
     * List an evaluator's version history in newest-first order.
     */
    @GET
    @Path("/api/public/v2/evaluators/{evaluatorId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    EvaluatorVersionsPage evaluatorsListVersions(
            @PathParam("evaluatorId") String evaluatorId,
            @QueryParam("limit") Integer limit,
            @QueryParam("cursor") String cursor);

    /**
     * List an evaluator's version history in newest-first order.
     */
    @Override
    default EvaluatorVersionsPage evaluatorsListVersions(APIEvaluatorsListVersionsRequest apiRequest) {
        return evaluatorsListVersions(apiRequest.evaluatorId(), apiRequest.limit(), apiRequest.cursor());
    }

    /**
     * Update an evaluator by its stable identifier.
     */
    @PATCH
    @Path("/api/public/v2/evaluators/{evaluatorId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Evaluator evaluatorsUpdate(
            @PathParam("evaluatorId") String evaluatorId,
            UpdateEvaluatorRequest updateEvaluatorRequest);

    /**
     * Update an evaluator by its stable identifier.
     */
    @Override
    default Evaluator evaluatorsUpdate(APIEvaluatorsUpdateRequest apiRequest) {
        return evaluatorsUpdate(apiRequest.evaluatorId(), apiRequest.updateEvaluatorRequest());
    }

}
