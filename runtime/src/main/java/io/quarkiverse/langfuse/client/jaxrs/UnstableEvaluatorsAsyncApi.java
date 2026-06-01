package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.model.UnstableEvaluator;
import com.langfuse.api.model.UnstableEvaluators;
import com.langfuse.api.unstableEvaluators.UnstableEvaluatorsApi.APIUnstableEvaluatorsCreateRequest;
import com.langfuse.api.unstableEvaluators.UnstableEvaluatorsApi.APIUnstableEvaluatorsGetRequest;
import com.langfuse.api.unstableEvaluators.UnstableEvaluatorsApi.APIUnstableEvaluatorsListRequest;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "UnstableEvaluators")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface UnstableEvaluatorsAsyncApi extends com.langfuse.api.unstableEvaluators.async.UnstableEvaluatorsApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "unstableEvaluatorsCreate", method = "POST", path = "/api/public/unstable/evaluators")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/unstable/evaluators")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("unstable_evaluators_create")
    @Override
    public CompletionStage<UnstableEvaluator> unstableEvaluatorsCreate(
            APIUnstableEvaluatorsCreateRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "unstableEvaluatorsGet", method = "GET", path = "/api/public/unstable/evaluators/{evaluatorId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/unstable/evaluators/{evaluatorId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("unstable_evaluators_get")
    @Override
    public CompletionStage<UnstableEvaluator> unstableEvaluatorsGet(
            APIUnstableEvaluatorsGetRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "unstableEvaluatorsList", method = "GET", path = "/api/public/unstable/evaluators")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/unstable/evaluators")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("unstable_evaluators_list")
    @Override
    public CompletionStage<UnstableEvaluators> unstableEvaluatorsList(
            APIUnstableEvaluatorsListRequest apiRequest);

}
