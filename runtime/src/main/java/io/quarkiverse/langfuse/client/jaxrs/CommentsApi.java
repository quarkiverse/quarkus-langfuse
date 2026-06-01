package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.model.Comment;
import com.langfuse.api.model.CreateCommentResponse;
import com.langfuse.api.model.GetCommentsResponse;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Comments")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface CommentsApi extends com.langfuse.api.comments.CommentsApi {

    /**
     * Create a comment. Comments may be attached to different object types (trace, observation, session, prompt).
     *
     * @param apiRequest {@link APICommentsCreateRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "commentsCreate", method = "POST", path = "/api/public/comments")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/comments")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("comments_create")
    @Override
    public CreateCommentResponse commentsCreate(
            APICommentsCreateRequest apiRequest);

    /**
     * Get all comments
     *
     * @param apiRequest {@link APICommentsGetRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "commentsGet", method = "GET", path = "/api/public/comments")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/comments")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("comments_get")
    @Override
    public GetCommentsResponse commentsGet(
            APICommentsGetRequest apiRequest);

    /**
     * Get a comment by id
     *
     * @param apiRequest {@link APICommentsGetByIdRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "commentsGetById", method = "GET", path = "/api/public/comments/{commentId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/comments/{commentId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("comments_get-by-id")
    @Override
    public Comment commentsGetById(
            APICommentsGetByIdRequest apiRequest);

}
