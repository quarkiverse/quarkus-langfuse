package io.quarkiverse.langfuse.api;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Describes <em>which</em> batches a cursor-addressed traversal should visit.
 *
 * <p>
 * The cursor-addressed counterpart of {@link PageSelection}. Where a {@link Cursor} is a position
 * ("resume from this token, 75 items at a time"), a {@link CursorSelection} is an intent
 * ("everything", "this batch only", "three batches from here").
 *
 * <pre>{@code
 * CursorSelection.all(); // every batch, using the configured default batch size
 * CursorSelection.all(75); // every batch, 75 items per request
 * CursorSelection.from(cursor); // that cursor onward
 * CursorSelection.only(cursor); // that batch, and nothing else
 * CursorSelection.first(3, 75); // the first three batches
 * CursorSelection.from(cursor, 3); // three batches, starting at that cursor
 * }</pre>
 *
 * <p>
 * There is deliberately no range or index-based factory: Langfuse's cursor metadata carries only the
 * next cursor, so batches have no addressable ordinal and no totals are available.
 *
 * @see Cursor
 * @see CursorResult
 */
public sealed interface CursorSelection permits AllCursors, FromCursor, SingleCursor, CursorBatches {

    /**
     * Every batch, from the start of the collection, using the batch size configured by
     * {@code quarkus.langfuse.api.default-batch-size}.
     *
     * @return the selection
     */
    static CursorSelection all() {
        return new AllCursors(OptionalInt.empty());
    }

    /**
     * Every batch, from the start of the collection, requesting the given number of items each time.
     *
     * @param limit the number of items per request, must be greater than or equal to {@code 1}
     * @return the selection
     * @throws IllegalArgumentException if {@code limit} is less than {@code 1}
     */
    static CursorSelection all(int limit) {
        return new AllCursors(OptionalInt.of(requireValidLimit(limit)));
    }

    /**
     * The batch at the given cursor and every batch after it.
     *
     * @param start the cursor to start from
     * @return the selection
     */
    static CursorSelection from(Cursor start) {
        return new FromCursor(start);
    }

    /**
     * The batch at the given cursor only.
     *
     * @param cursor the cursor identifying the single batch to fetch
     * @return the selection
     */
    static CursorSelection only(Cursor cursor) {
        return new SingleCursor(cursor);
    }

    /**
     * The first {@code batches} batches of the collection.
     *
     * <p>
     * A {@code batches} value of {@code 0} selects nothing, and performs no request.
     *
     * @param batches the number of batches to fetch, must not be negative
     * @param limit the number of items per request, must be greater than or equal to {@code 1}
     * @return the selection
     * @throws IllegalArgumentException if {@code batches} is negative or {@code limit} is less than {@code 1}
     */
    static CursorSelection first(int batches, int limit) {
        return new CursorBatches(Cursor.first(limit), batches);
    }

    /**
     * The given number of batches, starting at the given cursor.
     *
     * <p>
     * A {@code batches} value of {@code 0} selects nothing, and performs no request.
     *
     * @param start the cursor to start from
     * @param batches the number of batches to fetch, must not be negative
     * @return the selection
     * @throws IllegalArgumentException if {@code batches} is negative
     */
    static CursorSelection from(Cursor start, int batches) {
        return new CursorBatches(start, batches);
    }

    /**
     * The cursor to start from, or empty when the traversal should start at the beginning of the
     * collection using the configured default batch size.
     *
     * @return the starting cursor, if this selection specifies one
     */
    Optional<Cursor> startCursor();

    /**
     * The number of items to request per batch, or empty when the configured default should be used.
     *
     * @return the limit, if this selection specifies one
     */
    OptionalInt limit();

    /**
     * The maximum number of batches to visit, or empty when the traversal should continue until
     * Langfuse reports no further cursor.
     *
     * @return the batch count, if this selection is bounded
     */
    OptionalInt batchCount();

    /**
     * Resolves this selection to its first cursor, applying the given default batch size when this
     * selection does not specify one.
     *
     * @param defaultBatchSize the number of items to request when this selection does not specify one
     * @return the cursor of the first batch to request
     * @throws IllegalArgumentException if {@code defaultBatchSize} is less than {@code 1}
     */
    default Cursor firstBatch(int defaultBatchSize) {
        return startCursor().orElseGet(() -> Cursor.first(limit().orElse(defaultBatchSize)));
    }

    private static int requireValidLimit(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Cursor limit must be greater than or equal to 1, but was %d".formatted(limit));
        }

        return limit;
    }
}
