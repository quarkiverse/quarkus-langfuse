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

import com.langfuse.api.model.CreateEvaluationRuleRequest;
import com.langfuse.api.model.DeletedEvaluationRule;
import com.langfuse.api.model.EvaluationRule;
import com.langfuse.api.model.EvaluationRulesPage;
import com.langfuse.api.model.UpdateEvaluationRuleRequest;

/**
 * Langfuse Evaluation Rules API
 */
public interface QuarkusEvaluationRulesApi extends com.langfuse.api.evaluationRules.EvaluationRulesApi {

    /**
     * Create an evaluation rule using stable evaluator identifiers.
     */
    @POST
    @Path("/api/public/v2/evaluation-rules")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    EvaluationRule evaluationRulesCreate(
            CreateEvaluationRuleRequest createEvaluationRuleRequest);

    /**
     * Create an evaluation rule using stable evaluator identifiers.
     */
    @Override
    default EvaluationRule evaluationRulesCreate(APIEvaluationRulesCreateRequest apiRequest) {
        return evaluationRulesCreate(apiRequest.createEvaluationRuleRequest());
    }

    /**
     * Delete an evaluation rule.
     */
    @DELETE
    @Path("/api/public/v2/evaluation-rules/{evaluationRuleId}")
    @Produces(MediaType.APPLICATION_JSON)
    DeletedEvaluationRule evaluationRulesDelete(
            @PathParam("evaluationRuleId") String evaluationRuleId);

    /**
     * Delete an evaluation rule.
     */
    @Override
    default DeletedEvaluationRule evaluationRulesDelete(APIEvaluationRulesDeleteRequest apiRequest) {
        return evaluationRulesDelete(apiRequest.evaluationRuleId());
    }

    /**
     * Get one evaluation rule by its stable identifier.
     */
    @GET
    @Path("/api/public/v2/evaluation-rules/{evaluationRuleId}")
    @Produces(MediaType.APPLICATION_JSON)
    EvaluationRule evaluationRulesGet(
            @PathParam("evaluationRuleId") String evaluationRuleId);

    /**
     * Get one evaluation rule by its stable identifier.
     */
    @Override
    default EvaluationRule evaluationRulesGet(APIEvaluationRulesGetRequest apiRequest) {
        return evaluationRulesGet(apiRequest.evaluationRuleId());
    }

    /**
     * List evaluation rules in newest-first creation order.
     */
    @GET
    @Path("/api/public/v2/evaluation-rules")
    @Produces(MediaType.APPLICATION_JSON)
    EvaluationRulesPage evaluationRulesList(
            @QueryParam("limit") Integer limit,
            @QueryParam("cursor") String cursor);

    /**
     * List evaluation rules in newest-first creation order.
     */
    @Override
    default EvaluationRulesPage evaluationRulesList(APIEvaluationRulesListRequest apiRequest) {
        return evaluationRulesList(apiRequest.limit(), apiRequest.cursor());
    }

    /**
     * Update an evaluation rule by its stable identifier.
     */
    @PATCH
    @Path("/api/public/v2/evaluation-rules/{evaluationRuleId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    EvaluationRule evaluationRulesUpdate(
            @PathParam("evaluationRuleId") String evaluationRuleId,
            UpdateEvaluationRuleRequest updateEvaluationRuleRequest);

    /**
     * Update an evaluation rule by its stable identifier.
     */
    @Override
    default EvaluationRule evaluationRulesUpdate(APIEvaluationRulesUpdateRequest apiRequest) {
        return evaluationRulesUpdate(apiRequest.evaluationRuleId(), apiRequest.updateEvaluationRuleRequest());
    }

}
