package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.model.Prompt;
import com.langfuse.api.promptVersion.PromptVersionApi.APIPromptVersionUpdateRequest;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "PromptVersion")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface PromptVersionApi extends com.langfuse.api.promptVersion.PromptVersionApi {

    /**
     * Update labels for a specific prompt version
     *
     * @param apiRequest {@link APIPromptVersionUpdateRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "promptVersionUpdate", method = "PATCH", path = "/api/public/v2/prompts/{name}/versions/{version}")
    @jakarta.ws.rs.PATCH
    @jakarta.ws.rs.Path("/api/public/v2/prompts/{name}/versions/{version}")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("promptVersion_update")
    @Override
    public Prompt promptVersionUpdate(
            APIPromptVersionUpdateRequest apiRequest);

}
