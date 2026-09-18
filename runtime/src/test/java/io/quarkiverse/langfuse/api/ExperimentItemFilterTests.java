package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ExperimentItemFilterTests {

    /**
     * The mandatory distinct-value test. {@code build()} hands the builder's seven fields to the record
     * positionally, and four of them are {@code String} criteria naming an experiment, so a
     * transposition would compile silently: only setting every field to a value that identifies it,
     * and reading each back, catches that.
     */
    @Test
    void everyCriterionIsReadBackFromTheFieldItWasSetOn() {
        var filter = fullFilter();

        assertThat(filter.fields()).containsExactly(ExperimentItemFieldGroup.IO);
        assertThat(filter.scoreLimit()).contains(7);
        assertThat(filter.experimentId()).contains("the-experiment-id");
        assertThat(filter.experimentName()).contains("the-experiment-name");
        assertThat(filter.experimentItemId()).contains("the-experiment-item-id");
        assertThat(filter.datasetId()).contains("the-dataset-id");
        assertThat(filter.filter()).contains("the-filter");
    }

    @Test
    void anEmptyFilterCarriesNoCriterion() {
        assertThat(ExperimentItemFilter.none())
                .satisfies(filter -> assertThat(filter.fields()).isEmpty())
                .satisfies(filter -> assertThat(filter.scoreLimit()).isEmpty())
                .satisfies(filter -> assertThat(filter.experimentId()).isEmpty())
                .satisfies(filter -> assertThat(filter.experimentName()).isEmpty())
                .satisfies(filter -> assertThat(filter.experimentItemId()).isEmpty())
                .satisfies(filter -> assertThat(filter.datasetId()).isEmpty())
                .satisfies(filter -> assertThat(filter.filter()).isEmpty());
    }

    @Test
    void aFreshBuilderProducesAnEmptyFilter() {
        assertThat(ExperimentItemFilter.builder().build()).isEqualTo(ExperimentItemFilter.none());
    }

    @Test
    void anUnsetCriterionIsEmptyRatherThanNull() {
        var filter = ExperimentItemFilter.builder()
                .experimentName("the-experiment-name")
                .build();

        assertThat(filter)
                .satisfies(it -> assertThat(it.experimentName()).contains("the-experiment-name"))
                .satisfies(it -> assertThat(it.experimentId()).isEmpty())
                .satisfies(it -> assertThat(it.scoreLimit()).isEmpty())
                .satisfies(it -> assertThat(it.fields()).isEmpty());
    }

    @Test
    void aNullCriterionClearsIt() {
        var filter = ExperimentItemFilter.builder()
                .experimentName("the-experiment-name")
                .datasetId("the-dataset-id")
                .scoreLimit(7)
                .fields(ExperimentItemFieldGroup.IO)
                .experimentName(null)
                .datasetId(null)
                .scoreLimit(null)
                .fields((Set<ExperimentItemFieldGroup>) null)
                .build();

        assertThat(filter).isEqualTo(ExperimentItemFilter.none());
    }

    @Test
    void anEmptyFieldGroupSetIsTheSameAsNoneAtAll() {
        assertThat(ExperimentItemFilter.builder().fields(Set.of()).build()).isEqualTo(ExperimentItemFilter.none());
    }

    @Test
    void theFieldGroupSetIsCopiedRatherThanCaptured() {
        var groups = new HashSet<>(Set.of(ExperimentItemFieldGroup.CORE));

        var filter = ExperimentItemFilter.builder()
                .fields(groups)
                .build();

        groups.add(ExperimentItemFieldGroup.IO);

        assertThat(filter.fields()).containsExactly(ExperimentItemFieldGroup.CORE);
    }

    @Test
    void theFieldGroupSetIsNotModifiable() {
        assertThat(ExperimentItemFilter.builder().fields(ExperimentItemFieldGroup.CORE).build().fields())
                .isUnmodifiable();
    }

    @Test
    void aRepeatedFieldGroupIsAccepted() {
        assertThat(ExperimentItemFilter.builder()
                .fields(ExperimentItemFieldGroup.CORE, ExperimentItemFieldGroup.CORE)
                .build()
                .fields()).containsExactly(ExperimentItemFieldGroup.CORE);
    }

    @Test
    void toBuilderRoundTripsEveryCriterion() {
        var filter = fullFilter();

        assertThat(filter.toBuilder().build()).isEqualTo(filter);
    }

    @Test
    void toBuilderOverridesOnlyTheCriterionItIsGiven() {
        var filter = fullFilter();

        assertThat(filter.toBuilder().experimentName("another-experiment-name").build())
                .satisfies(it -> assertThat(it.experimentName()).contains("another-experiment-name"))
                .satisfies(it -> assertThat(it.experimentId()).contains("the-experiment-id"))
                .satisfies(it -> assertThat(it.experimentItemId()).contains("the-experiment-item-id"))
                .satisfies(it -> assertThat(it.fields()).containsExactly(ExperimentItemFieldGroup.IO));
    }

    /**
     * A filter is exactly the type where a silently wrong {@code equals} produces confusing caching and
     * test bugs, which is why the implementation is a record.
     */
    @Test
    void filtersWithTheSameCriteriaAreEqual() {
        assertThat(ExperimentItemFilter.builder().experimentId("the-experiment-id").build())
                .isEqualTo(ExperimentItemFilter.builder().experimentId("the-experiment-id").build())
                .hasSameHashCodeAs(ExperimentItemFilter.builder().experimentId("the-experiment-id").build())
                .isNotEqualTo(ExperimentItemFilter.builder().experimentId("another-experiment-id").build());
    }

    private static ExperimentItemFilter fullFilter() {
        return ExperimentItemFilter.builder()
                .fields(ExperimentItemFieldGroup.IO)
                .scoreLimit(7)
                .experimentId("the-experiment-id")
                .experimentName("the-experiment-name")
                .experimentItemId("the-experiment-item-id")
                .datasetId("the-dataset-id")
                .filter("the-filter")
                .build();
    }
}
