package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.langfuse.api.model.ObservationLevel;
import com.langfuse.api.model.ObservationType;

class ObservationFilterTests {
    private static final OffsetDateTime FROM_START_TIME = OffsetDateTime.parse("2024-01-02T03:04:05Z");
    private static final OffsetDateTime TO_START_TIME = OffsetDateTime.parse("2024-02-03T04:05:06Z");

    /**
     * The mandatory distinct-value test. {@code build()} hands the builder's fifteen fields to the
     * record positionally, so two same-typed fields could be transposed and still compile: only setting
     * every field to a value that identifies it, and reading each back, catches that.
     */
    @Test
    void everyCriterionIsReadBackFromTheFieldItWasSetOn() {
        var filter = fullFilter();

        assertThat(filter.fields()).contains("the-fields");
        assertThat(filter.expandMetadata()).contains("the-expand-metadata");
        assertThat(filter.name()).contains("the-name");
        assertThat(filter.userId()).contains("the-user-id");
        assertThat(filter.sessionId()).contains("the-session-id");
        assertThat(filter.type()).contains(ObservationType.GENERATION);
        assertThat(filter.traceId()).contains("the-trace-id");
        assertThat(filter.level()).contains(ObservationLevel.ERROR);
        assertThat(filter.parentObservationId()).contains("the-parent-observation-id");
        assertThat(filter.isRootObservation()).contains(true);
        assertThat(filter.environment()).containsExactly("the-environment");
        assertThat(filter.fromStartTime()).contains(FROM_START_TIME);
        assertThat(filter.toStartTime()).contains(TO_START_TIME);
        assertThat(filter.version()).contains("the-version");
        assertThat(filter.filter()).contains("the-filter");
    }

    @Test
    void anEmptyFilterCarriesNoCriterion() {
        assertThat(ObservationFilter.none())
                .satisfies(filter -> assertThat(filter.fields()).isEmpty())
                .satisfies(filter -> assertThat(filter.expandMetadata()).isEmpty())
                .satisfies(filter -> assertThat(filter.name()).isEmpty())
                .satisfies(filter -> assertThat(filter.userId()).isEmpty())
                .satisfies(filter -> assertThat(filter.sessionId()).isEmpty())
                .satisfies(filter -> assertThat(filter.type()).isEmpty())
                .satisfies(filter -> assertThat(filter.traceId()).isEmpty())
                .satisfies(filter -> assertThat(filter.level()).isEmpty())
                .satisfies(filter -> assertThat(filter.parentObservationId()).isEmpty())
                .satisfies(filter -> assertThat(filter.isRootObservation()).isEmpty())
                .satisfies(filter -> assertThat(filter.environment()).isEmpty())
                .satisfies(filter -> assertThat(filter.fromStartTime()).isEmpty())
                .satisfies(filter -> assertThat(filter.toStartTime()).isEmpty())
                .satisfies(filter -> assertThat(filter.version()).isEmpty())
                .satisfies(filter -> assertThat(filter.filter()).isEmpty());
    }

    @Test
    void aFreshBuilderProducesAnEmptyFilter() {
        assertThat(ObservationFilter.builder().build()).isEqualTo(ObservationFilter.none());
    }

    @Test
    void anUnsetCriterionIsEmptyRatherThanNull() {
        var filter = ObservationFilter.builder()
                .traceId("the-trace-id")
                .build();

        assertThat(filter)
                .satisfies(it -> assertThat(it.traceId()).contains("the-trace-id"))
                .satisfies(it -> assertThat(it.name()).isEmpty())
                .satisfies(it -> assertThat(it.level()).isEmpty())
                .satisfies(it -> assertThat(it.environment()).isEmpty());
    }

    @Test
    void aNullCriterionClearsIt() {
        var filter = ObservationFilter.builder()
                .traceId("the-trace-id")
                .level(ObservationLevel.ERROR)
                .environment(List.of("the-environment"))
                .traceId(null)
                .level(null)
                .environment(null)
                .build();

        assertThat(filter).isEqualTo(ObservationFilter.none());
    }

    /**
     * An empty environment list means "unrestricted", exactly like an unset one, so the two must not
     * produce filters that differ.
     */
    @Test
    void anEmptyEnvironmentListIsTheSameAsNoEnvironmentAtAll() {
        assertThat(ObservationFilter.builder().environment(List.of()).build())
                .isEqualTo(ObservationFilter.none());
    }

    @Test
    void theEnvironmentListIsCopiedRatherThanCaptured() {
        var environments = new ArrayList<>(List.of("the-environment"));

        var filter = ObservationFilter.builder()
                .environment(environments)
                .build();

        environments.add("another-environment");

        assertThat(filter.environment()).containsExactly("the-environment");
    }

    @Test
    void theEnvironmentListIsNotModifiable() {
        var filter = ObservationFilter.builder()
                .environment(List.of("the-environment"))
                .build();

        assertThat(filter.environment()).isUnmodifiable();
    }

    @Test
    void toBuilderRoundTripsEveryCriterion() {
        var filter = fullFilter();

        assertThat(filter.toBuilder().build()).isEqualTo(filter);
    }

    @Test
    void toBuilderOverridesOnlyTheCriterionItIsGiven() {
        var filter = fullFilter();

        assertThat(filter.toBuilder().traceId("another-trace-id").build())
                .satisfies(it -> assertThat(it.traceId()).contains("another-trace-id"))
                .satisfies(it -> assertThat(it.name()).contains("the-name"))
                .satisfies(it -> assertThat(it.level()).contains(ObservationLevel.ERROR))
                .satisfies(it -> assertThat(it.environment()).containsExactly("the-environment"));
    }

    /**
     * A filter is exactly the type where a silently wrong {@code equals} produces confusing caching and
     * test bugs, which is why the implementation is a record.
     */
    @Test
    void filtersWithTheSameCriteriaAreEqual() {
        assertThat(ObservationFilter.builder().traceId("the-trace-id").build())
                .isEqualTo(ObservationFilter.builder().traceId("the-trace-id").build())
                .hasSameHashCodeAs(ObservationFilter.builder().traceId("the-trace-id").build())
                .isNotEqualTo(ObservationFilter.builder().traceId("another-trace-id").build());
    }

    private static ObservationFilter fullFilter() {
        return ObservationFilter.builder()
                .fields("the-fields")
                .expandMetadata("the-expand-metadata")
                .name("the-name")
                .userId("the-user-id")
                .sessionId("the-session-id")
                .type(ObservationType.GENERATION)
                .traceId("the-trace-id")
                .level(ObservationLevel.ERROR)
                .parentObservationId("the-parent-observation-id")
                .isRootObservation(true)
                .environment(List.of("the-environment"))
                .fromStartTime(FROM_START_TIME)
                .toStartTime(TO_START_TIME)
                .version("the-version")
                .filter("the-filter")
                .build();
    }
}
