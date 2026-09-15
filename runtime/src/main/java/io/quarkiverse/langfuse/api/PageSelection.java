package io.quarkiverse.langfuse.api;

import java.util.OptionalInt;
import java.util.stream.IntStream;

/**
 * Describes <em>which</em> pages a paginated traversal should visit.
 *
 * <p>
 * Where a {@link Page} is a coordinate ("page 2, 75 items per page"), a {@link PageSelection}
 * is an intent ("page 2 and everything after it", "only page 2", "pages 2 through 4"). The
 * factory method used makes that intent explicit at the call site:
 *
 * <pre>{@code
 * PageSelection.all(); // every page, using the configured default page size
 * PageSelection.all(75); // every page, 75 items per page
 * PageSelection.from(Page.of(2, 75)); // page 2 and everything after it
 * PageSelection.only(Page.of(2, 75)); // page 2, and nothing else
 * PageSelection.range(2, 4, 75); // pages 2 and 3       (end exclusive)
 * PageSelection.rangeClosed(2, 4, 75); // pages 2, 3 and 4    (end inclusive)
 * }</pre>
 *
 * <p>
 * {@link #range(int, int, int)} and {@link #rangeClosed(int, int, int)} mirror
 * {@link IntStream#range(int, int)} and
 * {@link IntStream#rangeClosed(int, int)}. Because page indexes are 1-based,
 * {@code range(1, 4, 50)} selects three pages (1, 2 and 3), not four.
 *
 * @see Page
 * @see PagedResult
 */
public sealed interface PageSelection permits AllPages, FromPage, SinglePage, PageRange {

    /**
     * Every page, from the first to the last, using the page size configured by
     * {@code quarkus.langfuse.api.default-page-size}.
     *
     * @return the selection
     */
    static PageSelection all() {
        return new AllPages(OptionalInt.empty());
    }

    /**
     * Every page, from the first to the last, using the given page size.
     *
     * @param size the number of items per page, must be greater than or equal to {@code 1}
     * @return the selection
     * @throws IllegalArgumentException if {@code size} is less than {@code 1}
     */
    static PageSelection all(int size) {
        return new AllPages(OptionalInt.of(requireValidSize(size)));
    }

    /**
     * The given page and every page after it, up to the last.
     *
     * @param start the first page to visit
     * @return the selection
     */
    static PageSelection from(Page start) {
        return new FromPage(start);
    }

    /**
     * The given page only.
     *
     * @param page the single page to visit
     * @return the selection
     */
    static PageSelection only(Page page) {
        return new SinglePage(page);
    }

    /**
     * The pages from {@code startIndexInclusive} up to, but not including, {@code endIndexExclusive}.
     *
     * <p>
     * Mirrors {@link IntStream#range(int, int)}: if {@code endIndexExclusive} is less
     * than or equal to {@code startIndexInclusive}, the selection is empty and no request is made.
     *
     * @param startIndexInclusive the 1-based index of the first page, must be greater than or equal to {@code 1}
     * @param endIndexExclusive the 1-based index of the first page <em>not</em> to visit
     * @param size the number of items per page, must be greater than or equal to {@code 1}
     * @return the selection
     * @throws IllegalArgumentException if {@code startIndexInclusive} or {@code size} is less than {@code 1}
     */
    static PageSelection range(int startIndexInclusive, int endIndexExclusive, int size) {
        return new PageRange(Page.of(startIndexInclusive, size),
                Math.max(0, endIndexExclusive - startIndexInclusive));
    }

    /**
     * The pages from {@code startIndexInclusive} up to and including {@code endIndexInclusive}.
     *
     * <p>
     * Mirrors {@link IntStream#rangeClosed(int, int)}: if {@code endIndexInclusive} is
     * less than {@code startIndexInclusive}, the selection is empty and no request is made.
     *
     * @param startIndexInclusive the 1-based index of the first page, must be greater than or equal to {@code 1}
     * @param endIndexInclusive the 1-based index of the last page to visit
     * @param size the number of items per page, must be greater than or equal to {@code 1}
     * @return the selection
     * @throws IllegalArgumentException if {@code startIndexInclusive} or {@code size} is less than {@code 1}
     */
    static PageSelection rangeClosed(int startIndexInclusive, int endIndexInclusive, int size) {
        return new PageRange(Page.of(startIndexInclusive, size),
                Math.max(0, (endIndexInclusive - startIndexInclusive) + 1));
    }

    /**
     * The 1-based index of the first page to visit.
     *
     * @return the starting page index
     */
    int startIndex();

    /**
     * The number of items per page, or empty when the configured default should be used.
     *
     * @return the page size, if this selection specifies one
     */
    OptionalInt pageSize();

    /**
     * The maximum number of pages to visit, or empty when the traversal should continue
     * until the last available page.
     *
     * @return the page count, if this selection is bounded
     */
    OptionalInt pageCount();

    /**
     * Resolves this selection to the coordinate of its first page, applying the given default
     * page size when this selection does not specify one.
     *
     * @param defaultPageSize the page size to use when this selection does not specify one
     * @return the coordinate of the first page to request
     * @throws IllegalArgumentException if {@code defaultPageSize} is less than {@code 1}
     */
    default Page firstPage(int defaultPageSize) {
        return Page.of(startIndex(), pageSize().orElse(defaultPageSize));
    }

    private static int requireValidSize(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Page size must be greater than or equal to 1, but was %d".formatted(size));
        }

        return size;
    }
}
