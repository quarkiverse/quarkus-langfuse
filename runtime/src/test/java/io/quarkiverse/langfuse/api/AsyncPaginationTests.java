package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import io.smallrye.mutiny.Multi;

class AsyncPaginationTests {
    private static final int DEFAULT_SIZE = 50;
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    // --- page addressing -------------------------------------------------------------------

    @Test
    void walksEveryPageExactlyOnce() {
        var collection = FakeCollection.of(7);

        assertThat(collect(AsyncPagination.items(PageSelection.all(3), DEFAULT_SIZE, collection::pageAsync)))
                .containsExactlyElementsOf(allItems(7));

        assertThat(collection.requestCount()).isEqualTo(3);
    }

    @Test
    void usesTheDefaultPageSizeWhenTheSelectionDoesNotSpecifyOne() {
        var collection = FakeCollection.of(7);

        assertThat(collect(AsyncPagination.items(PageSelection.all(), DEFAULT_SIZE, collection::pageAsync)))
                .hasSize(7);

        assertThat(collection.requestCount()).isEqualTo(1);
    }

    @Test
    void nothingIsRequestedUntilSubscription() {
        var collection = FakeCollection.of(7);

        var multi = AsyncPagination.items(PageSelection.all(3), DEFAULT_SIZE, collection::pageAsync);

        assertThat(collection.requestCount()).isZero();

        assertThat(collect(multi)).hasSize(7);
        assertThat(collection.requestCount()).isEqualTo(3);
    }

    @Test
    void cancellingStopsTheTraversal() {
        var collection = FakeCollection.of(7);

        assertThat(AsyncPagination.items(PageSelection.all(3), DEFAULT_SIZE, collection::pageAsync)
                .select()
                .first()
                .toUni()
                .await()
                .atMost(TIMEOUT))
                .isEqualTo("item-1");

        assertThat(collection.requestCount()).isEqualTo(1);
    }

    @Test
    void boundedSelectionsStopAfterTheirLastPage() {
        var collection = FakeCollection.of(20);

        assertThat(collect(AsyncPagination.items(PageSelection.rangeClosed(2, 3, 3), DEFAULT_SIZE, collection::pageAsync)))
                .containsExactly("item-4", "item-5", "item-6", "item-7", "item-8", "item-9");

        assertThat(collection.requestCount()).isEqualTo(2);
    }

    @Test
    void anEmptySelectionRequestsNothing() {
        var collection = FakeCollection.of(7);

        assertThat(collect(AsyncPagination.items(PageSelection.range(2, 2, 3), DEFAULT_SIZE, collection::pageAsync)))
                .isEmpty();

        assertThat(collection.requestCount()).isZero();
    }

    @Test
    void anEmptyCollectionIsRequestedOnce() {
        var collection = FakeCollection.of(0);

        assertThat(collect(AsyncPagination.items(PageSelection.all(3), DEFAULT_SIZE, collection::pageAsync)))
                .isEmpty();

        assertThat(collection.requestCount()).isEqualTo(1);
    }

    /**
     * The last page must be emitted even though it is the one that ends the traversal.
     */
    @Test
    void pagesExposeTheirMetadataIncludingTheLastPage() {
        var collection = FakeCollection.of(7);

        assertThat(collect(AsyncPagination.pages(PageSelection.all(3), DEFAULT_SIZE, collection::pageAsync)))
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

        assertThat(collect(AsyncPagination.items(CursorSelection.all(3), DEFAULT_SIZE, collection::batchAsync)))
                .containsExactlyElementsOf(allItems(7));

        assertThat(collection.requestCount()).isEqualTo(3);
    }

    @Test
    void cursorTraversalStopsWhenNoFurtherCursorIsReported() {
        var collection = FakeCollection.of(6);

        assertThat(collect(AsyncPagination.items(CursorSelection.all(3), DEFAULT_SIZE, collection::batchAsync)))
                .hasSize(6);

        assertThat(collection.requestCount()).isEqualTo(2);
    }

    @Test
    void cancellingStopsTheCursorTraversal() {
        var collection = FakeCollection.of(7);

        assertThat(AsyncPagination.items(CursorSelection.all(3), DEFAULT_SIZE, collection::batchAsync)
                .select()
                .first()
                .toUni()
                .await()
                .atMost(TIMEOUT))
                .isEqualTo("item-1");

        assertThat(collection.requestCount()).isEqualTo(1);
    }

    @Test
    void boundedCursorSelectionsStopAfterTheirLastBatch() {
        var collection = FakeCollection.of(20);

        assertThat(collect(AsyncPagination.items(CursorSelection.first(2, 3), DEFAULT_SIZE, collection::batchAsync)))
                .containsExactly("item-1", "item-2", "item-3", "item-4", "item-5", "item-6");

        assertThat(collection.requestCount()).isEqualTo(2);
    }

    @Test
    void anEmptyCursorSelectionRequestsNothing() {
        var collection = FakeCollection.of(7);

        assertThat(collect(AsyncPagination.items(CursorSelection.first(0, 3), DEFAULT_SIZE, collection::batchAsync)))
                .isEmpty();

        assertThat(collection.requestCount()).isZero();
    }

    @Test
    void batchesAreEmittedIncludingTheLastOne() {
        var collection = FakeCollection.of(7);

        assertThat(collect(AsyncPagination.batches(CursorSelection.all(3), DEFAULT_SIZE, collection::batchAsync)))
                .hasSize(3)
                .extracting(CursorResult::hasNext)
                .containsExactly(true, true, false);
    }

    /**
     * Both trees must agree, item for item and request for request.
     */
    @Test
    void asyncAgreesWithSync() {
        var syncCollection = FakeCollection.of(11);
        var asyncCollection = FakeCollection.of(11);

        var sync = Pagination.items(PageSelection.all(4), DEFAULT_SIZE, syncCollection::page).toList();
        var async = collect(AsyncPagination.items(PageSelection.all(4), DEFAULT_SIZE, asyncCollection::pageAsync));

        assertThat(async).isEqualTo(sync);
        assertThat(asyncCollection.requestCount()).isEqualTo(syncCollection.requestCount());
    }

    private static <T> List<T> collect(Multi<T> multi) {
        return multi.collect()
                .asList()
                .await()
                .atMost(TIMEOUT);
    }

    private static List<String> allItems(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj("item-%d"::formatted)
                .toList();
    }
}
