package io.quarkiverse.langfuse.api;

/**
 * A single page coordinate: which page to request, and how many items it should contain.
 *
 * <p>
 * Page indexes are <strong>1-based</strong>, matching the Langfuse REST API and the Langfuse UI.
 * This differs from {@code io.quarkus.panache.common.Page}, which is 0-based.
 *
 * <p>
 * A {@link Page} identifies exactly one page. To describe which pages a traversal should visit,
 * use {@link PageSelection}.
 *
 * <pre>{@code
 * Page.of(2, 75); // page 2, 75 items per page
 * Page.ofSize(75); // page 1, 75 items per page
 * }</pre>
 *
 * @see PageSelection
 * @see PagedResult
 */
public sealed interface Page permits DefaultPage {

    /**
     * Creates a page coordinate.
     *
     * @param index the 1-based page index, must be greater than or equal to {@code 1}
     * @param size the number of items per page, must be greater than or equal to {@code 1}
     * @return the page coordinate
     * @throws IllegalArgumentException if {@code index} or {@code size} is less than {@code 1}
     */
    static Page of(int index, int size) {
        return new DefaultPage(index, size);
    }

    /**
     * Creates a coordinate for the first page with the given size.
     *
     * @param size the number of items per page, must be greater than or equal to {@code 1}
     * @return the page coordinate for page {@code 1}
     * @throws IllegalArgumentException if {@code size} is less than {@code 1}
     */
    static Page ofSize(int size) {
        return of(1, size);
    }

    /**
     * The 1-based index of this page.
     *
     * @return the page index
     */
    int index();

    /**
     * The number of items requested per page.
     *
     * @return the page size
     */
    int size();

    /**
     * The coordinate of the page immediately following this one, keeping the same size.
     *
     * <p>
     * This performs no bounds checking: it may describe a page beyond the end of the
     * available data, in which case Langfuse returns an empty page.
     *
     * @return the next page coordinate
     */
    Page next();
}
