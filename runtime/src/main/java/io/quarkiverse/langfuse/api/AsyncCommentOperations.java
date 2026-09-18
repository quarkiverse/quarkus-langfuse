package io.quarkiverse.langfuse.api;

import com.langfuse.api.model.Comment;
import com.langfuse.api.model.CreateCommentRequest;

import io.smallrye.mutiny.Uni;

/**
 * Higher-level operations over Langfuse comments, returning Mutiny types.
 *
 * <p>
 * Obtained from {@link AsyncLangfuseOperations#comments()}. The asynchronous counterpart of
 * {@link CommentOperations}; the two behave identically apart from how absence is represented, which
 * follows each style's own convention: {@link java.util.Optional} for the synchronous tree, a
 * {@code null} item for the asynchronous one.
 *
 * <p>
 * <strong>Comments have no name and cannot be deleted</strong>, so this domain offers a lookup by id
 * and no name-based lookup, no {@code createIfAbsent}, and no delete.
 *
 * @see CommentOperations
 */
public sealed interface AsyncCommentOperations extends AsyncPagedOperations<Comment> permits DefaultAsyncCommentOperations {

    /**
     * A view of this collection restricted to the comments matching {@code filter}.
     *
     * <p>
     * <strong>Replaces any filter already applied rather than combining with it.</strong>
     * {@code comments().matching(a).matching(b)} is filtered by {@code b} alone. On the returned view,
     * every inherited operation is scoped to the view: {@link #findAll()} means "every comment
     * <em>of this view</em>", not every comment in the project.
     *
     * <p>
     * The view is a new instance; this one is unaffected and stays usable.
     *
     * @param filter the criteria to restrict the collection to, must not be {@code null}; use
     *        {@link CommentFilter#none()} for an unrestricted view
     * @return a view of this collection restricted to the matching comments. Never {@code null}
     * @throws IllegalArgumentException if {@code filter} is {@code null}. Thrown from this call rather
     *         than emitted as a failure
     */
    AsyncCommentOperations matching(CommentFilter filter);

    /**
     * Finds a comment by its id.
     *
     * <p>
     * <strong>Emits {@code null} if no comment has that id.</strong>
     *
     * <p>
     * This is a <strong>direct lookup</strong>: Langfuse resolves the id server-side, so it costs a
     * single request whatever the size of the collection.
     *
     * <p>
     * The lookup is not scoped by the view's filter - a comment is fetched by id alone, so calling this
     * on a filtered view emits the same comment as calling it on the unfiltered collection.
     *
     * @param id the comment id to look for, must not be {@code null} or blank
     * @return the matching comment, or {@code null} if no comment has that id
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank. Thrown from this call
     *         rather than emitted as a failure
     */
    Uni<Comment> findById(String id);

    /**
     * Creates a comment.
     *
     * <p>
     * <strong>Emits the new comment's id, not the comment.</strong> Langfuse answers a comment
     * creation with the id alone, and this layer does not issue a second request to turn that into a
     * {@link Comment}: doing so would make a create silently cost two round trips. Pass the emitted id
     * to {@link #findById(String)} where the stored comment is actually needed.
     *
     * @param request the comment to create, must not be {@code null}
     * @return the id of the created comment. Never {@code null}
     * @throws IllegalArgumentException if {@code request} is {@code null}. Thrown from this call rather
     *         than emitted as a failure
     */
    Uni<String> create(CreateCommentRequest request);
}
