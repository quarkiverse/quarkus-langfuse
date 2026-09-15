package io.quarkiverse.langfuse.api;

import java.util.List;
import java.util.Optional;

/**
 * A single page of results together with the pagination metadata reported by Langfuse.
 *
 * <p>
 * Returned by the {@code findPage} operations, and emitted by the {@code streamPages} operations,
 * for callers who need the totals or want to page manually:
 *
 * <pre>{@code
 * var page = langfuse.models().findPage(Page.of(2, 75));
 *
 * page.nextPage()
 *     .map(langfuse.models()::findPage)
 *     .ifPresent(next -> ...);
 * }</pre>
 *
 * @param <T> the type of the items on the page
 * @see Page
 * @see PageSelection
 */
public sealed interface PagedResult<T> permits DefaultPagedResult {

    /**
     * The items on this page, in the order returned by Langfuse.
     *
     * <p>
     * Never {@code null}. Empty when the requested page lies beyond the available data.
     *
     * @return an unmodifiable list of the items on this page
     */
    List<T> items();

    /**
     * The coordinate these items were requested with.
     *
     * @return the page coordinate
     */
    Page page();

    /**
     * The total number of items across all pages, as reported by Langfuse.
     *
     * @return the total item count
     */
    int totalItems();

    /**
     * The total number of pages available at this page size, as reported by Langfuse.
     *
     * @return the total page count
     */
    int totalPages();

    /**
     * The coordinate of the next page, or empty when this is the last page.
     *
     * @return the next page coordinate, if there is one
     */
    Optional<Page> nextPage();

    /**
     * Whether a page follows this one.
     *
     * @return {@code true} if {@link #nextPage()} is present
     */
    default boolean hasNext() {
        return nextPage().isPresent();
    }
}
