package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class PaginationTests {
    private static final int DEFAULT_SIZE = 50;

    // --- page addressing -------------------------------------------------------------------

    @Test
    void walksEveryPageExactlyOnce() {
        var collection = FakeCollection.of(7);

        assertThat(Pagination.items(PageSelection.all(3), DEFAULT_SIZE, collection::page))
                .containsExactlyElementsOf(allItems(7));

        assertThat(collection.requestCount()).isEqualTo(3);
    }

    @Test
    void usesTheDefaultPageSizeWhenTheSelectionDoesNotSpecifyOne() {
        var collection = FakeCollection.of(7);

        assertThat(Pagination.items(PageSelection.all(), DEFAULT_SIZE, collection::page))
                .hasSize(7);

        assertThat(collection.requestCount()).isEqualTo(1);
    }

    @Test
    void nothingIsRequestedUntilTheStreamIsConsumed() {
        var collection = FakeCollection.of(7);

        var stream = Pagination.items(PageSelection.all(3), DEFAULT_SIZE, collection::page);

        assertThat(collection.requestCount()).isZero();

        assertThat(stream.toList()).hasSize(7);
        assertThat(collection.requestCount()).isEqualTo(3);
    }

    @Test
    void shortCircuitingStopsTheTraversal() {
        var collection = FakeCollection.of(7);

        assertThat(Pagination.items(PageSelection.all(3), DEFAULT_SIZE, collection::page).findFirst())
                .contains("item-1");

        assertThat(collection.requestCount()).isEqualTo(1);
    }

    @Test
    void findingAnItemOnTheSecondPageDoesNotRequestTheThird() {
        var collection = FakeCollection.of(7);

        assertThat(Pagination.items(PageSelection.all(3), DEFAULT_SIZE, collection::page)
                .filter("item-5"::equals)
                .findFirst())
                .contains("item-5");

        assertThat(collection.requestCount()).isEqualTo(2);
    }

    @Test
    void boundedSelectionsStopAfterTheirLastPage() {
        var collection = FakeCollection.of(20);

        assertThat(Pagination.items(PageSelection.rangeClosed(2, 3, 3), DEFAULT_SIZE, collection::page))
                .containsExactly("item-4", "item-5", "item-6", "item-7", "item-8", "item-9");

        assertThat(collection.requestCount()).isEqualTo(2);
    }

    @Test
    void onlySelectsASinglePage() {
        var collection = FakeCollection.of(20);

        assertThat(Pagination.items(PageSelection.only(Page.of(2, 3)), DEFAULT_SIZE, collection::page))
                .containsExactly("item-4", "item-5", "item-6");

        assertThat(collection.requestCount()).isEqualTo(1);
    }

    @Test
    void fromRunsToTheEnd() {
        var collection = FakeCollection.of(7);

        assertThat(Pagination.items(PageSelection.from(Page.of(2, 3)), DEFAULT_SIZE, collection::page))
                .containsExactly("item-4", "item-5", "item-6", "item-7");

        assertThat(collection.requestCount()).isEqualTo(2);
    }

    @Test
    void anEmptySelectionRequestsNothing() {
        var collection = FakeCollection.of(7);

        assertThat(Pagination.items(PageSelection.range(2, 2, 3), DEFAULT_SIZE, collection::page))
                .isEmpty();

        assertThat(collection.requestCount()).isZero();
    }

    @Test
    void anEmptyCollectionIsRequestedOnce() {
        var collection = FakeCollection.of(0);

        assertThat(Pagination.items(PageSelection.all(3), DEFAULT_SIZE, collection::page))
                .isEmpty();

        assertThat(collection.requestCount()).isEqualTo(1);
    }

    @Test
    void pagesExposeTheirMetadata() {
        var collection = FakeCollection.of(7);

        assertThat(Pagination.pages(PageSelection.all(3), DEFAULT_SIZE, collection::page))
                .hasSize(3)
                .extracting(PagedResult::totalItems, PagedResult::totalPages, PagedResult::hasNext)
                .containsExactly(
                        tuple(7, 3, true),
                        tuple(7, 3, true),
                        tuple(7, 3, false));
    }

    // --- cursor addressing -----------------------------------------------------------------

    @Test
    void walksEveryBatchExactlyOnce() {
        var collection = FakeCollection.of(7);

        assertThat(Pagination.items(CursorSelection.all(3), DEFAULT_SIZE, collection::batch))
                .containsExactlyElementsOf(allItems(7));

        assertThat(collection.requestCount()).isEqualTo(3);
    }

    @Test
    void cursorTraversalStopsWhenNoFurtherCursorIsReported() {
        var collection = FakeCollection.of(6);

        assertThat(Pagination.items(CursorSelection.all(3), DEFAULT_SIZE, collection::batch))
                .hasSize(6);

        assertThat(collection.requestCount()).isEqualTo(2);
    }

    @Test
    void cursorShortCircuitingStopsTheTraversal() {
        var collection = FakeCollection.of(7);

        assertThat(Pagination.items(CursorSelection.all(3), DEFAULT_SIZE, collection::batch).findFirst())
                .contains("item-1");

        assertThat(collection.requestCount()).isEqualTo(1);
    }

    @Test
    void boundedCursorSelectionsStopAfterTheirLastBatch() {
        var collection = FakeCollection.of(20);

        assertThat(Pagination.items(CursorSelection.first(2, 3), DEFAULT_SIZE, collection::batch))
                .containsExactly("item-1", "item-2", "item-3", "item-4", "item-5", "item-6");

        assertThat(collection.requestCount()).isEqualTo(2);
    }

    @Test
    void onlySelectsASingleBatch() {
        var collection = FakeCollection.of(20);

        assertThat(Pagination.items(CursorSelection.only(Cursor.at("3", 3)), DEFAULT_SIZE, collection::batch))
                .containsExactly("item-4", "item-5", "item-6");

        assertThat(collection.requestCount()).isEqualTo(1);
    }

    @Test
    void anEmptyCursorSelectionRequestsNothing() {
        var collection = FakeCollection.of(7);

        assertThat(Pagination.items(CursorSelection.first(0, 3), DEFAULT_SIZE, collection::batch))
                .isEmpty();

        assertThat(collection.requestCount()).isZero();
    }

    @Test
    void batchesCarryTheCursorForTheNextRequest() {
        var collection = FakeCollection.of(7);

        assertThat(Pagination.batches(CursorSelection.all(3), DEFAULT_SIZE, collection::batch))
                .hasSize(3)
                .last()
                .satisfies(batch -> assertThat(batch.hasNext()).isFalse());
    }

    private static List<String> allItems(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj("item-%d"::formatted)
                .toList();
    }
}
