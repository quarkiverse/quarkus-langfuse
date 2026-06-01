package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.model.UnstableDeleteEvaluationRuleResponse;
import com.langfuse.api.model.UnstableEvaluationRule;
import com.langfuse.api.model.UnstableEvaluationRules;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesCreateRequest;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesDeleteRequest;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesGetRequest;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesListRequest;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi.APIUnstableEvaluationRulesUpdateRequest;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "UnstableEvaluationRules")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface UnstableEvaluationRulesApi extends com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi {

    /**
     * Create an evaluation rule.
     *
     * @param apiRequest {@link APIUnstableEvaluationRulesCreateRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "unstableEvaluationRulesCreate", method = "POST", path = "/api/public/unstable/evaluation-rules")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/unstable/evaluation-rules")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("unstable_evaluationRules_create")
    @Override
    public UnstableEvaluationRule unstableEvaluationRulesCreate(
            APIUnstableEvaluationRulesCreateRequest apiRequest);

    /**
     * Delete an evaluation rule.
     *
     * @param apiRequest {@link APIUnstableEvaluationRulesDeleteRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "unstableEvaluationRulesDelete", method = "DELETE", path = "/api/public/unstable/evaluation-rules/{evaluationRuleId}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/unstable/evaluation-rules/{evaluationRuleId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("unstable_evaluationRules_delete")
    @Override
    public UnstableDeleteEvaluationRuleResponse unstableEvaluationRulesDelete(
            APIUnstableEvaluationRulesDeleteRequest apiRequest);

    /**
     * Get one evaluation rule by its identifier.
     *
     * @param apiRequest {@link APIUnstableEvaluationRulesGetRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "unstableEvaluationRulesGet", method = "GET", path = "/api/public/unstable/evaluation-rules/{evaluationRuleId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/unstable/evaluation-rules/{evaluationRuleId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("unstable_evaluationRules_get")
    @Override
    public UnstableEvaluationRule unstableEvaluationRulesGet(
            APIUnstableEvaluationRulesGetRequest apiRequest);

    /**
     * List evaluation rules in the authenticated project.
     *
     * @param apiRequest {@link APIUnstableEvaluationRulesListRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "unstableEvaluationRulesList", method = "GET", path = "/api/public/unstable/evaluation-rules")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/unstable/evaluation-rules")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("unstable_evaluationRules_list")
    @Override
    public UnstableEvaluationRules unstableEvaluationRulesList(
            APIUnstableEvaluationRulesListRequest apiRequest);

    /**
     * Update an evaluation rule.
     *
     * @param apiRequest {@link APIUnstableEvaluationRulesUpdateRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "unstableEvaluationRulesUpdate", method = "PATCH", path = "/api/public/unstable/evaluation-rules/{evaluationRuleId}")
    @jakarta.ws.rs.PATCH
    @jakarta.ws.rs.Path("/api/public/unstable/evaluation-rules/{evaluationRuleId}")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("unstable_evaluationRules_update")
    @Override
    public UnstableEvaluationRule unstableEvaluationRulesUpdate(
            APIUnstableEvaluationRulesUpdateRequest apiRequest);

}
