package io.quarkiverse.langfuse.api;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * Walks a paginated Langfuse collection lazily, without blocking.
 *
 * <p>
 * The asynchronous counterpart of {@link Pagination}. Nothing is requested until the returned
 * {@link Multi} is subscribed to, and each page or batch is requested only when the previous one has
 * been consumed. Cancelling the subscription - which {@code select().first(n)} and
 * {@code toUni()} do - stops the traversal without requesting the pages it did not need.
 *
 * <p>
 * Structured identically to {@link Pagination}: both addressing models this layer supports reduce to
 * one private {@link #walk}, so the two classes stay in lock-step with each other. Nothing here
 * blocks, and nothing in {@link Pagination} is reused by delegation - the two are independent
 * implementations over the synchronous and asynchronous halves of the generated client.
 *
 * @see Pagination the blocking counterpart of this class
 */
final class AsyncPagination {

    private AsyncPagination() {
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
    static <T> Multi<PagedResult<T>> pages(PageSelection selection, int defaultPageSize, AsyncPageFetcher<T> fetcher) {
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
    static <T> Multi<T> items(PageSelection selection, int defaultPageSize, AsyncPageFetcher<T> fetcher) {
        // createFrom().iterable(...) on an already-fetched page's items is synchronous and instant - the
        // only asynchronous step in this whole chain is fetching the page itself, upstream in pages().
        return pages(selection, defaultPageSize, fetcher)
                .flatMap(page -> Multi.createFrom().iterable(page.items()));
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
    static <T> Multi<CursorResult<T>> batches(CursorSelection selection, int defaultBatchSize,
            AsyncCursorFetcher<T> fetcher) {
        return walk(selection.firstBatch(defaultBatchSize), fetcher::fetch, CursorResult::nextCursor,
                selection.batchCount());
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
    static <T> Multi<T> items(CursorSelection selection, int defaultBatchSize, AsyncCursorFetcher<T> fetcher) {
        return batches(selection, defaultBatchSize, fetcher)
                .flatMap(batch -> Multi.createFrom().iterable(batch.items()));
    }

    /**
     * The asynchronous mirror of {@code Pagination.walk}: same shape (start, fetch, decide what's
     * next, stop at a bound or when there's nothing left), but {@code fetch} returns a {@link Uni}
     * instead of a plain value, since each request is a non-blocking HTTP call.
     *
     * @param start the first request to make
     * @param fetch performs one request and returns a {@link Uni} of its result
     * @param next given a result, the next request to make, or empty if there is none
     * @param maxResults the maximum number of results to fetch, or empty to fetch until {@code next}
     *        reports there are no more
     * @param <R> the type of each fetched result
     * @param <S> the type of the request describing which result to fetch next
     * @return the results of the walk, as a lazily subscribed {@link Multi}
     */
    private static <R, S> Multi<R> walk(S start, Function<S, Uni<R>> fetch, Function<R, Optional<S>> next,
            OptionalInt maxResults) {
        // See Pagination.walk: a bound of 0 short-circuits before any Multi is even built, so an empty
        // selection costs no request. select().first(max) elsewhere in this method is what turns a
        // caller's subscription cancellation into the traversal actually stopping - see lazyMulti.
        return maxResults.stream()
                .boxed()
                .findFirst()
                .map(max -> (max == 0)
                        ? Multi.createFrom().<R> empty()
                        : lazyMulti(start, fetch, next).select().first(max))
                .orElseGet(() -> lazyMulti(start, fetch, next));
    }

    /**
     * Built on Mutiny's {@code repeating()}, which re-invokes the item supplier for as long as
     * {@code whilst} holds, threading state between invocations via the mutable container the state
     * supplier creates once at subscription time - here an {@link AtomicReference} holding the request
     * to make next.
     *
     * <p>
     * Each invocation fetches using whatever request is currently in the reference, then - before the
     * item is even emitted downstream, via {@code invoke} - overwrites the reference with the request
     * for the following call, or {@code null} once {@code next} reports there is nothing further.
     * {@code whilst} is evaluated against the item that was just fetched, and Mutiny evaluates it
     * <strong>after</strong> emitting that item, not before: the item that ends the traversal is still
     * delivered downstream, it simply is not followed by another fetch. Confirmed by
     * {@code AsyncPaginationTests.pagesExposeTheirMetadataIncludingTheLastPage} - if this were reversed,
     * the final page would silently disappear.
     *
     * <p>
     * Cancelling the subscription - which {@code select().first(n)} does in {@link #walk} - stops
     * {@code repeating()} from invoking the supplier again, so a short-circuiting subscriber never
     * causes a request beyond what it consumed.
     *
     * @param start the first request to make
     * @param fetch performs one request and returns a {@link Uni} of its result
     * @param next given a result, the next request to make, or empty if there is none
     * @param <R> the type of each fetched result
     * @param <S> the type of the request describing which result to fetch next
     * @return the results of the walk, as an unbounded, lazily subscribed {@link Multi}
     */
    private static <R, S> Multi<R> lazyMulti(S start, Function<S, Uni<R>> fetch, Function<R, Optional<S>> next) {
        return Multi.createBy()
                .repeating()
                .uni(() -> new AtomicReference<>(start),
                        // state.get(): the request to make this time. state.set(...): recorded for next
                        // time, before this item reaches whilst() or any downstream subscriber.
                        state -> fetch.apply(state.get())
                                .invoke(result -> state.set(next.apply(result).orElse(null))))
                .whilst(result -> next.apply(result).isPresent());
    }
}
