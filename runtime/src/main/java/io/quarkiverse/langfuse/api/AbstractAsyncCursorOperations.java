package io.quarkiverse.langfuse.api;

import java.util.function.Function;

import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * Supplies the shared implementation of {@link AsyncCursorOperations} for every cursor-addressed
 * domain, mirroring what {@link AbstractCursorOperations} supplies for the synchronous tree.
 *
 * @param <T> the type of the items in the collection
 */
abstract non-sealed class AbstractAsyncCursorOperations<T> implements AsyncCursorOperations<T> {
    private final AsyncCursorFetcher<T> fetcher;
    private final LangfuseConfig config;

    protected AbstractAsyncCursorOperations(AsyncCursorFetcher<T> fetcher, LangfuseConfig config) {
        this.fetcher = fetcher;
        this.config = config;
    }

    @Override
    public Multi<T> stream(CursorSelection selection) {
        return AsyncPagination.items(selection, defaultBatchSize(), this.fetcher);
    }

    @Override
    public Multi<CursorResult<T>> streamBatches(CursorSelection selection) {
        return AsyncPagination.batches(selection, defaultBatchSize(), this.fetcher);
    }

    @Override
    public Uni<CursorResult<T>> findBatch(Cursor cursor) {
        return this.fetcher.fetch(cursor);
    }

    /**
     * Scans every batch for the first item matching {@code key}, tolerating a
     * {@link LangfuseNotFoundException} as absence rather than failure.
     *
     * <p>
     * <strong>Emits {@code null} if no item matches.</strong>
     *
     * @param key the value to match, already validated as non-blank by the caller
     * @param keyExtractor extracts the value to compare {@code key} against from each item
     * @return the first matching item, or {@code null} if none matched or the collection does not
     *         exist
     */
    protected final Uni<T> scanForName(String key, Function<T, String> keyExtractor) {
        // See AbstractAsyncPagedOperations.scanForName: toUni() supplies the null-for-absent contract,
        // and only a genuine LangfuseNotFoundException is recovered rather than any failure.
        return stream(CursorSelection.all())
                .filter(item -> key.equals(keyExtractor.apply(item)))
                .toUni()
                .onFailure(LangfuseNotFoundException.class)
                .recoverWithNull();
    }

    // See AbstractPagedOperations.defaultPageSize(): read per call, never cached.
    private int defaultBatchSize() {
        return this.config.api().defaultBatchSize();
    }
}
