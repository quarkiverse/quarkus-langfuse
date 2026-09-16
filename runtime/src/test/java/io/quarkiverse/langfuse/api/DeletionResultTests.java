package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApiException;

class DeletionResultTests {

    @Test
    void exposesEveryOutcomeKeyedByIdentifier() {
        var cause = new LangfuseApiException("rate limited");
        var result = DeletionResult.of(List.of(
                DeletionOutcome.deleted("a"),
                DeletionOutcome.notFound("b"),
                DeletionOutcome.failed("c", cause)));

        assertThat(result.outcomes())
                .hasSize(3)
                .containsEntry("a", DeletionOutcome.deleted("a"))
                .containsEntry("b", DeletionOutcome.notFound("b"))
                .containsEntry("c", DeletionOutcome.failed("c", cause));
        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    void looksUpASingleOutcomeByIdentifier() {
        var result = DeletionResult.of(List.of(DeletionOutcome.deleted("a")));

        assertThat(result.outcome("a"))
                .isPresent()
                .contains(DeletionOutcome.deleted("a"));
        assertThat(result.outcome("missing")).isEmpty();
        assertThat(result.outcome(null)).isEmpty();
    }

    @Test
    void derivesTheConvenienceViewsFromTheOutcomes() {
        var cause = new LangfuseApiException("rate limited");
        var result = DeletionResult.of(List.of(
                DeletionOutcome.deleted("a"),
                DeletionOutcome.deleted("b"),
                DeletionOutcome.notFound("c"),
                DeletionOutcome.failed("d", cause)));

        assertThat(result.deleted()).containsExactly("a", "b");
        assertThat(result.notFound()).containsExactly("c");
        assertThat(result.failed())
                .hasSize(1)
                .containsEntry("d", cause);
        assertThat(result.hasFailures()).isTrue();
    }

    @Test
    void absenceIsNotAFailure() {
        var result = DeletionResult.of(List.of(
                DeletionOutcome.deleted("a"),
                DeletionOutcome.notFound("b")));

        assertThat(result.hasFailures()).isFalse();
        assertThat(result.failed()).isEmpty();
        assertThat(result.notFound()).containsExactly("b");
    }

    @Test
    void anEmptyResultIsWellFormed() {
        var result = DeletionResult.of(List.of());

        assertThat(result.outcomes()).isEmpty();
        assertThat(result.deleted()).isEmpty();
        assertThat(result.notFound()).isEmpty();
        assertThat(result.failed()).isEmpty();
        assertThat(result.hasFailures()).isFalse();
        assertThat(result.size()).isZero();
    }

    @Test
    void theFirstOutcomeWinsForARepeatedIdentifier() {
        var result = DeletionResult.of(List.of(
                DeletionOutcome.deleted("a"),
                DeletionOutcome.notFound("a")));

        assertThat(result.outcome("a")).contains(DeletionOutcome.deleted("a"));
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void outcomesAreDefensivelyCopiedAndUnmodifiable() {
        var source = new ArrayList<DeletionOutcome>(List.of(DeletionOutcome.deleted("a")));
        var result = DeletionResult.of(source);

        source.add(DeletionOutcome.deleted("b"));

        assertThat(result.outcomes()).hasSize(1);
        assertThatThrownBy(() -> result.outcomes().put("b", DeletionOutcome.deleted("b")))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.deleted().add("b"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.failed().put("b", new LangfuseApiException("nope")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void iterationFollowsFirstOccurrenceInputOrder() {
        // An implementation detail rather than a contract - deletes may run concurrently - but asserted
        // here because logs and the rest of this suite depend on it being stable.
        var result = DeletionResult.of(List.of(
                DeletionOutcome.deleted("z"),
                DeletionOutcome.notFound("m"),
                DeletionOutcome.deleted("a")));

        assertThat(result.outcomes()).containsExactly(
                entry("z", DeletionOutcome.deleted("z")),
                entry("m", DeletionOutcome.notFound("m")),
                entry("a", DeletionOutcome.deleted("a")));
    }

    @Test
    void nullInputIsRejected() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> DeletionResult.of(null))
                .withMessage("Outcomes must not be null");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> DeletionResult.of(Arrays.asList(DeletionOutcome.deleted("a"), null)))
                .withMessage("Outcome must not be null");
    }
}
