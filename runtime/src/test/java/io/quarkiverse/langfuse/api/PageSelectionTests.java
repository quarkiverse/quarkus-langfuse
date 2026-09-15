package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

class PageSelectionTests {

    @Test
    void allUsesConfiguredDefaultSize() {
        assertThat(PageSelection.all())
                .extracting(PageSelection::startIndex, PageSelection::pageSize, PageSelection::pageCount)
                .containsExactly(1, OptionalInt.empty(), OptionalInt.empty());
    }

    @Test
    void allWithSizeStartsAtFirstPageAndIsUnbounded() {
        assertThat(PageSelection.all(75))
                .extracting(PageSelection::startIndex, PageSelection::pageSize, PageSelection::pageCount)
                .containsExactly(1, OptionalInt.of(75), OptionalInt.empty());
    }

    @Test
    void fromIsUnbounded() {
        assertThat(PageSelection.from(Page.of(2, 75)))
                .extracting(PageSelection::startIndex, PageSelection::pageSize, PageSelection::pageCount)
                .containsExactly(2, OptionalInt.of(75), OptionalInt.empty());
    }

    @Test
    void onlySelectsASinglePage() {
        assertThat(PageSelection.only(Page.of(2, 75)))
                .extracting(PageSelection::startIndex, PageSelection::pageSize, PageSelection::pageCount)
                .containsExactly(2, OptionalInt.of(75), OptionalInt.of(1));
    }

    @Test
    void rangeEndIsExclusive() {
        assertThat(PageSelection.range(2, 4, 75))
                .extracting(PageSelection::startIndex, PageSelection::pageSize, PageSelection::pageCount)
                .containsExactly(2, OptionalInt.of(75), OptionalInt.of(2));
    }

    @Test
    void rangeClosedEndIsInclusive() {
        assertThat(PageSelection.rangeClosed(2, 4, 75))
                .extracting(PageSelection::startIndex, PageSelection::pageSize, PageSelection::pageCount)
                .containsExactly(2, OptionalInt.of(75), OptionalInt.of(3));
    }

    /**
     * Page indexes are 1-based, so a half-open range starting at 1 covers one fewer page than
     * its end index might suggest. Pinned because it is the most likely thing to be "corrected".
     */
    @Test
    void rangeFromFirstPageIsOneShorterThanEndIndex() {
        assertThat(PageSelection.range(1, 4, 50).pageCount()).hasValue(3);
        assertThat(PageSelection.rangeClosed(1, 4, 50).pageCount()).hasValue(4);
    }

    @Test
    void emptyRangesSelectNoPages() {
        assertThat(PageSelection.range(2, 2, 75).pageCount()).hasValue(0);
        assertThat(PageSelection.range(4, 2, 75).pageCount()).hasValue(0);
        assertThat(PageSelection.rangeClosed(4, 2, 75).pageCount()).hasValue(0);
    }

    @Test
    void singlePageRanges() {
        assertThat(PageSelection.range(2, 3, 75).pageCount()).hasValue(1);
        assertThat(PageSelection.rangeClosed(2, 2, 75).pageCount()).hasValue(1);
    }

    @Test
    void firstPageResolvesTheConfiguredDefaultSize() {
        assertThat(PageSelection.all().firstPage(50))
                .extracting(Page::index, Page::size)
                .containsExactly(1, 50);
    }

    @Test
    void firstPageKeepsAnExplicitSize() {
        assertThat(PageSelection.from(Page.of(3, 75)).firstPage(50))
                .extracting(Page::index, Page::size)
                .containsExactly(3, 75);
    }

    @Test
    void sizeMustBePositive() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> PageSelection.all(0))
                .withMessageContaining("Page size");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> PageSelection.range(1, 2, 0));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> PageSelection.rangeClosed(1, 2, -1));
    }

    @Test
    void startIndexMustBeOneBased() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> PageSelection.range(0, 3, 75))
                .withMessageContaining("1-based");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> PageSelection.rangeClosed(0, 3, 75));
    }
}
