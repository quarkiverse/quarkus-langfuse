package io.quarkiverse.langfuse.api;

import java.util.function.Function;

import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * Supplies the shared implementation of {@link AsyncPagedOperations} for every page-addressed
 * domain, mirroring what {@link AbstractPagedOperations} supplies for the synchronous tree.
 *
 * @param <T> the type of the items in the collection
 */
abstract non-sealed class AbstractAsyncPagedOperations<T> implements AsyncPagedOperations<T> {
    private final AsyncPageFetcher<T> fetcher;
    private final LangfuseConfig config;

    protected AbstractAsyncPagedOperations(AsyncPageFetcher<T> fetcher, LangfuseConfig config) {
        this.fetcher = fetcher;
        this.config = config;
    }

    @Override
    public Multi<T> stream(PageSelection selection) {
        return AsyncPagination.items(selection, defaultPageSize(), this.fetcher);
    }

    @Override
    public Multi<PagedResult<T>> streamPages(PageSelection selection) {
        return AsyncPagination.pages(selection, defaultPageSize(), this.fetcher);
    }

    @Override
    public Uni<PagedResult<T>> findPage(Page page) {
        return this.fetcher.fetch(page);
    }

    /**
     * Scans every page for the first item matching {@code key}, tolerating a
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
        // toUni() on a Multi emits its first item, or null if the Multi completes with none - which is
        // exactly the null-means-absent contract this method promises. onFailure here mirrors the
        // synchronous scanForName: only genuine absence is recovered, everything else still fails the Uni.
        return stream(PageSelection.all())
                .filter(item -> key.equals(keyExtractor.apply(item)))
                .toUni()
                .onFailure(LangfuseNotFoundException.class)
                .recoverWithNull();
    }

    // See AbstractPagedOperations.defaultPageSize(): read per call, never cached.
    private int defaultPageSize() {
        return this.config.api().defaultPageSize();
    }

    protected final int deleteConcurrency() {
        return Math.max(1, this.config.api().deleteConcurrency());
    }
}
