package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.model.Prompt;
import com.langfuse.api.promptVersion.PromptVersionApi.APIPromptVersionUpdateRequest;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "PromptVersion")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface PromptVersionAsyncApi extends com.langfuse.api.promptVersion.async.PromptVersionApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "promptVersionUpdate", method = "PATCH", path = "/api/public/v2/prompts/{name}/versions/{version}")
    @jakarta.ws.rs.PATCH
    @jakarta.ws.rs.Path("/api/public/v2/prompts/{name}/versions/{version}")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("promptVersion_update")
    @Override
    public CompletionStage<Prompt> promptVersionUpdate(
            APIPromptVersionUpdateRequest apiRequest);

}
