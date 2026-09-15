package io.quarkiverse.langfuse.api;

import java.util.List;
import java.util.stream.Stream;

/**
 * Common operations over a cursor-addressed Langfuse collection.
 *
 * <p>
 * The cursor-addressed counterpart of {@link PagedOperations}. Not part of the public API in its
 * own right - each domain's operations interface (e.g. {@code EvaluationRuleOperations}) extends
 * this to inherit {@code stream}/{@code streamBatches}/{@code findBatch} and the operations derived
 * from them.
 *
 * @param <T> the type of the items in the collection
 * @see AbstractCursorOperations
 */
sealed interface CursorOperations<T> permits EvaluationRuleOperations, AbstractCursorOperations {

    /**
     * All items, fetched using the batch size configured by
     * {@code quarkus.langfuse.api.default-batch-size}.
     *
     * @return every item
     */
    default List<T> findAll() {
        return find(CursorSelection.all());
    }

    /**
     * The items in the selected batches.
     *
     * @param selection which batches to fetch
     * @return the items in those batches
     */
    default List<T> find(CursorSelection selection) {
        return stream(selection).toList();
    }

    /**
     * All items, as a lazily paginated stream.
     *
     * @return every item
     * @see #stream(CursorSelection)
     */
    default Stream<T> streamAll() {
        return stream(CursorSelection.all());
    }

    /**
     * The items in the selected batches, as a lazily paginated stream.
     *
     * <p>
     * No request is made until the stream is consumed, and each batch is fetched only once the
     * previous one has been exhausted. Because requests happen during traversal, a
     * {@link com.langfuse.api.LangfuseApiException} may be thrown from a terminal operation rather
     * than from this method.
     *
     * @param selection which batches to fetch
     * @return the items in those batches
     */
    Stream<T> stream(CursorSelection selection);

    /**
     * The selected batches themselves, as a lazily paginated stream, for callers that need the next
     * cursor as they go.
     *
     * @param selection which batches to fetch
     * @return the selected batches
     */
    Stream<CursorResult<T>> streamBatches(CursorSelection selection);

    /**
     * Fetches exactly one batch.
     *
     * @param cursor the cursor to fetch
     * @return the batch, together with the next cursor, if there is one
     */
    CursorResult<T> findBatch(Cursor cursor);
}
