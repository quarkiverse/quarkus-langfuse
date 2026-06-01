package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.media.MediaApi.APIMediaGetRequest;
import com.langfuse.api.media.MediaApi.APIMediaGetUploadUrlRequest;
import com.langfuse.api.media.MediaApi.APIMediaPatchRequest;
import com.langfuse.api.model.GetMediaResponse;
import com.langfuse.api.model.GetMediaUploadUrlResponse;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Media")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface MediaApi extends com.langfuse.api.media.MediaApi {

    /**
     * Get a media record
     *
     * @param apiRequest {@link APIMediaGetRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "mediaGet", method = "GET", path = "/api/public/media/{mediaId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/media/{mediaId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("media_get")
    @Override
    public GetMediaResponse mediaGet(
            APIMediaGetRequest apiRequest);

    /**
     * Get a presigned upload URL for a media record
     *
     * @param apiRequest {@link APIMediaGetUploadUrlRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "mediaGetUploadUrl", method = "POST", path = "/api/public/media")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/media")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("media_getUploadUrl")
    @Override
    public GetMediaUploadUrlResponse mediaGetUploadUrl(
            APIMediaGetUploadUrlRequest apiRequest);

    /**
     * Patch a media record
     *
     * @param apiRequest {@link APIMediaPatchRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "mediaPatch", method = "PATCH", path = "/api/public/media/{mediaId}")
    @jakarta.ws.rs.PATCH
    @jakarta.ws.rs.Path("/api/public/media/{mediaId}")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("media_patch")
    @Override
    public void mediaPatch(
            APIMediaPatchRequest apiRequest);

}
