package io.quarkiverse.langfuse.api;

import com.langfuse.api.comments.CommentsApi.APICommentsCreateRequest;
import com.langfuse.api.comments.CommentsApi.APICommentsGetByIdRequest;
import com.langfuse.api.comments.CommentsApi.APICommentsGetRequest;
import com.langfuse.api.comments.async.CommentsApi;
import com.langfuse.api.model.Comment;
import com.langfuse.api.model.CreateCommentRequest;
import com.langfuse.api.model.CreateCommentResponse;

import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;
import io.smallrye.mutiny.Uni;

final class DefaultAsyncCommentOperations extends AbstractAsyncPagedOperations<Comment>
        implements AsyncCommentOperations {
    private final CommentsApi commentsApi;
    private final LangfuseConfig config;

    DefaultAsyncCommentOperations(CommentsApi commentsApi, LangfuseConfig config) {
        this(commentsApi, config, CommentFilter.none());
    }

    // The filter is captured in the fetcher lambda handed to the engine, which is why a filtered view
    // needs neither a widened AsyncPageFetcher nor a change to AsyncPagination: a view is just this
    // class constructed over a different fetcher.
    private DefaultAsyncCommentOperations(CommentsApi commentsApi, LangfuseConfig config, CommentFilter filter) {
        super(page -> fetch(commentsApi, page, filter), config);
        this.commentsApi = commentsApi;
        this.config = config;
    }

    @Override
    public AsyncCommentOperations matching(CommentFilter filter) {
        return new DefaultAsyncCommentOperations(this.commentsApi, this.config,
                ValidationUtils.ensureNotNull(filter, "Filter"));
    }

    @Override
    public Uni<Comment> findById(String id) {
        // Validated here rather than inside the completionStage supplier: a throw in there becomes a
        // failure event, and blank input must surface as a thrown IllegalArgumentException.
        var commentId = ValidationUtils.ensureNotBlank(id, "Comment id");

        // recoverWithNull is scoped to LangfuseNotFoundException alone: every other failure, a 401
        // included, must still fail the Uni rather than read as absence.
        return Uni.createFrom()
                .completionStage(() -> this.commentsApi.commentsGetById(APICommentsGetByIdRequest.newBuilder()
                        .commentId(commentId)
                        .build()))
                .onFailure(LangfuseNotFoundException.class)
                .recoverWithNull();
    }

    @Override
    public Uni<String> create(CreateCommentRequest request) {
        ValidationUtils.ensureNotNull(request, "request");

        // Langfuse answers with the id alone. No follow-up commentsGetById is issued: a create must not
        // silently cost two round trips, so the id is what this emits.
        return Uni.createFrom()
                .completionStage(() -> this.commentsApi.commentsCreate(APICommentsCreateRequest.newBuilder()
                        .createCommentRequest(request)
                        .build()))
                .map(CreateCommentResponse::getId);
    }

    private static Uni<PagedResult<Comment>> fetch(CommentsApi commentsApi, Page page, CommentFilter filter) {
        var builder = APICommentsGetRequest.newBuilder()
                .page(page.index())
                .limit(page.size());

        // The generated builder takes objectType as a String while our filter exposes the enum, so the
        // conversion happens here, at the boundary, rather than widening the public surface to String.
        filter.objectType()
                .map(Enum::name)
                .ifPresent(builder::objectType);
        filter.objectId().ifPresent(builder::objectId);
        filter.authorUserId().ifPresent(builder::authorUserId);

        var request = builder.build();

        return Uni.createFrom()
                .completionStage(() -> commentsApi.commentsGet(request))
                .map(response -> PagedResults.from(page, response.getData(), response.getMeta()));
    }
}
