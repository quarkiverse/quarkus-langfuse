package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.model.UnstableEvaluator;
import com.langfuse.api.model.UnstableEvaluators;
import com.langfuse.api.unstableEvaluators.UnstableEvaluatorsApi.APIUnstableEvaluatorsCreateRequest;
import com.langfuse.api.unstableEvaluators.UnstableEvaluatorsApi.APIUnstableEvaluatorsGetRequest;
import com.langfuse.api.unstableEvaluators.UnstableEvaluatorsApi.APIUnstableEvaluatorsListRequest;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "UnstableEvaluators")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface UnstableEvaluatorsApi extends com.langfuse.api.unstableEvaluators.UnstableEvaluatorsApi {

    /**
     * Create an evaluator in the authenticated project.
     *
     * @param apiRequest {@link APIUnstableEvaluatorsCreateRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "unstableEvaluatorsCreate", method = "POST", path = "/api/public/unstable/evaluators")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/unstable/evaluators")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("unstable_evaluators_create")
    @Override
    public UnstableEvaluator unstableEvaluatorsCreate(
            APIUnstableEvaluatorsCreateRequest apiRequest);

    /**
     * Get one evaluator by id.
     *
     * @param apiRequest {@link APIUnstableEvaluatorsGetRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "unstableEvaluatorsGet", method = "GET", path = "/api/public/unstable/evaluators/{evaluatorId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/unstable/evaluators/{evaluatorId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("unstable_evaluators_get")
    @Override
    public UnstableEvaluator unstableEvaluatorsGet(
            APIUnstableEvaluatorsGetRequest apiRequest);

    /**
     * List the evaluators available to the authenticated project.
     *
     * @param apiRequest {@link APIUnstableEvaluatorsListRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "unstableEvaluatorsList", method = "GET", path = "/api/public/unstable/evaluators")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/unstable/evaluators")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("unstable_evaluators_list")
    @Override
    public UnstableEvaluators unstableEvaluatorsList(
            APIUnstableEvaluatorsListRequest apiRequest);

}
