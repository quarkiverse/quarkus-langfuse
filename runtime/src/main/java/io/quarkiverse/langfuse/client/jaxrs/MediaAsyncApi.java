package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.media.MediaApi.APIMediaGetRequest;
import com.langfuse.api.media.MediaApi.APIMediaGetUploadUrlRequest;
import com.langfuse.api.media.MediaApi.APIMediaPatchRequest;
import com.langfuse.api.model.GetMediaResponse;
import com.langfuse.api.model.GetMediaUploadUrlResponse;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Media")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface MediaAsyncApi extends com.langfuse.api.media.async.MediaApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "mediaGet", method = "GET", path = "/api/public/media/{mediaId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/media/{mediaId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("media_get")
    @Override
    public CompletionStage<GetMediaResponse> mediaGet(
            APIMediaGetRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "mediaGetUploadUrl", method = "POST", path = "/api/public/media")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/media")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("media_getUploadUrl")
    @Override
    public CompletionStage<GetMediaUploadUrlResponse> mediaGetUploadUrl(
            APIMediaGetUploadUrlRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "mediaPatch", method = "PATCH", path = "/api/public/media/{mediaId}")
    @jakarta.ws.rs.PATCH
    @jakarta.ws.rs.Path("/api/public/media/{mediaId}")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("media_patch")
    @Override
    public CompletionStage<Void> mediaPatch(
            APIMediaPatchRequest apiRequest);

}
