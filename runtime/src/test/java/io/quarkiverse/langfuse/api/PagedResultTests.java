package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class PagedResultTests {

    @Test
    void exposesItemsAndMetadata() {
        assertThat(new DefaultPagedResult<>(List.of("a", "b", "c"), Page.of(2, 3), 7, 3))
                .satisfies(result -> assertThat(result.items()).containsExactly("a", "b", "c"))
                .extracting(PagedResult::page, PagedResult::totalItems, PagedResult::totalPages)
                .containsExactly(Page.of(2, 3), 7, 3);
    }

    @Test
    void nextPageKeepsTheSameSize() {
        var result = new DefaultPagedResult<>(List.of("a"), Page.of(2, 3), 7, 3);

        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextPage())
                .isPresent()
                .contains(Page.of(3, 3));
    }

    @Test
    void lastPageHasNoNextPage() {
        var result = new DefaultPagedResult<>(List.of("g"), Page.of(3, 3), 7, 3);

        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextPage()).isEmpty();
    }

    @Test
    void pastTheEndPageHasNoNextPage() {
        assertThat(new DefaultPagedResult<>(List.of(), Page.of(9, 3), 7, 3).hasNext())
                .isFalse();
    }

    @Test
    void emptyResultHasNoNextPage() {
        assertThat(new DefaultPagedResult<>(List.of(), Page.of(1, 50), 0, 0))
                .satisfies(result -> assertThat(result.items()).isEmpty())
                .extracting(PagedResult::hasNext, PagedResult::totalItems, PagedResult::totalPages)
                .containsExactly(false, 0, 0);
    }

    @Test
    void itemsAreDefensivelyCopiedAndUnmodifiable() {
        var source = new ArrayList<>(List.of("a", "b"));
        var result = new DefaultPagedResult<>(source, Page.of(1, 50), 2, 1);

        source.add("c");

        assertThat(result.items()).containsExactly("a", "b");
        assertThatThrownBy(() -> result.items().add("d"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void nullItemsBecomeAnEmptyList() {
        assertThat(new DefaultPagedResult<String>(null, Page.of(1, 50), 0, 0).items())
                .isNotNull()
                .isEmpty();
    }

    @Test
    void negativeTotalsAreClampedToZero() {
        assertThat(new DefaultPagedResult<>(List.of(), Page.of(1, 50), -1, -1))
                .extracting(PagedResult::totalItems, PagedResult::totalPages)
                .containsExactly(0, 0);
    }

    @Test
    void pageIsRequired() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DefaultPagedResult<>(List.of(), null, 0, 0))
                .withMessageContaining("Page must not be null");
    }
}
