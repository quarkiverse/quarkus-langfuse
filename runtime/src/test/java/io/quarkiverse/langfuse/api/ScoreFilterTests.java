package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.langfuse.api.model.ScoreDataType;
import com.langfuse.api.model.ScoreSource;

class ScoreFilterTests {
    private static final OffsetDateTime FROM_TIMESTAMP = OffsetDateTime.parse("2024-01-02T03:04:05Z");
    private static final OffsetDateTime TO_TIMESTAMP = OffsetDateTime.parse("2024-02-03T04:05:06Z");

    /**
     * The mandatory distinct-value test, and the reason it matters most on this domain: {@code build()}
     * hands the builder's eighteen fields to the record positionally, eleven of them {@code String},
     * so two could be transposed and still compile. Only setting every field to a value that names it,
     * and reading each back, catches that.
     */
    @Test
    void everyCriterionIsReadBackFromTheFieldItWasSetOn() {
        var filter = ScoreFilter.builder()
                .fields(ScoreFieldGroup.DETAILS, ScoreFieldGroup.SUBJECT)
                .id("the-id")
                .name("the-name")
                .source(ScoreSource.EVAL)
                .dataType(ScoreDataType.NUMERIC)
                .environment("the-environment")
                .configId("the-config-id")
                .queueId("the-queue-id")
                .authorUserId("the-author-user-id")
                .value("the-value")
                .valueMin(1.5d)
                .valueMax(2.5d)
                .traceId("the-trace-id")
                .sessionId("the-session-id")
                .observationId("the-observation-id")
                .experimentId("the-experiment-id")
                .fromTimestamp(FROM_TIMESTAMP)
                .toTimestamp(TO_TIMESTAMP)
                .build();

        assertThat(filter.fields()).containsExactlyInAnyOrder(ScoreFieldGroup.DETAILS, ScoreFieldGroup.SUBJECT);
        assertThat(filter.id()).contains("the-id");
        assertThat(filter.name()).contains("the-name");
        assertThat(filter.source()).contains(ScoreSource.EVAL);
        assertThat(filter.dataType()).contains(ScoreDataType.NUMERIC);
        assertThat(filter.environment()).contains("the-environment");
        assertThat(filter.configId()).contains("the-config-id");
        assertThat(filter.queueId()).contains("the-queue-id");
        assertThat(filter.authorUserId()).contains("the-author-user-id");
        assertThat(filter.value()).contains("the-value");
        assertThat(filter.valueMin()).contains(1.5d);
        assertThat(filter.valueMax()).contains(2.5d);
        assertThat(filter.traceId()).contains("the-trace-id");
        assertThat(filter.sessionId()).contains("the-session-id");
        assertThat(filter.observationId()).contains("the-observation-id");
        assertThat(filter.experimentId()).contains("the-experiment-id");
        assertThat(filter.fromTimestamp()).contains(FROM_TIMESTAMP);
        assertThat(filter.toTimestamp()).contains(TO_TIMESTAMP);
    }

    @Test
    void anEmptyFilterCarriesNoCriterion() {
        assertThat(ScoreFilter.none())
                .satisfies(filter -> assertThat(filter.fields()).isEmpty())
                .satisfies(filter -> assertThat(filter.id()).isEmpty())
                .satisfies(filter -> assertThat(filter.name()).isEmpty())
                .satisfies(filter -> assertThat(filter.source()).isEmpty())
                .satisfies(filter -> assertThat(filter.dataType()).isEmpty())
                .satisfies(filter -> assertThat(filter.environment()).isEmpty())
                .satisfies(filter -> assertThat(filter.configId()).isEmpty())
                .satisfies(filter -> assertThat(filter.queueId()).isEmpty())
                .satisfies(filter -> assertThat(filter.authorUserId()).isEmpty())
                .satisfies(filter -> assertThat(filter.value()).isEmpty())
                .satisfies(filter -> assertThat(filter.valueMin()).isEmpty())
                .satisfies(filter -> assertThat(filter.valueMax()).isEmpty())
                .satisfies(filter -> assertThat(filter.traceId()).isEmpty())
                .satisfies(filter -> assertThat(filter.sessionId()).isEmpty())
                .satisfies(filter -> assertThat(filter.observationId()).isEmpty())
                .satisfies(filter -> assertThat(filter.experimentId()).isEmpty())
                .satisfies(filter -> assertThat(filter.fromTimestamp()).isEmpty())
                .satisfies(filter -> assertThat(filter.toTimestamp()).isEmpty());
    }

    @Test
    void aFreshBuilderProducesAnEmptyFilter() {
        assertThat(ScoreFilter.builder().build()).isEqualTo(ScoreFilter.none());
    }

    @Test
    void anUnsetCriterionIsEmptyRatherThanNull() {
        var filter = ScoreFilter.builder()
                .traceId("the-trace-id")
                .build();

        assertThat(filter)
                .satisfies(it -> assertThat(it.traceId()).contains("the-trace-id"))
                .satisfies(it -> assertThat(it.sessionId()).isEmpty())
                .satisfies(it -> assertThat(it.observationId()).isEmpty())
                .satisfies(it -> assertThat(it.fields()).isEmpty());
    }

    @Test
    void aNullCriterionClearsIt() {
        var filter = ScoreFilter.builder()
                .traceId("the-trace-id")
                .dataType(ScoreDataType.BOOLEAN)
                .fields(ScoreFieldGroup.ANNOTATION)
                .traceId(null)
                .dataType(null)
                .fields((Set<ScoreFieldGroup>) null)
                .build();

        assertThat(filter).isEqualTo(ScoreFilter.none());
    }

    /**
     * {@code fields()} is the one accessor that does not return an {@link java.util.Optional}, so
     * absence has to read as an empty set rather than {@code null} for a caller iterating it.
     */
    @Test
    void unrequestedFieldGroupsReadAsAnEmptySetRatherThanNull() {
        assertThat(ScoreFilter.none().fields())
                .isNotNull()
                .isEmpty();
    }

    @Test
    void fieldGroupsAreHeldIndependentlyOfTheSetTheBuilderWasGiven() {
        var groups = new java.util.HashSet<>(Set.of(ScoreFieldGroup.DETAILS));
        var filter = ScoreFilter.builder()
                .fields(groups)
                .build();

        groups.add(ScoreFieldGroup.ANNOTATION);

        assertThat(filter.fields()).containsExactly(ScoreFieldGroup.DETAILS);
    }

    @Test
    void namingAFieldGroupTwiceIsHarmless() {
        assertThat(ScoreFilter.builder()
                .fields(ScoreFieldGroup.DETAILS, ScoreFieldGroup.DETAILS)
                .build()
                .fields())
                .containsExactly(ScoreFieldGroup.DETAILS);
    }

    @Test
    void toBuilderRoundTripsEveryCriterion() {
        var filter = ScoreFilter.builder()
                .fields(ScoreFieldGroup.DETAILS, ScoreFieldGroup.SUBJECT)
                .id("the-id")
                .name("the-name")
                .source(ScoreSource.API)
                .dataType(ScoreDataType.CATEGORICAL)
                .environment("the-environment")
                .configId("the-config-id")
                .queueId("the-queue-id")
                .authorUserId("the-author-user-id")
                .value("the-value")
                .valueMin(1.5d)
                .valueMax(2.5d)
                .traceId("the-trace-id")
                .sessionId("the-session-id")
                .observationId("the-observation-id")
                .experimentId("the-experiment-id")
                .fromTimestamp(FROM_TIMESTAMP)
                .toTimestamp(TO_TIMESTAMP)
                .build();

        assertThat(filter.toBuilder().build()).isEqualTo(filter);
    }

    @Test
    void toBuilderOverridesOnlyTheCriterionItIsGiven() {
        var filter = ScoreFilter.builder()
                .traceId("the-trace-id")
                .sessionId("the-session-id")
                .dataType(ScoreDataType.NUMERIC)
                .build();

        assertThat(filter.toBuilder().traceId("another-trace-id").build())
                .satisfies(it -> assertThat(it.traceId()).contains("another-trace-id"))
                .satisfies(it -> assertThat(it.sessionId()).contains("the-session-id"))
                .satisfies(it -> assertThat(it.dataType()).contains(ScoreDataType.NUMERIC));
    }

    /**
     * A filter is exactly the type where a silently wrong {@code equals} produces confusing caching and
     * test bugs, which is why the implementation is a record.
     */
    @Test
    void filtersWithTheSameCriteriaAreEqual() {
        assertThat(ScoreFilter.builder().traceId("the-trace-id").build())
                .isEqualTo(ScoreFilter.builder().traceId("the-trace-id").build())
                .hasSameHashCodeAs(ScoreFilter.builder().traceId("the-trace-id").build())
                .isNotEqualTo(ScoreFilter.builder().traceId("another-trace-id").build());
    }
}
