package io.quarkiverse.langfuse.api;

import java.util.List;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * Common operations over a page-addressed Langfuse collection, returning Mutiny types.
 *
 * <p>
 * The asynchronous counterpart of {@link PagedOperations}. Not part of the public API in its own
 * right - each domain's asynchronous operations interface (e.g. {@code AsyncModelOperations})
 * extends this to inherit {@code stream}/{@code streamPages}/{@code findPage} and the operations
 * derived from them.
 *
 * @param <T> the type of the items in the collection
 * @see AbstractAsyncPagedOperations
 */
sealed interface AsyncPagedOperations<T>
        permits AsyncModelOperations, AsyncDatasetOperations, AsyncLlmConnectionOperations, AsyncScoreConfigOperations,
        AbstractAsyncPagedOperations {

    /**
     * All items, fetched using the page size configured by
     * {@code quarkus.langfuse.api.default-page-size}.
     *
     * @return every item. Never {@code null}
     */
    default Uni<List<T>> findAll() {
        return find(PageSelection.all());
    }

    /**
     * The items on the selected pages.
     *
     * @param selection which pages to fetch
     * @return the items on those pages. Never {@code null}
     */
    default Uni<List<T>> find(PageSelection selection) {
        return stream(selection).collect().asList();
    }

    /**
     * All items, as a lazily paginated stream.
     *
     * @return every item
     * @see #stream(PageSelection)
     */
    default Multi<T> streamAll() {
        return stream(PageSelection.all());
    }

    /**
     * The items on the selected pages, as a lazily paginated stream.
     *
     * @param selection which pages to fetch
     * @return the items on those pages
     */
    Multi<T> stream(PageSelection selection);

    /**
     * The selected pages themselves, as a lazily paginated stream, for callers that need the
     * pagination metadata as they go.
     *
     * @param selection which pages to fetch
     * @return the selected pages
     */
    Multi<PagedResult<T>> streamPages(PageSelection selection);

    /**
     * Fetches exactly one page.
     *
     * @param page the page to fetch
     * @return the page, together with the totals reported by Langfuse. Never {@code null}
     */
    Uni<PagedResult<T>> findPage(Page page);
}
