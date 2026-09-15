package io.quarkiverse.langfuse.api;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import io.smallrye.mutiny.Uni;

/**
 * An in-memory stand-in for a paginated Langfuse collection, used to verify the traversal engines
 * without any HTTP involved.
 *
 * <p>
 * Records every request it serves, so tests can assert not just <em>what</em> came back but
 * <em>how many</em> requests it took - which is how laziness and short-circuiting are proven.
 */
final class FakeCollection {
    private final List<String> items;
    private final AtomicInteger requests = new AtomicInteger();

    private FakeCollection(List<String> items) {
        this.items = items;
    }

    static FakeCollection of(int itemCount) {
        return new FakeCollection(IntStream.rangeClosed(1, itemCount)
                .mapToObj("item-%d"::formatted)
                .toList());
    }

    int requestCount() {
        return this.requests.get();
    }

    int totalItems() {
        return this.items.size();
    }

    PagedResult<String> page(Page page) {
        this.requests.incrementAndGet();

        var from = Math.min((page.index() - 1) * page.size(), this.items.size());
        var to = Math.min(from + page.size(), this.items.size());

        return new DefaultPagedResult<>(this.items.subList(from, to), page, this.items.size(), totalPages(page.size()));
    }

    CursorResult<String> batch(Cursor cursor) {
        this.requests.incrementAndGet();

        var from = cursor.value()
                .map(Integer::parseInt)
                .orElse(0);
        var to = Math.min(from + cursor.limit(), this.items.size());
        var nextCursor = (to < this.items.size()) ? String.valueOf(to) : null;

        return new DefaultCursorResult<>(this.items.subList(Math.min(from, this.items.size()), to), cursor, nextCursor);
    }

    Uni<PagedResult<String>> pageAsync(Page page) {
        return Uni.createFrom().item(() -> page(page));
    }

    Uni<CursorResult<String>> batchAsync(Cursor cursor) {
        return Uni.createFrom().item(() -> batch(cursor));
    }

    private int totalPages(int pageSize) {
        return (this.items.size() + pageSize - 1) / pageSize;
    }
}
