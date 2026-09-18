package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.langfuse.api.model.ObservationLevel;
import com.langfuse.api.model.ObservationType;
import com.langfuse.api.model.ObservationV2;

import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.ObservationFilter;
import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorResult;
import io.quarkiverse.langfuse.api.cursor.CursorSelection;
import io.quarkiverse.langfuse.client.LangfuseAuthenticationException;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;

class ObservationOperationsTests extends ObservationOperationsTestSupport {
    // Seconds are non-zero on purpose: OffsetDateTime.toString() elides a zero seconds field, so a
    // value like 00:00:00Z would render differently depending on which formatter the client uses.
    private static final OffsetDateTime FROM_START_TIME = OffsetDateTime.parse("2024-01-02T03:04:05Z");
    private static final OffsetDateTime TO_START_TIME = OffsetDateTime.parse("2024-02-03T04:05:06Z");

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideRuntimeConfigKey("quarkus.langfuse.public-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.secret-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.api.default-batch-size", "3")
            .overrideRuntimeConfigKey(LangfuseConfig.BASE_URL_KEY, wiremockUrlForConfig());

    @Inject
    LangfuseOperations langfuse;

    @BeforeEach
    void beforeEach() {
        resetMappings();
        resetRequests();
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryBatch() {
        stubObservations(7, 3);

        assertThat(langfuse.observations().findAll())
                .hasSize(7)
                .extracting(ObservationV2::getId)
                .startsWith("observation-1")
                .endsWith("observation-7");

        verifyListRequests(3);
        verifyInitialBatchRequested(1);
        verifyResumedBatchRequested(1, "3");
        verifyResumedBatchRequested(1, "6");
    }

    @Test
    void findAllUsesTheConfiguredDefaultBatchSize() {
        stubObservations(7, 3);

        assertThat(langfuse.observations().findAll()).hasSize(7);

        wiremock().verifyThat(3, getRequestedFor(urlPathEqualTo(OBSERVATIONS_PATH))
                .withQueryParam("limit", equalTo("3")));
    }

    @Test
    void boundedCursorSelectionsStopAfterTheirLastBatch() {
        stubObservations(20, 3);

        assertThat(langfuse.observations().find(CursorSelection.first(2, 3)))
                .extracting(ObservationV2::getId)
                .containsExactly("observation-1", "observation-2", "observation-3", "observation-4", "observation-5",
                        "observation-6");

        verifyListRequests(2);
    }

    @Test
    void anEmptyCursorSelectionIssuesNoRequestAtAll() {
        stubObservations(20, 3);

        assertThat(langfuse.observations().find(CursorSelection.first(0, 3))).isEmpty();

        verifyListRequests(0);
    }

    @Test
    void streamsAreLazyUntilConsumed() {
        stubObservations(7, 3);

        var stream = langfuse.observations().streamAll();

        verifyListRequests(0);

        assertThat(stream.limit(4)).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findBatchExposesTheNextCursor() {
        stubObservations(7, 3);

        assertThat(langfuse.observations().findBatch(Cursor.first(3)))
                .satisfies(batch -> assertThat(batch.items()).hasSize(3))
                .satisfies(batch -> assertThat(batch.nextCursor()).contains(Cursor.at("3", 3)))
                .extracting(CursorResult::hasNext)
                .isEqualTo(true);
    }

    @Test
    void streamBatchesExposesEveryBatchIncludingTheLast() {
        stubObservations(7, 3);

        assertThat(langfuse.observations().streamBatches(CursorSelection.all(3)))
                .hasSize(3)
                .extracting(CursorResult::hasNext)
                .containsExactly(true, true, false);
    }

    // --- filtering -------------------------------------------------------------------------

    /**
     * A filtered view walks exactly like the unfiltered one, and carries its criteria on every request
     * of the walk rather than only the first.
     */
    @Test
    void filteredViewsSendTheirCriteriaOnEveryBatchOfTheWalk() {
        stubObservations(7, 3);

        assertThat(langfuse.observations().matching(fullFilter()).findAll())
                .hasSize(7)
                .extracting(ObservationV2::getId)
                .startsWith("observation-1")
                .endsWith("observation-7");

        verifyListRequests(3);
        verifyFullyFilteredListRequests(3, "2024-01-02T03:04:05", "2024-02-03T04:05:06");
    }

    /**
     * {@code matching} replaces rather than composes, so only the second filter's criteria are sent.
     */
    @Test
    void matchingReplacesTheFilterRatherThanCombiningIt() {
        stubObservations(2, 3);

        var filter = ObservationFilter.builder()
                .traceId("another-trace")
                .build();

        assertThat(langfuse.observations().matching(fullFilter()).matching(filter).findAll()).hasSize(2);

        verifyListRequests(1);
        verifyTraceIdRequested(1, "another-trace");
        verifyTraceIdRequested(0, "trace-1");
    }

    @Test
    void matchingLeavesTheReceivingViewUnfiltered() {
        stubObservations(2, 3);

        var observations = langfuse.observations();
        observations.matching(fullFilter());

        assertThat(observations.findAll()).hasSize(2);

        verifyUnfilteredListRequests(1);
    }

    @Test
    void matchingRejectsANullFilterBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.observations().matching(null))
                .withMessageContaining("Filter");

        verifyListRequests(0);
    }

    @Test
    void anEmptyFilterSendsNoCriteria() {
        stubObservations(2, 3);

        assertThat(langfuse.observations().matching(ObservationFilter.none()).findAll()).hasSize(2);

        verifyUnfilteredListRequests(1);
    }

    // --- error policy ----------------------------------------------------------------------

    @Test
    void findAllPropagatesNotFound() {
        stubListingFailure(404);

        assertThatThrownBy(() -> langfuse.observations().findAll())
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    @Test
    void findAllPropagatesRejectedCredentials() {
        stubListingFailure(401);

        assertThatThrownBy(() -> langfuse.observations().findAll())
                .isInstanceOf(LangfuseAuthenticationException.class);
    }

    private static ObservationFilter fullFilter() {
        return ObservationFilter.builder()
                .fields("core,basic")
                .expandMetadata("key1,key2")
                .name("the-name")
                .userId("user-1")
                .sessionId("session-1")
                .type(ObservationType.GENERATION)
                .traceId("trace-1")
                .level(ObservationLevel.ERROR)
                .parentObservationId("observation-parent")
                .isRootObservation(false)
                .environment(List.of("production"))
                .fromStartTime(FROM_START_TIME)
                .toStartTime(TO_START_TIME)
                .version("the-version")
                .filter("the-filter")
                .build();
    }
}
