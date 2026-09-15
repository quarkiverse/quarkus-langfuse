package io.quarkiverse.langfuse.api;

import java.util.Optional;

/**
 * A position within a cursor-addressed collection, together with how many items to request from it.
 *
 * <p>
 * Some Langfuse collections are addressed by an opaque cursor rather than by page index. For those,
 * {@link Cursor} plays the role {@link Page} plays for page-addressed collections: it identifies
 * exactly one batch of results. To describe which batches a traversal should visit, use
 * {@link CursorSelection}.
 *
 * <p>
 * The cursor value is produced by Langfuse and is <strong>never parsed or interpreted</strong> by
 * this extension, so a change to how Langfuse encodes cursors cannot break callers.
 *
 * <pre>{@code
 * Cursor.first(75); // start of the collection, 75 items per request
 * Cursor.at("ey...", 75); // resume from a cursor returned by a previous request
 * }</pre>
 *
 * @see CursorSelection
 * @see CursorResult
 */
public sealed interface Cursor permits DefaultCursor {

    /**
     * A cursor positioned at the start of the collection.
     *
     * @param limit the number of items to request, must be greater than or equal to {@code 1}
     * @return the cursor
     * @throws IllegalArgumentException if {@code limit} is less than {@code 1}
     */
    static Cursor first(int limit) {
        return new DefaultCursor(null, limit);
    }

    /**
     * A cursor positioned at an opaque value previously returned by Langfuse.
     *
     * @param value the cursor value, must not be {@code null} or blank
     * @param limit the number of items to request, must be greater than or equal to {@code 1}
     * @return the cursor
     * @throws IllegalArgumentException if {@code value} is {@code null} or blank, or if {@code limit} is
     *         less than {@code 1}
     */
    static Cursor at(String value, int limit) {
        if ((value == null) || value.isBlank()) {
            throw new IllegalArgumentException("Cursor value must not be null or blank");
        }

        return new DefaultCursor(value, limit);
    }

    /**
     * The opaque cursor value, or empty when this cursor denotes the start of the collection.
     *
     * @return the cursor value, if this cursor is positioned at one
     */
    Optional<String> value();

    /**
     * The number of items to request.
     *
     * @return the limit
     */
    int limit();
}
