package io.quarkiverse.langfuse.client.jaxrs;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.model.UnstableCreateEvaluatorRequest;
import com.langfuse.api.model.UnstableDeleteEvaluatorResponse;
import com.langfuse.api.model.UnstableEvaluator;
import com.langfuse.api.model.UnstableEvaluators;

/**
 * Langfuse Unstable Evaluators API
 */
public interface QuarkusUnstableEvaluatorsApi extends com.langfuse.api.unstableEvaluators.UnstableEvaluatorsApi {

    /**
     * Create an evaluator in the authenticated project.
     */
    @POST
    @Path("/api/public/unstable/evaluators")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    UnstableEvaluator unstableEvaluatorsCreate(
            UnstableCreateEvaluatorRequest unstableCreateEvaluatorRequest);

    /**
     * Create an evaluator in the authenticated project.
     */
    @Override
    default UnstableEvaluator unstableEvaluatorsCreate(APIUnstableEvaluatorsCreateRequest apiRequest) {
        return unstableEvaluatorsCreate(apiRequest.unstableCreateEvaluatorRequest());
    }

    /**
     * Delete an evaluator.
     */
    @DELETE
    @Path("/api/public/unstable/evaluators/{evaluatorId}")
    @Produces(MediaType.APPLICATION_JSON)
    UnstableDeleteEvaluatorResponse unstableEvaluatorsDelete(
            @PathParam("evaluatorId") String evaluatorId);

    /**
     * Delete an evaluator.
     */
    @Override
    default UnstableDeleteEvaluatorResponse unstableEvaluatorsDelete(
            APIUnstableEvaluatorsDeleteRequest apiRequest) {
        return unstableEvaluatorsDelete(apiRequest.evaluatorId());
    }

    /**
     * Get one evaluator by id.
     */
    @GET
    @Path("/api/public/unstable/evaluators/{evaluatorId}")
    @Produces(MediaType.APPLICATION_JSON)
    UnstableEvaluator unstableEvaluatorsGet(
            @PathParam("evaluatorId") String evaluatorId);

    /**
     * Get one evaluator by id.
     */
    @Override
    default UnstableEvaluator unstableEvaluatorsGet(APIUnstableEvaluatorsGetRequest apiRequest) {
        return unstableEvaluatorsGet(apiRequest.evaluatorId());
    }

    /**
     * List the evaluators available to the authenticated project.
     */
    @GET
    @Path("/api/public/unstable/evaluators")
    @Produces(MediaType.APPLICATION_JSON)
    UnstableEvaluators unstableEvaluatorsList(
            @QueryParam("page") Integer page,
            @QueryParam("limit") Integer limit);

    /**
     * List the evaluators available to the authenticated project.
     */
    @Override
    default UnstableEvaluators unstableEvaluatorsList(APIUnstableEvaluatorsListRequest apiRequest) {
        return unstableEvaluatorsList(apiRequest.page(), apiRequest.limit());
    }

}
