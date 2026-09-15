package io.quarkiverse.langfuse.api;

import java.util.List;
import java.util.Optional;

/**
 * A single batch of results from a cursor-addressed collection, together with the cursor needed to
 * continue.
 *
 * <p>
 * The cursor-addressed counterpart of {@link PagedResult}. Langfuse's cursor metadata carries only
 * the next cursor, so - unlike {@link PagedResult} - no totals are available. Nothing here is
 * inferred or fabricated from what the server does not report.
 *
 * <pre>{@code
 * var batch = langfuse.evaluationRules().findBatch(Cursor.first(75));
 *
 * batch.nextCursor()
 *     .map(langfuse.evaluationRules()::findBatch)
 *     .ifPresent(next -> ...);
 * }</pre>
 *
 * @param <T> the type of the items in the batch
 * @see Cursor
 * @see CursorSelection
 */
public sealed interface CursorResult<T> permits DefaultCursorResult {

    /**
     * The items in this batch, in the order returned by Langfuse.
     *
     * <p>
     * Never {@code null}. Empty when there is no further data.
     *
     * @return an unmodifiable list of the items in this batch
     */
    List<T> items();

    /**
     * The cursor these items were requested with.
     *
     * @return the cursor
     */
    Cursor cursor();

    /**
     * The cursor for the next batch, or empty when Langfuse reports no further data.
     *
     * <p>
     * The returned cursor keeps the same limit as {@link #cursor()}.
     *
     * @return the next cursor, if there is one
     */
    Optional<Cursor> nextCursor();

    /**
     * Whether a further batch is available.
     *
     * @return {@code true} if {@link #nextCursor()} is present
     */
    default boolean hasNext() {
        return nextCursor().isPresent();
    }
}
