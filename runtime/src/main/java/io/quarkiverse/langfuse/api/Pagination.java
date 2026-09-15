package io.quarkiverse.langfuse.api;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Walks a paginated Langfuse collection lazily.
 *
 * <p>
 * Nothing is requested until the returned {@link Stream} is consumed, and each page or batch is
 * requested only when the previous one has been exhausted. A short-circuiting terminal operation -
 * {@code findFirst()}, {@code anyMatch()}, {@code limit()} - therefore stops the traversal without
 * requesting the pages it did not need.
 *
 * <p>
 * Handles both addressing models this layer supports: page-addressed collections, via
 * {@link #pages(PageSelection, int, PageFetcher)} / {@link #items(PageSelection, int, PageFetcher)},
 * and cursor-addressed ones, via
 * {@link #batches(CursorSelection, int, CursorFetcher)} / {@link #items(CursorSelection, int, CursorFetcher)}.
 * Both pairs are thin wrappers around the same private {@link #walk} - the only place the actual
 * traversal logic lives - so the two addressing models can never drift apart from each other.
 *
 * @see AsyncPagination the non-blocking counterpart of this class
 */
final class Pagination {

    private Pagination() {
    }

    /**
     * The pages selected by {@code selection}, each fetched only once the previous one has been
     * consumed.
     *
     * @param selection which pages to visit
     * @param defaultPageSize the page size to use if {@code selection} does not specify one
     * @param fetcher fetches a single page, given its coordinate
     * @param <T> the type of the items on each page
     * @return the selected pages, lazily
     */
    static <T> Stream<PagedResult<T>> pages(PageSelection selection, int defaultPageSize, PageFetcher<T> fetcher) {
        // PagedResult::nextPage is what tells the walk where to go after each page - Optional.empty()
        // when the page just fetched was the last one, ending the traversal.
        return walk(selection.firstPage(defaultPageSize), fetcher::fetch, PagedResult::nextPage, selection.pageCount());
    }

    /**
     * The individual items across the pages selected by {@code selection}, flattened out of their
     * pages.
     *
     * @param selection which pages to visit
     * @param defaultPageSize the page size to use if {@code selection} does not specify one
     * @param fetcher fetches a single page, given its coordinate
     * @param <T> the type of the items on each page
     * @return the items on the selected pages, lazily
     */
    static <T> Stream<T> items(PageSelection selection, int defaultPageSize, PageFetcher<T> fetcher) {
        // flatMap here is still lazy end to end: a page's items are only read once that page has
        // actually been fetched by the upstream pages() stream, and a short-circuiting operation over
        // the flattened items (e.g. findFirst()) still stops pages() from fetching a further page.
        return pages(selection, defaultPageSize, fetcher).flatMap(page -> page.items().stream());
    }

    /**
     * The batches selected by {@code selection}, each fetched only once the previous one has been
     * consumed.
     *
     * @param selection which batches to visit
     * @param defaultBatchSize the batch size to use if {@code selection} does not specify one
     * @param fetcher fetches a single batch, given its cursor
     * @param <T> the type of the items in each batch
     * @return the selected batches, lazily
     */
    static <T> Stream<CursorResult<T>> batches(CursorSelection selection, int defaultBatchSize, CursorFetcher<T> fetcher) {
        // CursorResult::nextCursor plays the same role here that PagedResult::nextPage plays in
        // pages(): it is the one place that decides whether the walk continues.
        return walk(selection.firstBatch(defaultBatchSize), fetcher::fetch, CursorResult::nextCursor, selection.batchCount());
    }

    /**
     * The individual items across the batches selected by {@code selection}, flattened out of their
     * batches.
     *
     * @param selection which batches to visit
     * @param defaultBatchSize the batch size to use if {@code selection} does not specify one
     * @param fetcher fetches a single batch, given its cursor
     * @param <T> the type of the items in each batch
     * @return the items in the selected batches, lazily
     */
    static <T> Stream<T> items(CursorSelection selection, int defaultBatchSize, CursorFetcher<T> fetcher) {
        return batches(selection, defaultBatchSize, fetcher).flatMap(batch -> batch.items().stream());
    }

    /**
     * The single traversal both addressing models are built on: start somewhere, fetch one result, ask
     * that result where to go next, and stop either when there is nowhere left to go or when
     * {@code maxResults} has been reached.
     *
     * <p>
     * {@code R} is the fetched result type ({@link PagedResult} or {@link CursorResult}); {@code S} is
     * whatever identifies the next request ({@link Page} or {@link Cursor}). Expressing the walk once in
     * terms of these two type parameters is what lets page-addressed and cursor-addressed collections
     * share one traversal engine despite having no common request or result type.
     *
     * @param start the first request to make
     * @param fetch performs one request and returns its result
     * @param next given a result, the next request to make, or empty if there is none
     * @param maxResults the maximum number of results to fetch, or empty to fetch until {@code next}
     *        reports there are no more
     * @param <R> the type of each fetched result
     * @param <S> the type of the request describing which result to fetch next
     * @return the results of the walk, lazily
     */
    private static <R, S> Stream<R> walk(S start, Function<S, R> fetch, Function<R, Optional<S>> next,
            OptionalInt maxResults) {
        // maxResults is empty for an unbounded selection (PageSelection.all(), CursorSelection.all()),
        // and present otherwise. A bound of 0 (e.g. PageSelection.range(2, 2, ...)) is handled here,
        // before the spliterator is even created, so an empty selection costs no request at all rather
        // than being fetched and then discarded by limit(0).
        return maxResults.stream()
                .boxed()
                .findFirst()
                .map(max -> (max == 0)
                        ? Stream.<R> empty()
                        : lazyStream(start, fetch, next).limit(max))
                .orElseGet(() -> lazyStream(start, fetch, next));
    }

    private static <R, S> Stream<R> lazyStream(S start, Function<S, R> fetch, Function<R, Optional<S>> next) {
        // false: sequential. Parallel would let the JDK fork tryAdvance calls across threads, which
        // would fetch pages out of order and ahead of what a caller's short-circuiting terminal
        // operation actually needs - defeating the entire point of this class.
        return StreamSupport.stream(new WalkingSpliterator<>(start, fetch, next), false);
    }

    /**
     * Where the laziness actually lives. A {@link Stream} built from this spliterator does not fetch
     * anything on construction; it fetches exactly one result per call to {@link #tryAdvance}, and
     * {@code tryAdvance} is only ever called by a terminal operation pulling for another element. A
     * short-circuiting terminal operation - {@code findFirst()}, {@code anyMatch()}, {@code limit(n)} -
     * simply stops calling it, so results beyond what was needed are never fetched.
     *
     * @param <R> the type of each fetched result
     * @param <S> the type of the request describing which result to fetch next
     */
    private static final class WalkingSpliterator<R, S> extends AbstractSpliterator<R> {
        private final Function<S, R> fetch;
        private final Function<R, Optional<S>> next;
        // The request to make on the next tryAdvance call, or null once the collection is exhausted.
        // null is what makes tryAdvance stop returning elements - it is not a sentinel error value.
        private S pending;

        private WalkingSpliterator(S start, Function<S, R> fetch, Function<R, Optional<S>> next) {
            // Long.MAX_VALUE: the true size isn't known up front (that's the whole reason this is lazy),
            // so report the size as unbounded rather than guessing. ORDERED and NONNULL describe what
            // this spliterator actually guarantees: results arrive in fetch order, and a fetched result is
            // never itself null. IMMUTABLE: this spliterator's own state is only ever touched from within
            // tryAdvance, never concurrently or externally.
            super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
            this.pending = start;
            this.fetch = fetch;
            this.next = next;
        }

        @Override
        public boolean tryAdvance(Consumer<? super R> action) {
            var request = this.pending;
            var hasMore = request != null;

            if (hasMore) {
                // The one and only place a request is issued. Compute what comes after this result before
                // handing the result to the caller, so a caller that stops consuming here (e.g.
                // findFirst()) never causes a further request to be prepared, let alone made.
                var result = this.fetch.apply(request);
                this.pending = this.next.apply(result).orElse(null);
                action.accept(result);
            }

            return hasMore;
        }
    }
}
