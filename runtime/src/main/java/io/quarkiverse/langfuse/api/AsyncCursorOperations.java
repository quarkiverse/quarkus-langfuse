package io.quarkiverse.langfuse.api;

import java.util.List;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * Common operations over a cursor-addressed Langfuse collection, returning Mutiny types.
 *
 * <p>
 * The asynchronous counterpart of {@link CursorOperations}. Not part of the public API in its own
 * right - each domain's asynchronous operations interface (e.g.
 * {@code AsyncEvaluationRuleOperations}) extends this to inherit {@code stream}/
 * {@code streamBatches}/{@code findBatch} and the operations derived from them.
 *
 * @param <T> the type of the items in the collection
 * @see AbstractAsyncCursorOperations
 */
sealed interface AsyncCursorOperations<T> permits AsyncEvaluationRuleOperations, AbstractAsyncCursorOperations {

    /**
     * All items, fetched using the batch size configured by
     * {@code quarkus.langfuse.api.default-batch-size}.
     *
     * @return every item. Never {@code null}
     */
    default Uni<List<T>> findAll() {
        return find(CursorSelection.all());
    }

    /**
     * The items in the selected batches.
     *
     * @param selection which batches to fetch
     * @return the items in those batches. Never {@code null}
     */
    default Uni<List<T>> find(CursorSelection selection) {
        return stream(selection).collect().asList();
    }

    /**
     * All items, as a lazily paginated stream.
     *
     * @return every item
     * @see #stream(CursorSelection)
     */
    default Multi<T> streamAll() {
        return stream(CursorSelection.all());
    }

    /**
     * The items in the selected batches, as a lazily paginated stream.
     *
     * @param selection which batches to fetch
     * @return the items in those batches
     */
    Multi<T> stream(CursorSelection selection);

    /**
     * The selected batches themselves, as a lazily paginated stream, for callers that need the next
     * cursor as they go.
     *
     * @param selection which batches to fetch
     * @return the selected batches
     */
    Multi<CursorResult<T>> streamBatches(CursorSelection selection);

    /**
     * Fetches exactly one batch.
     *
     * @param cursor the cursor to fetch
     * @return the batch, together with the next cursor, if there is one. Never {@code null}
     */
    Uni<CursorResult<T>> findBatch(Cursor cursor);
}
