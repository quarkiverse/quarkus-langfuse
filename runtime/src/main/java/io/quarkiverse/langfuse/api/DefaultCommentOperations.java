package io.quarkiverse.langfuse.api;

import java.util.Optional;

import com.langfuse.api.comments.CommentsApi;
import com.langfuse.api.comments.CommentsApi.APICommentsCreateRequest;
import com.langfuse.api.comments.CommentsApi.APICommentsGetByIdRequest;
import com.langfuse.api.comments.CommentsApi.APICommentsGetRequest;
import com.langfuse.api.model.Comment;
import com.langfuse.api.model.CreateCommentRequest;

import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;

final class DefaultCommentOperations extends AbstractPagedOperations<Comment> implements CommentOperations {
    private final CommentsApi commentsApi;
    private final LangfuseConfig config;

    DefaultCommentOperations(CommentsApi commentsApi, LangfuseConfig config) {
        this(commentsApi, config, CommentFilter.none());
    }

    // The filter is captured in the fetcher lambda handed to the engine, which is why a filtered view
    // needs neither a widened PageFetcher nor a change to Pagination: a view is just this class
    // constructed over a different fetcher.
    private DefaultCommentOperations(CommentsApi commentsApi, LangfuseConfig config, CommentFilter filter) {
        super(page -> fetch(commentsApi, page, filter), config);
        this.commentsApi = commentsApi;
        this.config = config;
    }

    @Override
    public CommentOperations matching(CommentFilter filter) {
        return new DefaultCommentOperations(this.commentsApi, this.config,
                ValidationUtils.ensureNotNull(filter, "Filter"));
    }

    @Override
    public Optional<Comment> findById(String id) {
        var commentId = ValidationUtils.ensureNotBlank(id, "Comment id");

        // Only LangfuseNotFoundException is caught - catching LangfuseApiException would report a 401 or
        // a 500 as "absent", which is the one mistake this layer must never make.
        try {
            return Optional.of(this.commentsApi.commentsGetById(APICommentsGetByIdRequest.newBuilder()
                    .commentId(commentId)
                    .build()));
        } catch (LangfuseNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public String create(CreateCommentRequest request) {
        ValidationUtils.ensureNotNull(request, "request");

        // Langfuse answers with the id alone. No follow-up commentsGetById is issued: a create must not
        // silently cost two round trips, so the id is what this returns.
        return this.commentsApi.commentsCreate(APICommentsCreateRequest.newBuilder()
                .createCommentRequest(request)
                .build())
                .getId();
    }

    private static PagedResult<Comment> fetch(CommentsApi commentsApi, Page page, CommentFilter filter) {
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

        var response = commentsApi.commentsGet(builder.build());

        return PagedResults.from(page, response.getData(), response.getMeta());
    }
}
