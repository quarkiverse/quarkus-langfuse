package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.model.Prompt;
import com.langfuse.api.model.PromptMetaListResponse;
import com.langfuse.api.prompts.PromptsApi.APIPromptsCreateRequest;
import com.langfuse.api.prompts.PromptsApi.APIPromptsDeleteRequest;
import com.langfuse.api.prompts.PromptsApi.APIPromptsGetRequest;
import com.langfuse.api.prompts.PromptsApi.APIPromptsListRequest;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Prompts")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface PromptsAsyncApi extends com.langfuse.api.prompts.async.PromptsApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "promptsCreate", method = "POST", path = "/api/public/v2/prompts")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/v2/prompts")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("prompts_create")
    @Override
    public CompletionStage<Prompt> promptsCreate(
            APIPromptsCreateRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "promptsDelete", method = "DELETE", path = "/api/public/v2/prompts/{promptName}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/v2/prompts/{promptName}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("prompts_delete")
    @Override
    public CompletionStage<Void> promptsDelete(
            APIPromptsDeleteRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "promptsGet", method = "GET", path = "/api/public/v2/prompts/{promptName}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/v2/prompts/{promptName}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("prompts_get")
    @Override
    public CompletionStage<Prompt> promptsGet(
            APIPromptsGetRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "promptsList", method = "GET", path = "/api/public/v2/prompts")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/v2/prompts")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("prompts_list")
    @Override
    public CompletionStage<PromptMetaListResponse> promptsList(
            APIPromptsListRequest apiRequest);

}
