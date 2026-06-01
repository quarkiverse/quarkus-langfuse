package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.model.UnstableDeleteEvaluationRuleResponse;
import com.langfuse.api.model.UnstableEvaluationRule;
import com.langfuse.api.model.UnstableEvaluationRules;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesCreateRequest;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesDeleteRequest;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesGetRequest;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesListRequest;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesUpdateRequest;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "UnstableEvaluationRules")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface UnstableEvaluationRulesAsyncApi
        extends com.langfuse.api.unstableEvaluationRules.async.UnstableEvaluationRulesApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "unstableEvaluationRulesCreate", method = "POST", path = "/api/public/unstable/evaluation-rules")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/unstable/evaluation-rules")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("unstable_evaluationRules_create")
    @Override
    public CompletionStage<UnstableEvaluationRule> unstableEvaluationRulesCreate(
            APIUnstableEvaluationRulesCreateRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "unstableEvaluationRulesDelete", method = "DELETE", path = "/api/public/unstable/evaluation-rules/{evaluationRuleId}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/unstable/evaluation-rules/{evaluationRuleId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("unstable_evaluationRules_delete")
    @Override
    public CompletionStage<UnstableDeleteEvaluationRuleResponse> unstableEvaluationRulesDelete(
            APIUnstableEvaluationRulesDeleteRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "unstableEvaluationRulesGet", method = "GET", path = "/api/public/unstable/evaluation-rules/{evaluationRuleId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/unstable/evaluation-rules/{evaluationRuleId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("unstable_evaluationRules_get")
    @Override
    public CompletionStage<UnstableEvaluationRule> unstableEvaluationRulesGet(
            APIUnstableEvaluationRulesGetRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "unstableEvaluationRulesList", method = "GET", path = "/api/public/unstable/evaluation-rules")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/unstable/evaluation-rules")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("unstable_evaluationRules_list")
    @Override
    public CompletionStage<UnstableEvaluationRules> unstableEvaluationRulesList(
            APIUnstableEvaluationRulesListRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "unstableEvaluationRulesUpdate", method = "PATCH", path = "/api/public/unstable/evaluation-rules/{evaluationRuleId}")
    @jakarta.ws.rs.PATCH
    @jakarta.ws.rs.Path("/api/public/unstable/evaluation-rules/{evaluationRuleId}")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("unstable_evaluationRules_update")
    @Override
    public CompletionStage<UnstableEvaluationRule> unstableEvaluationRulesUpdate(
            APIUnstableEvaluationRulesUpdateRequest apiRequest);

}
