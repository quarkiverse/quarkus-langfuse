package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.comments.CommentsApi.APICommentsCreateRequest;
import com.langfuse.api.comments.CommentsApi.APICommentsGetByIdRequest;
import com.langfuse.api.comments.CommentsApi.APICommentsGetRequest;
import com.langfuse.api.model.Comment;
import com.langfuse.api.model.CreateCommentResponse;
import com.langfuse.api.model.GetCommentsResponse;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Comments")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface CommentsAsyncApi extends com.langfuse.api.comments.async.CommentsApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "commentsCreate", method = "POST", path = "/api/public/comments")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/comments")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("comments_create")
    @Override
    public CompletionStage<CreateCommentResponse> commentsCreate(
            APICommentsCreateRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "commentsGet", method = "GET", path = "/api/public/comments")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/comments")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("comments_get")
    @Override
    public CompletionStage<GetCommentsResponse> commentsGet(
            APICommentsGetRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "commentsGetById", method = "GET", path = "/api/public/comments/{commentId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/comments/{commentId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("comments_get-by-id")
    @Override
    public CompletionStage<Comment> commentsGetById(
            APICommentsGetByIdRequest apiRequest);

}
