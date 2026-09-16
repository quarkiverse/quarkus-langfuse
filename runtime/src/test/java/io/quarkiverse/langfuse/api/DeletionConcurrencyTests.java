package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

class DeletionConcurrencyTests {
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final List<String> NAMES = IntStream.rangeClosed(1, 8)
            .mapToObj("item-%d"::formatted)
            .toList();

    @Test
    void outcomesAreIdenticalAtEveryConcurrencyValue() {
        var results = IntStream.of(-1, 0, 1, 4, 100)
                .mapToObj(DeletionConcurrencyTests::deleteAllAt)
                .toList();

        assertThat(results)
                .hasSize(5)
                .allSatisfy(outcomes -> assertThat(outcomes).isEqualTo(results.get(0)))
                .allSatisfy(outcomes -> assertThat(outcomes)
                        .hasSize(8)
                        .containsOnlyKeys(NAMES.toArray(String[]::new)));
    }

    @Test
    void asyncOutcomesAreIdenticalAtEveryConcurrencyValue() {
        var results = IntStream.of(-1, 0, 1, 4, 100)
                .mapToObj(DeletionConcurrencyTests::deleteAllAsyncAt)
                .toList();

        assertThat(results)
                .hasSize(5)
                .allSatisfy(outcomes -> assertThat(outcomes).isEqualTo(results.get(0)))
                .allSatisfy(outcomes -> assertThat(outcomes).hasSize(8));
    }

    @Test
    void syncAndAsyncTreesAgreeOnOutcomes() {
        assertThat(deleteAllAt(4)).isEqualTo(deleteAllAsyncAt(4));
    }

    @Test
    void peakInFlightNeverExceedsTheConfiguredCeiling() {
        var collection = FakeCollection.of(8);

        var result = Deletions.deleteAll(NAMES, "Name", collection::resolve, collection::deleteTracked, 3);

        assertThat(collection.peakInFlight())
                .isGreaterThan(1)
                .isLessThanOrEqualTo(3);
        assertThat(result.deleted()).hasSize(8);
    }

    @Test
    void asyncPeakInFlightNeverExceedsTheConfiguredCeiling() {
        var collection = FakeCollection.of(8);

        var result = AsyncDeletions
                .deleteAll(NAMES, "Name", collection::resolveAsync, collection::deleteTrackedAsync, 3)
                .await().atMost(TIMEOUT);

        assertThat(collection.peakInFlight())
                .isGreaterThan(1)
                .isLessThanOrEqualTo(3);
        assertThat(result.deleted()).hasSize(8);
    }

    @Test
    void aCeilingAboveTheBatchSizeNeverExceedsTheBatchSize() {
        var collection = FakeCollection.of(8);

        Deletions.deleteAll(NAMES, "Name", collection::resolve, collection::deleteTracked, 100);

        assertThat(collection.peakInFlight()).isLessThanOrEqualTo(8);
    }

    @Test
    void aCeilingOfOneSubmitsNothingAndRunsOnTheCallingThread() {
        assertThat(List.of(-1, 0, 1)).allSatisfy(concurrency -> {
            var collection = FakeCollection.of(8);

            Deletions.deleteAll(NAMES, "Name", collection::resolve, collection::deleteTracked, concurrency);

            assertThat(collection.deletingThreadNames())
                    .as("concurrency %d must not submit any work", concurrency)
                    .containsExactly(Thread.currentThread().getName());
            assertThat(collection.peakInFlight()).isEqualTo(1);
        });
    }

    @Test
    void aSingleIdentifierRunsOnTheCallingThreadEvenAtAHighCeiling() {
        var collection = FakeCollection.of(8);

        Deletions.deleteAll(List.of("item-1"), "Name", collection::resolve, collection::deleteTracked, 8);

        assertThat(collection.deletingThreadNames()).containsExactly(Thread.currentThread().getName());
    }

    @Test
    void aBatchInvokedFromAWorkerThreadCompletesRatherThanDeadlocking() {
        var collection = FakeCollection.of(8);

        var result = CompletableFuture
                .supplyAsync(() -> Deletions.deleteAll(NAMES, "Name", collection::resolve, collection::delete, 4),
                        Infrastructure.getDefaultExecutor())
                .orTimeout(TIMEOUT.toSeconds(), java.util.concurrent.TimeUnit.SECONDS)
                .join();

        assertThat(result.deleted()).hasSize(8);
        assertThat(result.hasFailures()).isFalse();
    }

    @Test
    void everyIdentifierIsAttemptedWhenSomeFail() {
        var collection = FakeCollection.of(8);
        Consumer<String> delete = id -> {
            if (id.endsWith("3")) {
                throw new IllegalStateException("boom");
            }

            collection.delete(id);
        };

        var result = Deletions.deleteAll(NAMES, "Name", collection::resolve, delete, 4);

        assertThat(result.outcomes()).hasSize(8);
        assertThat(result.failed()).containsOnlyKeys("item-3");
        assertThat(result.deleted()).hasSize(7);
    }

    @Test
    void outcomesAreKeyedByIdentifierRegardlessOfCompletionOrder() {
        var collection = FakeCollection.of(8);

        var result = Deletions.deleteAll(NAMES, "Name", collection::resolve, collection::deleteTracked, 4);

        assertThat(NAMES).allSatisfy(name -> assertThat(result.outcome(name))
                .get()
                .satisfies(outcome -> assertThat(outcome.identifier()).isEqualTo(name)));
    }

    @Test
    void bothTreesIterateInInputOrderAtEveryConcurrencyValue() {
        assertThat(List.of(1, 2, 4, 8, 99)).allSatisfy(concurrency -> assertThat(deleteAllAt(concurrency).keySet())
                .containsExactlyElementsOf(NAMES));

        assertThat(List.of(1, 2, 4, 8, 99)).allSatisfy(concurrency -> assertThat(deleteAllAsyncAt(concurrency).keySet())
                .containsExactlyElementsOf(NAMES));
    }

    @Test
    void asyncIterationOrderFollowsInputRatherThanCompletionOrder() {
        var collection = FakeCollection.of(8);

        var result = AsyncDeletions.deleteAll(NAMES, "Name", collection::resolveAsync,
                collection.deleteAsyncDelayedInReverse(NAMES), 4)
                .await().atMost(TIMEOUT);

        assertThat(result.outcomes().keySet()).containsExactlyElementsOf(NAMES);
        assertThat(result.deleted()).containsExactlyElementsOf(NAMES);
    }

    @Test
    void anEmptyBatchEmitsAnEmptyResultRatherThanNeverCompleting() {
        var collection = FakeCollection.of(8);

        var result = AsyncDeletions.deleteAll(List.of(), "Name", collection::resolveAsync, collection::deleteAsync, 4)
                .await().atMost(TIMEOUT);

        assertThat(result.outcomes()).isEmpty();
        assertThat(result.size()).isZero();
    }

    private static java.util.Map<String, DeletionOutcome> deleteAllAt(int concurrency) {
        var collection = FakeCollection.of(8);
        Function<String, Optional<String>> resolve = collection::resolve;

        return Deletions.deleteAll(NAMES, "Name", resolve, collection::delete, concurrency).outcomes();
    }

    private static java.util.Map<String, DeletionOutcome> deleteAllAsyncAt(int concurrency) {
        var collection = FakeCollection.of(8);

        return AsyncDeletions.deleteAll(NAMES, "Name", collection::resolveAsync, collection::deleteAsync, concurrency)
                .await().atMost(TIMEOUT)
                .outcomes();
    }

    @Test
    void anAbsentNameYieldsNotFoundAtAnyConcurrency() {
        var collection = FakeCollection.of(8);

        var result = Deletions.deleteAll(List.of("item-1", "missing", "item-2"), "Name", collection::resolve,
                collection::delete, 4);

        assertThat(result.notFound()).containsExactly("missing");
        assertThat(collection.deletedIds()).hasSize(2);
    }

    @Test
    void anAsyncBatchNeverFailsItsUni() {
        var collection = FakeCollection.of(8);
        Function<String, Uni<?>> delete = id -> Uni.createFrom().failure(new IllegalStateException("boom"));

        var result = AsyncDeletions.deleteAll(NAMES, "Name", collection::resolveAsync, delete, 4)
                .await().atMost(TIMEOUT);

        assertThat(result.failed()).hasSize(8);
    }
}
