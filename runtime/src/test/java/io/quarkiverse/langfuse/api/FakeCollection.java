package io.quarkiverse.langfuse.api;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

import io.smallrye.mutiny.Uni;

/**
 * An in-memory stand-in for a paginated Langfuse collection, used to verify the traversal engines
 * without any HTTP involved.
 *
 * <p>
 * Records every request it serves, so tests can assert not just <em>what</em> came back but
 * <em>how many</em> requests it took - which is how laziness and short-circuiting are proven. The
 * same mechanism is what proves a delete unit issues <em>no</em> request for a name it could not
 * resolve.
 */
final class FakeCollection {
    private final List<String> items;
    private final AtomicInteger requests = new AtomicInteger();
    private final List<String> deleted = new CopyOnWriteArrayList<>();
    private final AtomicInteger inFlight = new AtomicInteger();
    private final AtomicInteger peakInFlight = new AtomicInteger();
    private final List<String> deletingThreads = new CopyOnWriteArrayList<>();

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

    // --- delete support --------------------------------------------------------------------

    /**
     * The ids passed to {@link #delete(String)}, in call order. Empty proves no delete was issued.
     */
    List<String> deletedIds() {
        return List.copyOf(this.deleted);
    }

    /**
     * Resolves a name the collection knows about, counting the lookup as a request.
     */
    Optional<String> resolve(String name) {
        this.requests.incrementAndGet();

        return this.items.stream()
                .filter(name::equals)
                .findFirst()
                .map("id-of-%s"::formatted);
    }

    /**
     * The asynchronous resolve contract: emits {@code null} rather than an empty {@link Optional} when
     * nothing matched, mirroring {@code AbstractAsyncPagedOperations.scanForName}.
     */
    Uni<String> resolveAsync(String name) {
        return Uni.createFrom().item(() -> resolve(name).orElse(null));
    }

    void delete(String id) {
        this.requests.incrementAndGet();
        this.deleted.add(id);
    }

    /**
     * The highest number of deletes observed running at the same time.
     */
    int peakInFlight() {
        return this.peakInFlight.get();
    }

    /**
     * The distinct thread names that ran a delete, used only to prove no work was submitted.
     */
    List<String> deletingThreadNames() {
        return this.deletingThreads.stream()
                .distinct()
                .toList();
    }

    /**
     * Deletes while recording peak overlap, pausing briefly so concurrent callers genuinely overlap.
     */
    void deleteTracked(String id) {
        this.deletingThreads.add(Thread.currentThread().getName());
        this.peakInFlight.accumulateAndGet(this.inFlight.incrementAndGet(), Math::max);

        try {
            Thread.sleep(20);
            delete(id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(e);
        } finally {
            this.inFlight.decrementAndGet();
        }
    }

    Uni<String> deleteTrackedAsync(String id) {
        return Uni.createFrom()
                .item(() -> {
                    this.peakInFlight.accumulateAndGet(this.inFlight.incrementAndGet(), Math::max);

                    return id;
                })
                .onItem().delayIt().by(Duration.ofMillis(20))
                .invoke(() -> {
                    delete(id);
                    this.inFlight.decrementAndGet();
                });
    }

    Uni<String> deleteAsync(String id) {
        return Uni.createFrom().item(() -> {
            delete(id);

            return id;
        });
    }

    // Delays each delete in proportion to how early its identifier appears, so the units complete in
    // reverse input order. Any result that still reads in input order got there by position rather than
    // by completion.
    Function<String, Uni<?>> deleteAsyncDelayedInReverse(List<String> ordered) {
        return id -> deleteAsync(id)
                .onItem().delayIt().by(Duration.ofMillis(10L * (ordered.size() - ordered.indexOf(id))));
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
