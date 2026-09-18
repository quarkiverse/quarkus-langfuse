package io.quarkiverse.langfuse.api;

import java.util.Optional;

import com.langfuse.api.model.CommentObjectType;

// The components carry the raw nullable values and are renamed, because a record component and an
// Optional-returning accessor cannot share a name. Same shape as DefaultCursor.cursorValue backing
// Optional<String> value().
record DefaultCommentFilter(String rawObjectType, String rawObjectId, String rawAuthorUserId) implements CommentFilter {

    DefaultCommentFilter(Builder builder) {
        this(builder.rawObjectType, builder.objectId, builder.authorUserId);
    }

    @Override
    public Optional<CommentObjectType> objectType() {
        return Optional.ofNullable(this.rawObjectType)
                .map(CommentObjectType::valueOf);
    }

    @Override
    public Optional<String> objectId() {
        return Optional.ofNullable(this.rawObjectId);
    }

    @Override
    public Optional<String> authorUserId() {
        return Optional.ofNullable(this.rawAuthorUserId);
    }
}
