package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class PageTests {

    @Test
    void ofCreatesCoordinate() {
        assertThat(Page.of(2, 75))
                .isNotNull()
                .extracting(Page::index, Page::size)
                .containsExactly(2, 75);
    }

    @Test
    void ofSizeStartsAtFirstPage() {
        assertThat(Page.ofSize(75))
                .extracting(Page::index, Page::size)
                .containsExactly(1, 75);
    }

    @Test
    void nextIncrementsIndexAndKeepsSize() {
        assertThat(Page.of(2, 75).next())
                .extracting(Page::index, Page::size)
                .containsExactly(3, 75);
    }

    @Test
    void nextIsNotBoundsChecked() {
        assertThat(Page.of(Integer.MAX_VALUE - 1, 10).next().index())
                .isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void pagesAreValueObjects() {
        assertThat(Page.of(2, 75))
                .isEqualTo(Page.of(2, 75))
                .hasSameHashCodeAs(Page.of(2, 75))
                .isNotEqualTo(Page.of(3, 75))
                .isNotEqualTo(Page.of(2, 50));
    }

    @Test
    void indexMustBeOneBased() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Page.of(0, 75))
                .withMessageContaining("1-based");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> Page.of(-1, 75));
    }

    @Test
    void sizeMustBePositive() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Page.of(1, 0))
                .withMessageContaining("Page size");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> Page.ofSize(-1));
    }
}
