package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.langfuse.api.model.CommentObjectType;

class CommentFilterTests {

    /**
     * The mandatory distinct-value test. {@code build()} hands the builder's fields to the record
     * positionally, so two same-typed fields could be transposed and still compile: only setting every
     * field to a value that identifies it, and reading each back, catches that.
     */
    @Test
    void everyCriterionIsReadBackFromTheFieldItWasSetOn() {
        var filter = CommentFilter.builder()
                .objectType(CommentObjectType.SESSION)
                .objectId("the-object-id")
                .authorUserId("the-author-user-id")
                .build();

        assertThat(filter.objectType()).contains(CommentObjectType.SESSION);
        assertThat(filter.objectId()).contains("the-object-id");
        assertThat(filter.authorUserId()).contains("the-author-user-id");
    }

    @Test
    void anEmptyFilterCarriesNoCriterion() {
        assertThat(CommentFilter.none())
                .satisfies(filter -> assertThat(filter.objectType()).isEmpty())
                .satisfies(filter -> assertThat(filter.objectId()).isEmpty())
                .satisfies(filter -> assertThat(filter.authorUserId()).isEmpty());
    }

    @Test
    void aFreshBuilderProducesAnEmptyFilter() {
        assertThat(CommentFilter.builder().build()).isEqualTo(CommentFilter.none());
    }

    @Test
    void anUnsetCriterionIsEmptyRatherThanNull() {
        var filter = CommentFilter.builder()
                .objectId("the-object-id")
                .build();

        assertThat(filter)
                .satisfies(it -> assertThat(it.objectType()).isEmpty())
                .satisfies(it -> assertThat(it.objectId()).contains("the-object-id"))
                .satisfies(it -> assertThat(it.authorUserId()).isEmpty());
    }

    @Test
    void aNullCriterionClearsIt() {
        var filter = CommentFilter.builder()
                .objectType(CommentObjectType.TRACE)
                .authorUserId("the-author-user-id")
                .objectType(null)
                .authorUserId(null)
                .build();

        assertThat(filter).isEqualTo(CommentFilter.none());
    }

    @Test
    void toBuilderRoundTripsEveryCriterion() {
        var filter = CommentFilter.builder()
                .objectType(CommentObjectType.OBSERVATION)
                .objectId("the-object-id")
                .authorUserId("the-author-user-id")
                .build();

        assertThat(filter.toBuilder().build()).isEqualTo(filter);
    }

    @Test
    void toBuilderOverridesOnlyTheCriterionItIsGiven() {
        var filter = CommentFilter.builder()
                .objectType(CommentObjectType.OBSERVATION)
                .objectId("the-object-id")
                .authorUserId("the-author-user-id")
                .build();

        assertThat(filter.toBuilder().objectId("another-object-id").build())
                .satisfies(it -> assertThat(it.objectType()).contains(CommentObjectType.OBSERVATION))
                .satisfies(it -> assertThat(it.objectId()).contains("another-object-id"))
                .satisfies(it -> assertThat(it.authorUserId()).contains("the-author-user-id"));
    }

    /**
     * A filter is exactly the type where a silently wrong {@code equals} produces confusing caching and
     * test bugs, which is why the implementation is a record.
     */
    @Test
    void filtersWithTheSameCriteriaAreEqual() {
        assertThat(CommentFilter.builder().objectId("the-object-id").build())
                .isEqualTo(CommentFilter.builder().objectId("the-object-id").build())
                .hasSameHashCodeAs(CommentFilter.builder().objectId("the-object-id").build())
                .isNotEqualTo(CommentFilter.builder().objectId("another-object-id").build());
    }
}
