package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

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
import com.langfuse.api.unstableEvaluators.UnstableEvaluatorsApi.APIUnstableEvaluatorsCreateRequest;
import com.langfuse.api.unstableEvaluators.UnstableEvaluatorsApi.APIUnstableEvaluatorsDeleteRequest;
import com.langfuse.api.unstableEvaluators.UnstableEvaluatorsApi.APIUnstableEvaluatorsGetRequest;
import com.langfuse.api.unstableEvaluators.UnstableEvaluatorsApi.APIUnstableEvaluatorsListRequest;

public interface QuarkusUnstableEvaluatorsAsyncApi extends com.langfuse.api.unstableEvaluators.async.UnstableEvaluatorsApi {

    /**
     * Create an evaluator in the authenticated project.
     */
    @POST
    @Path("/api/public/unstable/evaluators")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<UnstableEvaluator> unstableEvaluatorsCreate(
            UnstableCreateEvaluatorRequest unstableCreateEvaluatorRequest);

    /**
     * Create an evaluator in the authenticated project.
     */
    @Override
    default CompletionStage<UnstableEvaluator> unstableEvaluatorsCreate(
            APIUnstableEvaluatorsCreateRequest apiRequest) {
        return unstableEvaluatorsCreate(apiRequest.unstableCreateEvaluatorRequest());
    }

    /**
     * Delete an evaluator.
     */
    @DELETE
    @Path("/api/public/unstable/evaluators/{evaluatorId}")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<UnstableDeleteEvaluatorResponse> unstableEvaluatorsDelete(
            @PathParam("evaluatorId") String evaluatorId);

    /**
     * Delete an evaluator.
     */
    @Override
    default CompletionStage<UnstableDeleteEvaluatorResponse> unstableEvaluatorsDelete(
            APIUnstableEvaluatorsDeleteRequest apiRequest) {
        return unstableEvaluatorsDelete(apiRequest.evaluatorId());
    }

    /**
     * Get one evaluator by id.
     */
    @GET
    @Path("/api/public/unstable/evaluators/{evaluatorId}")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<UnstableEvaluator> unstableEvaluatorsGet(
            @PathParam("evaluatorId") String evaluatorId);

    /**
     * Get one evaluator by id.
     */
    @Override
    default CompletionStage<UnstableEvaluator> unstableEvaluatorsGet(APIUnstableEvaluatorsGetRequest apiRequest) {
        return unstableEvaluatorsGet(apiRequest.evaluatorId());
    }

    /**
     * List the evaluators available to the authenticated project.
     */
    @GET
    @Path("/api/public/unstable/evaluators")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<UnstableEvaluators> unstableEvaluatorsList(
            @QueryParam("page") Integer page,
            @QueryParam("limit") Integer limit);

    /**
     * List the evaluators available to the authenticated project.
     */
    @Override
    default CompletionStage<UnstableEvaluators> unstableEvaluatorsList(APIUnstableEvaluatorsListRequest apiRequest) {
        return unstableEvaluatorsList(apiRequest.page(), apiRequest.limit());
    }

}
