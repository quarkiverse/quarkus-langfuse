package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApiException;

import io.quarkiverse.langfuse.api.DeletionOutcome.Deleted;
import io.quarkiverse.langfuse.api.DeletionOutcome.Failed;
import io.quarkiverse.langfuse.api.DeletionOutcome.NotFound;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.smallrye.mutiny.Uni;

class DeletionUnitTests {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final Function<String, Optional<String>> RESOLVE_AS_ID = Optional::of;
    private static final Function<String, Uni<String>> RESOLVE_AS_ID_ASYNC = id -> Uni.createFrom().item(id);

    // --- synchronous -----------------------------------------------------------------------

    @Test
    void deletingAResolvedIdentifierYieldsDeleted() {
        var collection = FakeCollection.of(3);

        assertThat(Deletions.deleteOne("item-2", collection::resolve, collection::delete))
                .isInstanceOf(Deleted.class)
                .extracting(DeletionOutcome::identifier)
                .isEqualTo("item-2");

        assertThat(collection.deletedIds()).containsExactly("id-of-item-2");
    }

    @Test
    void anUnresolvedNameYieldsNotFoundAndIssuesNoDeleteRequest() {
        var collection = FakeCollection.of(3);

        assertThat(Deletions.deleteOne("absent", collection::resolve, collection::delete))
                .isInstanceOf(NotFound.class)
                .extracting(DeletionOutcome::identifier)
                .isEqualTo("absent");

        // One request for the resolve attempt and none for a delete: absence must cost nothing beyond
        // the lookup that established it.
        assertThat(collection.deletedIds()).isEmpty();
        assertThat(collection.requestCount()).isEqualTo(1);
    }

    @Test
    void aNotFoundExceptionDuringDeleteYieldsNotFound() {
        assertThat(Deletions.deleteOne("item-1", RESOLVE_AS_ID, failingDelete(new LangfuseNotFoundException("gone"))))
                .isInstanceOf(NotFound.class)
                .isNotInstanceOf(Failed.class);
    }

    @Test
    void anyOtherExceptionDuringDeleteYieldsFailedCarryingTheCause() {
        var cause = new LangfuseApiException("unauthorized");

        assertThat(Deletions.deleteOne("item-1", RESOLVE_AS_ID, failingDelete(cause)))
                .isInstanceOf(Failed.class)
                .extracting(DeletionOutcome::identifier, outcome -> ((Failed) outcome).cause())
                .containsExactly("item-1", cause);
    }

    @Test
    void aFailureDuringResolutionYieldsFailedRatherThanPropagating() {
        var cause = new IllegalStateException("resolution blew up");

        assertThat(Deletions.deleteOne("item-1", failingResolve(cause), id -> {
        }))
                .isInstanceOf(Failed.class)
                .extracting(outcome -> ((Failed) outcome).cause())
                .isEqualTo(cause);
    }

    @Test
    void aNotFoundExceptionDuringResolutionYieldsNotFound() {
        assertThat(Deletions.deleteOne("item-1", failingResolve(new LangfuseNotFoundException("no collection")), id -> {
        }))
                .isInstanceOf(NotFound.class);
    }

    @Test
    void anErrorIsCapturedRatherThanEscaping() {
        // Throwable, not Exception: the never-throws invariant has to hold for everything, since a unit
        // that escapes would cancel its siblings once Task 05 fans these out.
        var cause = new StackOverflowError("deep");

        assertThat(Deletions.deleteOne("item-1", RESOLVE_AS_ID, failingDelete(cause)))
                .isInstanceOf(Failed.class)
                .extracting(outcome -> ((Failed) outcome).cause())
                .isEqualTo(cause);
    }

    // --- asynchronous ----------------------------------------------------------------------

    @Test
    void deletingAResolvedIdentifierYieldsDeletedAsync() {
        var collection = FakeCollection.of(3);

        assertThat(await(AsyncDeletions.deleteOne("item-2", collection::resolveAsync, collection::deleteAsync)))
                .isInstanceOf(Deleted.class)
                .extracting(DeletionOutcome::identifier)
                .isEqualTo("item-2");

        assertThat(collection.deletedIds()).containsExactly("id-of-item-2");
    }

    @Test
    void aNullResolutionYieldsNotFoundAndIssuesNoDeleteRequestAsync() {
        var collection = FakeCollection.of(3);

        // The async resolve contract emits null for absent rather than an empty Optional.
        assertThat(await(AsyncDeletions.deleteOne("absent", collection::resolveAsync, collection::deleteAsync)))
                .isInstanceOf(NotFound.class)
                .extracting(DeletionOutcome::identifier)
                .isEqualTo("absent");

        assertThat(collection.deletedIds()).isEmpty();
        assertThat(collection.requestCount()).isEqualTo(1);
    }

    @Test
    void aNotFoundFailureYieldsNotFoundAsync() {
        assertThat(await(AsyncDeletions.deleteOne("item-1", RESOLVE_AS_ID_ASYNC,
                id -> Uni.createFrom().failure(new LangfuseNotFoundException("gone")))))
                .isInstanceOf(NotFound.class)
                .isNotInstanceOf(Failed.class);
    }

    @Test
    void anyOtherFailureYieldsFailedCarryingTheCauseAsync() {
        var cause = new LangfuseApiException("unauthorized");

        assertThat(await(AsyncDeletions.deleteOne("item-1", RESOLVE_AS_ID_ASYNC,
                id -> Uni.createFrom().failure(cause))))
                .isInstanceOf(Failed.class)
                .extracting(DeletionOutcome::identifier, outcome -> ((Failed) outcome).cause())
                .containsExactly("item-1", cause);
    }

    @Test
    void aSynchronousThrowWhileBuildingTheResolveUniBecomesFailedRatherThanEscaping() {
        // The reason for the deferred() wrapper: without it this throw would escape to the caller while
        // the pipeline was still being assembled, where no recovery operator could ever see it.
        var cause = new IllegalStateException("thrown before any Uni exists");

        assertThat(await(AsyncDeletions.deleteOne("item-1", id -> {
            throw cause;
        }, id -> Uni.createFrom().item(id))))
                .isInstanceOf(Failed.class)
                .extracting(outcome -> ((Failed) outcome).cause())
                .isEqualTo(cause);
    }

    @Test
    void aSynchronousThrowWhileBuildingTheDeleteUniBecomesFailed() {
        var cause = new IllegalStateException("thrown inside the delete mapper");

        assertThat(await(AsyncDeletions.deleteOne("item-1", RESOLVE_AS_ID_ASYNC, id -> {
            throw cause;
        })))
                .isInstanceOf(Failed.class)
                .extracting(outcome -> ((Failed) outcome).cause())
                .isEqualTo(cause);
    }

    @Test
    void aFailureDuringResolutionYieldsFailedAsync() {
        var cause = new IllegalStateException("resolution blew up");

        assertThat(await(AsyncDeletions.deleteOne("item-1", id -> Uni.createFrom().failure(cause),
                id -> Uni.createFrom().item(id))))
                .isInstanceOf(Failed.class)
                .extracting(outcome -> ((Failed) outcome).cause())
                .isEqualTo(cause);
    }

    @Test
    void theUnitIsLazyUntilSubscription() {
        var collection = FakeCollection.of(3);

        var uni = AsyncDeletions.deleteOne("item-2", collection::resolveAsync, collection::deleteAsync);

        assertThat(collection.requestCount()).isZero();

        assertThat(await(uni)).isInstanceOf(Deleted.class);
        assertThat(collection.requestCount()).isEqualTo(2);
    }

    private static Consumer<String> failingDelete(Throwable cause) {
        return id -> throwUnchecked(cause);
    }

    private static Function<String, Optional<String>> failingResolve(Throwable cause) {
        return identifier -> {
            throwUnchecked(cause);

            return Optional.empty();
        };
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwUnchecked(Throwable cause) throws T {
        throw (T) cause;
    }

    private static DeletionOutcome await(Uni<DeletionOutcome> uni) {
        return uni.await().atMost(TIMEOUT);
    }
}
