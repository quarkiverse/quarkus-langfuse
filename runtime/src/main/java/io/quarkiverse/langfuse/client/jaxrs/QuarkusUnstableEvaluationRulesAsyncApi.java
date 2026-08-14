package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

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

import com.langfuse.api.model.UnstableCreateEvaluationRuleRequest;
import com.langfuse.api.model.UnstableDeleteEvaluationRuleResponse;
import com.langfuse.api.model.UnstableEvaluationRule;
import com.langfuse.api.model.UnstableEvaluationRules;
import com.langfuse.api.model.UnstableReadableEvaluationRule;
import com.langfuse.api.model.UnstableUpdateEvaluationRuleRequest;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesCreateRequest;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesDeleteRequest;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesGetRequest;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesListRequest;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesUpdateRequest;

public interface QuarkusUnstableEvaluationRulesAsyncApi
        extends com.langfuse.api.unstableEvaluationRules.async.UnstableEvaluationRulesApi {

    /**
     * Create an evaluation rule.
     */
    @POST
    @Path("/api/public/unstable/evaluation-rules")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<UnstableEvaluationRule> unstableEvaluationRulesCreate(
            UnstableCreateEvaluationRuleRequest unstableCreateEvaluationRuleRequest);

    /**
     * Create an evaluation rule.
     */
    @Override
    default CompletionStage<UnstableEvaluationRule> unstableEvaluationRulesCreate(
            APIUnstableEvaluationRulesCreateRequest apiRequest) {
        return unstableEvaluationRulesCreate(apiRequest.unstableCreateEvaluationRuleRequest());
    }

    /**
     * Delete an evaluation rule.
     */
    @DELETE
    @Path("/api/public/unstable/evaluation-rules/{evaluationRuleId}")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<UnstableDeleteEvaluationRuleResponse> unstableEvaluationRulesDelete(
            @PathParam("evaluationRuleId") String evaluationRuleId);

    /**
     * Delete an evaluation rule.
     */
    @Override
    default CompletionStage<UnstableDeleteEvaluationRuleResponse> unstableEvaluationRulesDelete(
            APIUnstableEvaluationRulesDeleteRequest apiRequest) {
        return unstableEvaluationRulesDelete(apiRequest.evaluationRuleId());
    }

    /**
     * Get one evaluation rule by its identifier.
     */
    @GET
    @Path("/api/public/unstable/evaluation-rules/{evaluationRuleId}")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<UnstableReadableEvaluationRule> unstableEvaluationRulesGet(
            @PathParam("evaluationRuleId") String evaluationRuleId);

    /**
     * Get one evaluation rule by its identifier.
     */
    @Override
    default CompletionStage<UnstableReadableEvaluationRule> unstableEvaluationRulesGet(
            APIUnstableEvaluationRulesGetRequest apiRequest) {
        return unstableEvaluationRulesGet(apiRequest.evaluationRuleId());
    }

    /**
     * List evaluation rules in the authenticated project.
     */
    @GET
    @Path("/api/public/unstable/evaluation-rules")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<UnstableEvaluationRules> unstableEvaluationRulesList(
            @QueryParam("page") Integer page,
            @QueryParam("limit") Integer limit);

    /**
     * List evaluation rules in the authenticated project.
     */
    @Override
    default CompletionStage<UnstableEvaluationRules> unstableEvaluationRulesList(
            APIUnstableEvaluationRulesListRequest apiRequest) {
        return unstableEvaluationRulesList(apiRequest.page(), apiRequest.limit());
    }

    /**
     * Update an evaluation rule.
     */
    @PATCH
    @Path("/api/public/unstable/evaluation-rules/{evaluationRuleId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<UnstableEvaluationRule> unstableEvaluationRulesUpdate(
            @PathParam("evaluationRuleId") String evaluationRuleId,
            UnstableUpdateEvaluationRuleRequest unstableUpdateEvaluationRuleRequest);

    /**
     * Update an evaluation rule.
     */
    @Override
    default CompletionStage<UnstableEvaluationRule> unstableEvaluationRulesUpdate(
            APIUnstableEvaluationRulesUpdateRequest apiRequest) {
        return unstableEvaluationRulesUpdate(apiRequest.evaluationRuleId(),
                apiRequest.unstableUpdateEvaluationRuleRequest());
    }

}
