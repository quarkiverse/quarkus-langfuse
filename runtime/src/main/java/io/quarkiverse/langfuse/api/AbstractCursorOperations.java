package io.quarkiverse.langfuse.api;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;

/**
 * Supplies the shared implementation of {@link CursorOperations} for every cursor-addressed domain,
 * mirroring what {@link AbstractPagedOperations} supplies for page-addressed ones.
 *
 * @param <T> the type of the items in the collection
 */
abstract non-sealed class AbstractCursorOperations<T> implements CursorOperations<T> {
    private final CursorFetcher<T> fetcher;
    private final LangfuseConfig config;

    protected AbstractCursorOperations(CursorFetcher<T> fetcher, LangfuseConfig config) {
        this.fetcher = fetcher;
        this.config = config;
    }

    @Override
    public Stream<T> stream(CursorSelection selection) {
        return Pagination.items(selection, defaultBatchSize(), this.fetcher);
    }

    @Override
    public Stream<CursorResult<T>> streamBatches(CursorSelection selection) {
        return Pagination.batches(selection, defaultBatchSize(), this.fetcher);
    }

    @Override
    public CursorResult<T> findBatch(Cursor cursor) {
        return this.fetcher.fetch(cursor);
    }

    /**
     * Scans every batch for the first item matching {@code key}, tolerating a
     * {@link LangfuseNotFoundException} from the underlying listing as absence rather than failure.
     *
     * @param key the value to match, already validated as non-blank by the caller
     * @param keyExtractor extracts the value to compare {@code key} against from each item
     * @return the first matching item, or empty if none matched or the collection does not exist
     */
    protected final Optional<T> scanForName(String key, Function<T, String> keyExtractor) {
        // See AbstractPagedOperations.scanForName: only genuine absence is swallowed, and findFirst()
        // is what stops the walk once a match is found rather than draining every batch regardless.
        try {
            return stream(CursorSelection.all())
                    .filter(item -> key.equals(keyExtractor.apply(item)))
                    .findFirst();
        } catch (LangfuseNotFoundException e) {
            return Optional.empty();
        }
    }

    // Reads the config on every call rather than caching it in the constructor - see the equivalent
    // note on AbstractPagedOperations.defaultPageSize().
    private int defaultBatchSize() {
        return this.config.api().defaultBatchSize();
    }

    protected final int deleteConcurrency() {
        return Math.max(1, this.config.api().deleteConcurrency());
    }
}
