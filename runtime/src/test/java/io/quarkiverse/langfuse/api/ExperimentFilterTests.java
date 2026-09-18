package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ExperimentFilterTests {

    /**
     * The mandatory distinct-value test. {@code build()} hands the builder's six fields to the record
     * positionally, so two same-typed fields could be transposed and still compile: only setting every
     * field to a value that identifies it, and reading each back, catches that.
     */
    @Test
    void everyCriterionIsReadBackFromTheFieldItWasSetOn() {
        var filter = fullFilter();

        assertThat(filter.fields()).containsExactly(ExperimentFieldGroup.SCORES);
        assertThat(filter.scoreLimit()).contains(7);
        assertThat(filter.id()).contains("the-id");
        assertThat(filter.name()).contains("the-name");
        assertThat(filter.datasetId()).contains("the-dataset-id");
        assertThat(filter.filter()).contains("the-filter");
    }

    @Test
    void anEmptyFilterCarriesNoCriterion() {
        assertThat(ExperimentFilter.none())
                .satisfies(filter -> assertThat(filter.fields()).isEmpty())
                .satisfies(filter -> assertThat(filter.scoreLimit()).isEmpty())
                .satisfies(filter -> assertThat(filter.id()).isEmpty())
                .satisfies(filter -> assertThat(filter.name()).isEmpty())
                .satisfies(filter -> assertThat(filter.datasetId()).isEmpty())
                .satisfies(filter -> assertThat(filter.filter()).isEmpty());
    }

    @Test
    void aFreshBuilderProducesAnEmptyFilter() {
        assertThat(ExperimentFilter.builder().build()).isEqualTo(ExperimentFilter.none());
    }

    @Test
    void anUnsetCriterionIsEmptyRatherThanNull() {
        var filter = ExperimentFilter.builder()
                .name("the-name")
                .build();

        assertThat(filter)
                .satisfies(it -> assertThat(it.name()).contains("the-name"))
                .satisfies(it -> assertThat(it.id()).isEmpty())
                .satisfies(it -> assertThat(it.scoreLimit()).isEmpty())
                .satisfies(it -> assertThat(it.fields()).isEmpty());
    }

    @Test
    void aNullCriterionClearsIt() {
        var filter = ExperimentFilter.builder()
                .name("the-name")
                .datasetId("the-dataset-id")
                .scoreLimit(7)
                .fields(ExperimentFieldGroup.SCORES)
                .name(null)
                .datasetId(null)
                .scoreLimit(null)
                .fields((Set<ExperimentFieldGroup>) null)
                .build();

        assertThat(filter).isEqualTo(ExperimentFilter.none());
    }

    @Test
    void anEmptyFieldGroupSetIsTheSameAsNoneAtAll() {
        assertThat(ExperimentFilter.builder().fields(Set.of()).build()).isEqualTo(ExperimentFilter.none());
    }

    @Test
    void theFieldGroupSetIsCopiedRatherThanCaptured() {
        var groups = new HashSet<>(Set.of(ExperimentFieldGroup.CORE));

        var filter = ExperimentFilter.builder()
                .fields(groups)
                .build();

        groups.add(ExperimentFieldGroup.SCORES);

        assertThat(filter.fields()).containsExactly(ExperimentFieldGroup.CORE);
    }

    @Test
    void theFieldGroupSetIsNotModifiable() {
        assertThat(ExperimentFilter.builder().fields(ExperimentFieldGroup.CORE).build().fields()).isUnmodifiable();
    }

    /**
     * Naming the same group twice is harmless rather than a caller error, which is why the varargs
     * overload copies rather than using {@code Set.of}.
     */
    @Test
    void aRepeatedFieldGroupIsAccepted() {
        assertThat(ExperimentFilter.builder()
                .fields(ExperimentFieldGroup.CORE, ExperimentFieldGroup.CORE)
                .build()
                .fields()).containsExactly(ExperimentFieldGroup.CORE);
    }

    @Test
    void toBuilderRoundTripsEveryCriterion() {
        var filter = fullFilter();

        assertThat(filter.toBuilder().build()).isEqualTo(filter);
    }

    @Test
    void toBuilderOverridesOnlyTheCriterionItIsGiven() {
        var filter = fullFilter();

        assertThat(filter.toBuilder().name("another-name").build())
                .satisfies(it -> assertThat(it.name()).contains("another-name"))
                .satisfies(it -> assertThat(it.id()).contains("the-id"))
                .satisfies(it -> assertThat(it.scoreLimit()).contains(7))
                .satisfies(it -> assertThat(it.fields()).containsExactly(ExperimentFieldGroup.SCORES));
    }

    /**
     * A filter is exactly the type where a silently wrong {@code equals} produces confusing caching and
     * test bugs, which is why the implementation is a record.
     */
    @Test
    void filtersWithTheSameCriteriaAreEqual() {
        assertThat(ExperimentFilter.builder().name("the-name").build())
                .isEqualTo(ExperimentFilter.builder().name("the-name").build())
                .hasSameHashCodeAs(ExperimentFilter.builder().name("the-name").build())
                .isNotEqualTo(ExperimentFilter.builder().name("another-name").build());
    }

    private static ExperimentFilter fullFilter() {
        return ExperimentFilter.builder()
                .fields(ExperimentFieldGroup.SCORES)
                .scoreLimit(7)
                .id("the-id")
                .name("the-name")
                .datasetId("the-dataset-id")
                .filter("the-filter")
                .build();
    }
}
