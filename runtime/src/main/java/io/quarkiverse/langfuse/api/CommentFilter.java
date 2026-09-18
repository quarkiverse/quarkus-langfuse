package io.quarkiverse.langfuse.api;

import java.util.Optional;

import com.langfuse.api.model.CommentObjectType;

/**
 * The criteria Langfuse applies when listing comments.
 *
 * <p>
 * Every criterion is optional, and a filter with none set matches the whole collection. Instances are
 * immutable and safe to share; build one with {@link #builder()} and derive a variant from an
 * existing one with {@link #toBuilder()}.
 *
 * <pre>{@code
 * var filter = CommentFilter.builder()
 *         .objectType(CommentObjectType.TRACE)
 *         .objectId("trace-1")
 *         .build();
 *
 * langfuse.comments().matching(filter).findAll();
 * }</pre>
 *
 * <p>
 * Langfuse rejects a request carrying {@code objectId} without {@code objectType}. That constraint is
 * the server's, and this type does not pre-empt it: the rejection surfaces as a
 * {@link com.langfuse.api.LangfuseApiException} from whichever listing operation is invoked.
 *
 * @see CommentOperations#matching(CommentFilter)
 */
public sealed interface CommentFilter permits DefaultCommentFilter {

    /**
     * A new, empty builder.
     *
     * @return a builder with no criterion set
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * The filter matching every comment, which is what an unfiltered
     * {@link LangfuseOperations#comments()} uses.
     *
     * @return a filter with no criterion set
     */
    static CommentFilter none() {
        return new DefaultCommentFilter(null, null, null);
    }

    /**
     * A builder pre-populated with this filter's criteria, for deriving a variant of it.
     *
     * @return a builder holding this filter's criteria
     */
    default Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * The kind of object the comment is attached to.
     *
     * @return the object type to match, or empty if comments on any kind of object match
     */
    Optional<CommentObjectType> objectType();

    /**
     * The id of the object the comment is attached to.
     *
     * @return the object id to match, or empty if comments on any object match
     */
    Optional<String> objectId();

    /**
     * The id of the user who authored the comment.
     *
     * @return the author user id to match, or empty if comments by any author match
     */
    Optional<String> authorUserId();

    /**
     * Builds a {@link CommentFilter}.
     */
    class Builder {
        // The object type is held as the wire string rather than the enum so that this builder and
        // DefaultCommentFilter agree on one representation, and the enum round-trip happens in exactly
        // one place - the accessor - instead of once per conversion site.
        String rawObjectType;
        String objectId;
        String authorUserId;

        private Builder() {
        }

        private Builder(CommentFilter filter) {
            this.rawObjectType = filter.objectType()
                    .map(Enum::name)
                    .orElse(null);
            this.objectId = filter.objectId().orElse(null);
            this.authorUserId = filter.authorUserId().orElse(null);
        }

        /**
         * Restricts the filter to comments attached to the given kind of object.
         *
         * @param objectType the object type to match, or {@code null} to match any
         * @return this builder
         */
        public Builder objectType(CommentObjectType objectType) {
            this.rawObjectType = (objectType == null) ? null : objectType.name();

            return this;
        }

        /**
         * Restricts the filter to comments attached to the given object.
         *
         * <p>
         * Langfuse requires {@link #objectType(CommentObjectType)} to be set alongside this.
         *
         * @param objectId the object id to match, or {@code null} to match any
         * @return this builder
         */
        public Builder objectId(String objectId) {
            this.objectId = objectId;

            return this;
        }

        /**
         * Restricts the filter to comments written by the given user.
         *
         * @param authorUserId the author user id to match, or {@code null} to match any
         * @return this builder
         */
        public Builder authorUserId(String authorUserId) {
            this.authorUserId = authorUserId;

            return this;
        }

        /**
         * Builds the filter.
         *
         * @return the filter holding the criteria set on this builder
         */
        public CommentFilter build() {
            return new DefaultCommentFilter(this);
        }
    }
}
