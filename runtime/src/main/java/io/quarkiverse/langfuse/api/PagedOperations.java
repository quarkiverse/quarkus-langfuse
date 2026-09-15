package io.quarkiverse.langfuse.api;

import java.util.List;
import java.util.stream.Stream;

/**
 * Common operations over a page-addressed Langfuse collection.
 *
 * <p>
 * Implemented by every domain in this layer whose collection Langfuse addresses by page index.
 * Not part of the public API in its own right - each domain's operations interface (e.g.
 * {@code ModelOperations}) extends this to inherit {@code stream}/{@code streamPages}/
 * {@code findPage} and the operations derived from them, alongside its own domain-specific lookup
 * and write operations.
 *
 * @param <T> the type of the items in the collection
 * @see AbstractPagedOperations
 */
sealed interface PagedOperations<T>
        permits ModelOperations, DatasetOperations, LlmConnectionOperations, ScoreConfigOperations, AbstractPagedOperations {

    /**
     * All items, fetched using the page size configured by
     * {@code quarkus.langfuse.api.default-page-size}.
     *
     * @return every item
     */
    default List<T> findAll() {
        return find(PageSelection.all());
    }

    /**
     * The items on the selected pages.
     *
     * @param selection which pages to fetch
     * @return the items on those pages
     */
    default List<T> find(PageSelection selection) {
        return stream(selection).toList();
    }

    /**
     * All items, as a lazily paginated stream.
     *
     * @return every item
     * @see #stream(PageSelection)
     */
    default Stream<T> streamAll() {
        return stream(PageSelection.all());
    }

    /**
     * The items on the selected pages, as a lazily paginated stream.
     *
     * <p>
     * No request is made until the stream is consumed, and each page is fetched only once the
     * previous one has been exhausted. Because requests happen during traversal, a
     * {@link com.langfuse.api.LangfuseApiException} may be thrown from a terminal operation rather
     * than from this method.
     *
     * @param selection which pages to fetch
     * @return the items on those pages
     */
    Stream<T> stream(PageSelection selection);

    /**
     * The selected pages themselves, as a lazily paginated stream, for callers that need the
     * pagination metadata as they go.
     *
     * @param selection which pages to fetch
     * @return the selected pages
     */
    Stream<PagedResult<T>> streamPages(PageSelection selection);

    /**
     * Fetches exactly one page.
     *
     * @param page the page to fetch
     * @return the page, together with the totals reported by Langfuse
     */
    PagedResult<T> findPage(Page page);
}
