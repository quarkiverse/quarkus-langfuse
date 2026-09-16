package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApiException;

import io.quarkiverse.langfuse.api.DeletionOutcome.Deleted;
import io.quarkiverse.langfuse.api.DeletionOutcome.Failed;
import io.quarkiverse.langfuse.api.DeletionOutcome.NotFound;
import io.smallrye.mutiny.Uni;

class DeletionBatchTests {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final String LABEL = "Model name";
    private static final int CONCURRENCY = 4;
    private static final Function<String, Optional<String>> RESOLVE_AS_ID = Optional::of;
    private static final Function<String, Uni<String>> RESOLVE_AS_ID_ASYNC = id -> Uni.createFrom().item(id);

    // --- validation happens before any request ----------------------------------------------

    @Test
    void aBlankIdentifierAnywhereThrowsBeforeIssuingAnyRequest() {
        var collection = FakeCollection.of(3);

        // The blank sits last, after two identifiers that would have deleted successfully. Nothing may be
        // deleted: that is the entire point of validating the whole set up front.
        assertThatThrownBy(() -> Deletions.deleteAll(List.of("item-1", "item-2", "  "), LABEL,
                collection::resolve, collection::delete, CONCURRENCY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(LABEL);

        assertThat(collection.deletedIds()).isEmpty();
        assertThat(collection.requestCount()).isZero();
    }

    @Test
    void aNullElementThrowsIllegalArgumentRatherThanNullPointer() {
        var collection = FakeCollection.of(3);

        // Arrays.asList tolerates a null element, so validation - not the conversion - is what rejects it.
        // List.of would have thrown NullPointerException here and preempted our own contract.
        assertThatThrownBy(() -> Deletions.deleteAll(Arrays.asList("item-1", null), LABEL,
                collection::resolve, collection::delete, CONCURRENCY))
                .isInstanceOf(IllegalArgumentException.class)
                .isNotInstanceOf(NullPointerException.class);

        assertThat(collection.requestCount()).isZero();
    }

    @Test
    void aNullCollectionThrows() {
        var collection = FakeCollection.of(3);

        assertThatThrownBy(() -> Deletions.deleteAll(null, LABEL, collection::resolve, collection::delete, CONCURRENCY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(LABEL);
    }

    @Test
    void validationThrowsSynchronouslyFromTheAsyncCallRatherThanAsAFailedUni() {
        var collection = FakeCollection.of(3);

        // Malformed input is a programming error, not an asynchronous outcome: it must surface at the
        // call, without the caller having to subscribe to discover it.
        assertThatThrownBy(() -> AsyncDeletions.deleteAll(List.of("item-1", " "), LABEL,
                collection::resolveAsync, collection::deleteAsync, CONCURRENCY))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(collection.requestCount()).isZero();
    }

    // --- dedup and the empty case ------------------------------------------------------------

    @Test
    void duplicatesCollapseToOneOutcomeAndOneDeleteAttempt() {
        var collection = FakeCollection.of(3);

        var result = Deletions.deleteAll(List.of("item-1", "item-2", "item-1", "item-1"), LABEL,
                collection::resolve, collection::delete, CONCURRENCY);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.deleted()).containsExactly("item-1", "item-2");
        assertThat(collection.deletedIds()).containsExactly("id-of-item-1", "id-of-item-2");
    }

    @Test
    void anEmptyIdentifierSetIssuesNoRequests() {
        var collection = FakeCollection.of(3);

        var result = Deletions.deleteAll(List.of(), LABEL, collection::resolve, collection::delete, CONCURRENCY);

        assertThat(result.outcomes()).isEmpty();
        assertThat(result.hasFailures()).isFalse();
        assertThat(collection.requestCount()).isZero();
    }

    @Test
    void anEmptyIdentifierSetIssuesNoRequestsAsync() {
        var collection = FakeCollection.of(3);

        assertThat(await(AsyncDeletions.deleteAll(List.of(), LABEL, collection::resolveAsync,
                collection::deleteAsync, CONCURRENCY)).outcomes()).isEmpty();

        assertThat(collection.requestCount()).isZero();
    }

    // --- accumulation ------------------------------------------------------------------------

    @Test
    void aMixedBatchReturnsEveryOutcomeKeyedByIdentifier() {
        var collection = FakeCollection.of(3);
        var cause = new LangfuseApiException("unauthorized");
        var result = Deletions.deleteAll(List.of("item-1", "absent", "item-3"), LABEL,
                collection::resolve, failOnlyFor("id-of-item-3", cause, collection), CONCURRENCY);

        assertThat(result.outcome("item-1")).get().isInstanceOf(Deleted.class);
        assertThat(result.outcome("absent")).get().isInstanceOf(NotFound.class);
        assertThat(result.outcome("item-3")).get()
                .isInstanceOf(Failed.class)
                .extracting(outcome -> ((Failed) outcome).cause())
                .isEqualTo(cause);

        assertThat(result.hasFailures()).isTrue();
    }

    @Test
    void oneFailureDoesNotPreventTheRemainingIdentifiersFromBeingAttempted() {
        var collection = FakeCollection.of(3);

        // The failure is deliberately first, so anything after it proves accumulation rather than
        // fail-fast.
        var result = Deletions.deleteAll(List.of("item-1", "item-2", "item-3"), LABEL,
                collection::resolve, failOnlyFor("id-of-item-1", new LangfuseApiException("boom"), collection),
                CONCURRENCY);

        assertThat(result.failed()).containsOnlyKeys("item-1");
        assertThat(result.deleted()).containsExactly("item-2", "item-3");
        assertThat(collection.deletedIds()).containsExactly("id-of-item-2", "id-of-item-3");
    }

    @Test
    void aMixedBatchReturnsEveryOutcomeAsync() {
        var collection = FakeCollection.of(3);
        var cause = new LangfuseApiException("unauthorized");
        var result = await(AsyncDeletions.deleteAll(List.of("item-1", "absent", "item-3"), LABEL,
                collection::resolveAsync,
                id -> "id-of-item-3".equals(id) ? Uni.createFrom().failure(cause) : collection.deleteAsync(id),
                CONCURRENCY));

        assertThat(result.deleted()).containsExactly("item-1");
        assertThat(result.notFound()).containsExactly("absent");
        assertThat(result.failed()).containsOnlyKeys("item-3");
    }

    @Test
    void theCollectionAndVarargsFormsAgreeForEquivalentInput() {
        var viaCollection = Deletions.deleteAll(List.of("item-1", "item-2"), LABEL,
                RESOLVE_AS_ID, FakeCollection.of(2)::delete, CONCURRENCY);
        var viaVarargs = Deletions.deleteAll(Arrays.asList("item-1", "item-2"), LABEL,
                RESOLVE_AS_ID, FakeCollection.of(2)::delete, CONCURRENCY);

        assertThat(viaVarargs.outcomes().keySet()).isEqualTo(viaCollection.outcomes().keySet());
    }

    @Test
    void theBatchIsLazyUntilSubscriptionAsync() {
        var collection = FakeCollection.of(3);

        var uni = AsyncDeletions.deleteAll(List.of("item-1", "item-2"), LABEL,
                collection::resolveAsync, collection::deleteAsync, CONCURRENCY);

        assertThat(collection.requestCount()).isZero();

        assertThat(await(uni).deleted()).containsExactly("item-1", "item-2");
    }

    @Test
    void bothTreesProduceTheSameOutcomesForTheSameInput() {
        var syncCollection = FakeCollection.of(3);
        var asyncCollection = FakeCollection.of(3);
        var identifiers = List.of("item-1", "absent", "item-2", "item-1");

        var sync = Deletions.deleteAll(identifiers, LABEL, syncCollection::resolve, syncCollection::delete,
                CONCURRENCY);
        var async = await(AsyncDeletions.deleteAll(identifiers, LABEL, asyncCollection::resolveAsync,
                asyncCollection::deleteAsync, CONCURRENCY));

        assertThat(async.outcomes().keySet()).isEqualTo(sync.outcomes().keySet());
        assertThat(async.deleted()).isEqualTo(sync.deleted());
        assertThat(async.notFound()).isEqualTo(sync.notFound());
    }

    private static Consumer<String> failOnlyFor(String failingId, Throwable cause, FakeCollection collection) {
        return id -> {
            if (failingId.equals(id)) {
                throwUnchecked(cause);
            } else {
                collection.delete(id);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwUnchecked(Throwable cause) throws T {
        throw (T) cause;
    }

    private static DeletionResult await(Uni<DeletionResult> uni) {
        return uni.await().atMost(TIMEOUT);
    }
}
