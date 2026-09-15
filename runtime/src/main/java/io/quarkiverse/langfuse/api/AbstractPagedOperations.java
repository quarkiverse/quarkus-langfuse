package io.quarkiverse.langfuse.api;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;

/**
 * Supplies the shared implementation of {@link PagedOperations} for every page-addressed domain:
 * the {@link Pagination} plumbing, the configured default page size, and the scan-and-tolerate-
 * not-found shape behind each domain's name-based lookup.
 *
 * @param <T> the type of the items in the collection
 */
abstract non-sealed class AbstractPagedOperations<T> implements PagedOperations<T> {
    private final PageFetcher<T> fetcher;
    private final LangfuseConfig config;

    protected AbstractPagedOperations(PageFetcher<T> fetcher, LangfuseConfig config) {
        this.fetcher = fetcher;
        this.config = config;
    }

    @Override
    public Stream<T> stream(PageSelection selection) {
        return Pagination.items(selection, defaultPageSize(), this.fetcher);
    }

    @Override
    public Stream<PagedResult<T>> streamPages(PageSelection selection) {
        return Pagination.pages(selection, defaultPageSize(), this.fetcher);
    }

    @Override
    public PagedResult<T> findPage(Page page) {
        return this.fetcher.fetch(page);
    }

    /**
     * Scans every page for the first item matching {@code key}, tolerating a
     * {@link LangfuseNotFoundException} from the underlying listing as absence rather than failure.
     *
     * @param key the value to match, already validated as non-blank by the caller
     * @param keyExtractor extracts the value to compare {@code key} against from each item
     * @return the first matching item, or empty if none matched or the collection does not exist
     */
    protected final Optional<T> scanForName(String key, Function<T, String> keyExtractor) {
        // Only LangfuseNotFoundException is swallowed here - never a bare catch (Exception). Absence and
        // failure must stay distinguishable: a 401 or a timeout should propagate as a real exception, not
        // silently read the same as "no item with that name". findFirst() is what makes this stop after
        // the page containing the match, rather than scanning the whole collection every time.
        try {
            return stream(PageSelection.all())
                    .filter(item -> key.equals(keyExtractor.apply(item)))
                    .findFirst();
        } catch (LangfuseNotFoundException e) {
            return Optional.empty();
        }
    }

    // Reads the config on every call rather than caching it in the constructor, since it's a runtime
    // value and the cheapest way to guarantee this never observes a stale default is to never store one.
    private int defaultPageSize() {
        return this.config.api().defaultPageSize();
    }
}
