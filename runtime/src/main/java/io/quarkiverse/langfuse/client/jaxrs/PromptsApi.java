package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.model.Prompt;
import com.langfuse.api.model.PromptMetaListResponse;
import com.langfuse.api.prompts.PromptsApi.APIPromptsCreateRequest;
import com.langfuse.api.prompts.PromptsApi.APIPromptsDeleteRequest;
import com.langfuse.api.prompts.PromptsApi.APIPromptsGetRequest;
import com.langfuse.api.prompts.PromptsApi.APIPromptsListRequest;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Prompts")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface PromptsApi extends com.langfuse.api.prompts.PromptsApi {

    /**
     * Create a new version for the prompt with the given name
     *
     * @param apiRequest {@link APIPromptsCreateRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "promptsCreate", method = "POST", path = "/api/public/v2/prompts")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/v2/prompts")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("prompts_create")
    @Override
    public Prompt promptsCreate(
            APIPromptsCreateRequest apiRequest);

    /**
     * Delete prompt versions.
     *
     * @param apiRequest {@link APIPromptsDeleteRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "promptsDelete", method = "DELETE", path = "/api/public/v2/prompts/{promptName}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/v2/prompts/{promptName}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("prompts_delete")
    @Override
    public void promptsDelete(
            APIPromptsDeleteRequest apiRequest);

    /**
     * Get a prompt
     *
     * @param apiRequest {@link APIPromptsGetRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "promptsGet", method = "GET", path = "/api/public/v2/prompts/{promptName}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/v2/prompts/{promptName}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("prompts_get")
    @Override
    public Prompt promptsGet(
            APIPromptsGetRequest apiRequest);

    /**
     * Get a list of prompt names with versions and labels
     *
     * @param apiRequest {@link APIPromptsListRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "promptsList", method = "GET", path = "/api/public/v2/prompts")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/v2/prompts")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("prompts_list")
    @Override
    public PromptMetaListResponse promptsList(
            APIPromptsListRequest apiRequest);

}
