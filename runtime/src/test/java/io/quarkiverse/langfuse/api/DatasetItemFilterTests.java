package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

class DatasetItemFilterTests {
    private static final OffsetDateTime VERSION = OffsetDateTime.parse("2024-01-02T03:04:05Z");

    /**
     * The mandatory distinct-value test. {@code build()} hands the builder's fields to the record
     * positionally, so two same-typed fields could be transposed and still compile: only setting every
     * field to a value that identifies it, and reading each back, catches that.
     */
    @Test
    void everyCriterionIsReadBackFromTheFieldItWasSetOn() {
        var filter = DatasetItemFilter.builder()
                .datasetName("the-dataset-name")
                .sourceTraceId("the-source-trace-id")
                .sourceObservationId("the-source-observation-id")
                .version(VERSION)
                .build();

        assertThat(filter.datasetName()).contains("the-dataset-name");
        assertThat(filter.sourceTraceId()).contains("the-source-trace-id");
        assertThat(filter.sourceObservationId()).contains("the-source-observation-id");
        assertThat(filter.version()).contains(VERSION);
    }

    @Test
    void anEmptyFilterCarriesNoCriterion() {
        assertThat(DatasetItemFilter.none())
                .satisfies(filter -> assertThat(filter.datasetName()).isEmpty())
                .satisfies(filter -> assertThat(filter.sourceTraceId()).isEmpty())
                .satisfies(filter -> assertThat(filter.sourceObservationId()).isEmpty())
                .satisfies(filter -> assertThat(filter.version()).isEmpty());
    }

    @Test
    void aFreshBuilderProducesAnEmptyFilter() {
        assertThat(DatasetItemFilter.builder().build()).isEqualTo(DatasetItemFilter.none());
    }

    @Test
    void anUnsetCriterionIsEmptyRatherThanNull() {
        var filter = DatasetItemFilter.builder()
                .sourceTraceId("the-source-trace-id")
                .build();

        assertThat(filter)
                .satisfies(it -> assertThat(it.datasetName()).isEmpty())
                .satisfies(it -> assertThat(it.sourceTraceId()).contains("the-source-trace-id"))
                .satisfies(it -> assertThat(it.sourceObservationId()).isEmpty())
                .satisfies(it -> assertThat(it.version()).isEmpty());
    }

    @Test
    void aNullCriterionClearsIt() {
        var filter = DatasetItemFilter.builder()
                .datasetName("the-dataset-name")
                .version(VERSION)
                .datasetName(null)
                .version(null)
                .build();

        assertThat(filter).isEqualTo(DatasetItemFilter.none());
    }

    @Test
    void toBuilderRoundTripsEveryCriterion() {
        var filter = DatasetItemFilter.builder()
                .datasetName("the-dataset-name")
                .sourceTraceId("the-source-trace-id")
                .sourceObservationId("the-source-observation-id")
                .version(VERSION)
                .build();

        assertThat(filter.toBuilder().build()).isEqualTo(filter);
    }

    @Test
    void toBuilderOverridesOnlyTheCriterionItIsGiven() {
        var filter = DatasetItemFilter.builder()
                .datasetName("the-dataset-name")
                .sourceTraceId("the-source-trace-id")
                .sourceObservationId("the-source-observation-id")
                .version(VERSION)
                .build();

        assertThat(filter.toBuilder().sourceTraceId("another-source-trace-id").build())
                .satisfies(it -> assertThat(it.datasetName()).contains("the-dataset-name"))
                .satisfies(it -> assertThat(it.sourceTraceId()).contains("another-source-trace-id"))
                .satisfies(it -> assertThat(it.sourceObservationId()).contains("the-source-observation-id"))
                .satisfies(it -> assertThat(it.version()).contains(VERSION));
    }

    /**
     * A filter is exactly the type where a silently wrong {@code equals} produces confusing caching and
     * test bugs, which is why the implementation is a record.
     */
    @Test
    void filtersWithTheSameCriteriaAreEqual() {
        assertThat(DatasetItemFilter.builder().datasetName("the-dataset-name").build())
                .isEqualTo(DatasetItemFilter.builder().datasetName("the-dataset-name").build())
                .hasSameHashCodeAs(DatasetItemFilter.builder().datasetName("the-dataset-name").build())
                .isNotEqualTo(DatasetItemFilter.builder().datasetName("another-dataset-name").build());
    }
}
