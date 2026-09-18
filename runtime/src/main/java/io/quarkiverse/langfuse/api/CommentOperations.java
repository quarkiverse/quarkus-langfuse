package io.quarkiverse.langfuse.api;

import java.util.Optional;

import com.langfuse.api.model.Comment;
import com.langfuse.api.model.CreateCommentRequest;

import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PageSelection;

/**
 * Higher-level operations over Langfuse comments.
 *
 * <p>
 * Obtained from {@link LangfuseOperations#comments()}. Comments are a page-addressed collection, so
 * {@link #stream}, {@link #streamPages} and {@link #findPage} - inherited from
 * {@link PagedOperations} - accept a {@link PageSelection} or a {@link Page} directly.
 *
 * <p>
 * <strong>Comments have no name.</strong> A comment is identified by its id alone, so this domain
 * offers {@link #findById(String)} and no name-based lookup, and there is no
 * {@code createIfAbsent}: there is no key on which "already there" could be decided.
 *
 * <p>
 * <strong>Comments cannot be deleted.</strong> Langfuse exposes no delete endpoint for them, which is
 * why this interface has none.
 *
 * <pre>{@code
 * langfuse.comments().findById("comment-1");
 * langfuse.comments().matching(filter).findAll();
 * }</pre>
 *
 * @see AsyncCommentOperations
 */
public sealed interface CommentOperations extends PagedOperations<Comment> permits DefaultCommentOperations {

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
     * @return a view of this collection restricted to the matching comments
     * @throws IllegalArgumentException if {@code filter} is {@code null}
     */
    CommentOperations matching(CommentFilter filter);

    /**
     * Finds a comment by its id.
     *
     * <p>
     * This is a <strong>direct lookup</strong>: Langfuse resolves the id server-side, so it costs a
     * single request whatever the size of the collection.
     *
     * <p>
     * The lookup is not scoped by the view's filter - a comment is fetched by id alone, so calling this
     * on a filtered view returns the same comment as calling it on the unfiltered collection.
     *
     * @param id the comment id to look for, must not be {@code null} or blank
     * @return the matching comment, or empty if no comment has that id
     * @throws IllegalArgumentException if {@code id} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         comment not existing
     */
    Optional<Comment> findById(String id);

    /**
     * Creates a comment.
     *
     * <p>
     * <strong>Returns the new comment's id, not the comment.</strong> Langfuse answers a comment
     * creation with the id alone, and this layer does not issue a second request to turn that into a
     * {@link Comment}: doing so would make a create silently cost two round trips. Pass the returned id
     * to {@link #findById(String)} where the stored comment is actually needed.
     *
     * @param request the comment to create, must not be {@code null}
     * @return the id of the created comment
     * @throws IllegalArgumentException if {@code request} is {@code null}
     * @throws com.langfuse.api.LangfuseApiException if the request fails
     */
    String create(CreateCommentRequest request);
}
