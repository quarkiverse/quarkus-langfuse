package io.quarkiverse.langfuse.api;

import io.smallrye.mutiny.Uni;

/**
 * Asynchronously fetches a single page of a page-addressed Langfuse collection.
 *
 * @param <T> the type of the items on the page
 */
@FunctionalInterface
interface AsyncPageFetcher<T> {
    Uni<PagedResult<T>> fetch(Page page);
}
