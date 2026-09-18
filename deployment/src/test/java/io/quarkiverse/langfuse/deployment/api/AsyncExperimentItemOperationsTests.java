package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.langfuse.api.model.ExperimentItem;

import io.quarkiverse.langfuse.api.AsyncExperimentItemOperations;
import io.quarkiverse.langfuse.api.AsyncLangfuseOperations;
import io.quarkiverse.langfuse.api.ExperimentItemFieldGroup;
import io.quarkiverse.langfuse.api.ExperimentItemFilter;
import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorResult;
import io.quarkiverse.langfuse.api.cursor.CursorSelection;
import io.quarkiverse.langfuse.client.LangfuseAuthenticationException;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

class AsyncExperimentItemOperationsTests extends ExperimentItemOperationsTestSupport {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    // Seconds are non-zero on purpose: OffsetDateTime.toString() elides a zero seconds field, so a
    // value like 00:00:00Z would render differently depending on which formatter the client uses.
    private static final OffsetDateTime FROM_START_TIME = OffsetDateTime.parse("2024-01-02T03:04:05Z");
    private static final OffsetDateTime TO_START_TIME = OffsetDateTime.parse("2024-02-03T04:05:06Z");
    private static final String LOCAL_FROM_START_TIME = "2024-01-02T03:04:05";
    private static final String LOCAL_TO_START_TIME = "2024-02-03T04:05:06";

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideRuntimeConfigKey("quarkus.langfuse.public-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.secret-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.api.default-batch-size", "3")
            .overrideRuntimeConfigKey(LangfuseConfig.BASE_URL_KEY, wiremockUrlForConfig());

    @Inject
    AsyncLangfuseOperations asyncLangfuse;

    @BeforeEach
    void beforeEach() {
        resetMappings();
        resetRequests();
    }

    // --- the mandatory time bound ----------------------------------------------------------

    /**
     * The assertion this whole shape exists for. Langfuse requires {@code fromStartTime} on every
     * request, not merely on the first of a walk - and WireMock happily serves a stub that omits it, so
     * nothing but this explicit check would catch a bound that stopped being sent after batch one.
     */
    @Test
    void theLowerBoundIsSentOnEveryRequestOfTheWalk() {
        stubExperimentItems(7, 3);

        assertThat(await(sinceTheBound().findAll())).hasSize(7);

        verifyLowerBoundOnEveryListRequest(3, LOCAL_FROM_START_TIME);
    }

    /**
     * The same guarantee on a filtered view, which is a differently-constructed instance and could
     * therefore lose the bound the unfiltered one keeps.
     */
    @Test
    void theLowerBoundSurvivesMatchingOnEveryRequestOfTheWalk() {
        stubExperimentItems(7, 3);

        assertThat(await(sinceTheBound().matching(fullFilter()).findAll())).hasSize(7);

        verifyLowerBoundOnEveryListRequest(3, LOCAL_FROM_START_TIME);
    }

    @Test
    void betweenSendsBothBoundsOnEveryRequestOfTheWalk() {
        stubExperimentItems(7, 3);

        assertThat(await(this.asyncLangfuse.experimentItems().between(FROM_START_TIME, TO_START_TIME).findAll()))
                .hasSize(7);

        verifyLowerBoundOnEveryListRequest(3, LOCAL_FROM_START_TIME);
        verifyUpperBoundRequested(3, LOCAL_TO_START_TIME);
    }

    @Test
    void sinceSendsNoUpperBound() {
        stubExperimentItems(2, 3);

        assertThat(await(sinceTheBound().findAll())).hasSize(2);

        verifyNoUpperBoundRequested(1);
    }

    /**
     * The bound is validated eagerly, outside any deferred assembly, so an absent one throws from the
     * call rather than surfacing as a failed {@link Uni} at subscription.
     */
    @Test
    void sinceRejectsANullBoundBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> this.asyncLangfuse.experimentItems().since(null))
                .withMessageContaining("From start time");

        verifyListRequests(0);
    }

    @Test
    void betweenRejectsANullBoundBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> this.asyncLangfuse.experimentItems().between(null, TO_START_TIME))
                .withMessageContaining("From start time");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> this.asyncLangfuse.experimentItems().between(FROM_START_TIME, null))
                .withMessageContaining("To start time");

        verifyListRequests(0);
    }

    @Test
    void betweenRejectsReversedBoundsBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> this.asyncLangfuse.experimentItems().between(TO_START_TIME, FROM_START_TIME))
                .withMessageContaining("To start time must not precede from start time");

        verifyListRequests(0);
    }

    /**
     * An empty window is a legitimate query rather than reversed bounds, so it is accepted.
     */
    @Test
    void betweenAcceptsAnEmptyWindow() {
        stubExperimentItems(2, 3);

        assertThat(await(this.asyncLangfuse.experimentItems().between(FROM_START_TIME, FROM_START_TIME).findAll()))
                .hasSize(2);

        verifyLowerBoundOnEveryListRequest(1, LOCAL_FROM_START_TIME);
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryBatch() {
        stubExperimentItems(7, 3);

        assertThat(await(sinceTheBound().findAll()))
                .hasSize(7)
                .extracting(ExperimentItem::getId)
                .startsWith("experiment-item-1")
                .endsWith("experiment-item-7");

        verifyListRequests(3);
        verifyInitialBatchRequested(1);
        verifyResumedBatchRequested(1, "3");
        verifyResumedBatchRequested(1, "6");
    }

    @Test
    void findAllUsesTheConfiguredDefaultBatchSize() {
        stubExperimentItems(7, 3);

        assertThat(await(sinceTheBound().findAll())).hasSize(7);

        wiremock().verifyThat(3, getRequestedFor(urlPathEqualTo(EXPERIMENT_ITEMS_PATH))
                .withQueryParam("limit", equalTo("3")));
    }

    /**
     * The domain is a top-level collection, so an unfiltered walk reaches the items of every
     * experiment from the collection path directly rather than through a parent.
     */
    @Test
    void itemsAreServedFromTheirOwnTopLevelPathWithNoParentScope() {
        stubExperimentItems(2, 3);

        assertThat(await(sinceTheBound().findAll())).hasSize(2);

        wiremock().verifyThat(1, getRequestedFor(urlPathEqualTo(EXPERIMENT_ITEMS_PATH)));
        verifyUnfilteredListRequests(1);
    }

    @Test
    void boundedCursorSelectionsStopAfterTheirLastBatch() {
        stubExperimentItems(20, 3);

        assertThat(await(sinceTheBound().find(CursorSelection.first(2, 3))))
                .extracting(ExperimentItem::getId)
                .containsExactly("experiment-item-1", "experiment-item-2", "experiment-item-3", "experiment-item-4",
                        "experiment-item-5", "experiment-item-6");

        verifyListRequests(2);
    }

    @Test
    void anEmptyCursorSelectionIssuesNoRequestAtAll() {
        stubExperimentItems(20, 3);

        assertThat(await(sinceTheBound().find(CursorSelection.first(0, 3)))).isEmpty();

        verifyListRequests(0);
    }

    @Test
    void multisAreLazyUntilSubscribed() {
        stubExperimentItems(7, 3);

        var multi = sinceTheBound().streamAll();

        verifyListRequests(0);

        assertThat(collect(multi.select().first(4))).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findBatchExposesTheNextCursor() {
        stubExperimentItems(7, 3);

        assertThat(await(sinceTheBound().findBatch(Cursor.first(3))))
                .satisfies(batch -> assertThat(batch.items()).hasSize(3))
                .satisfies(batch -> assertThat(batch.nextCursor()).contains(Cursor.at("3", 3)))
                .extracting(CursorResult::hasNext)
                .isEqualTo(true);
    }

    @Test
    void streamBatchesExposesEveryBatchIncludingTheLast() {
        stubExperimentItems(7, 3);

        assertThat(collect(sinceTheBound().streamBatches(CursorSelection.all(3))))
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
        stubExperimentItems(7, 3);

        assertThat(await(sinceTheBound().matching(fullFilter()).findAll()))
                .hasSize(7)
                .extracting(ExperimentItem::getId)
                .startsWith("experiment-item-1")
                .endsWith("experiment-item-7");

        verifyListRequests(3);
        verifyFullyFilteredListRequests(3);
    }

    /**
     * {@code matching} replaces rather than composes, so only the second filter's criteria are sent.
     */
    @Test
    void matchingReplacesTheFilterRatherThanCombiningIt() {
        stubExperimentItems(2, 3);

        var filter = ExperimentItemFilter.builder()
                .experimentName("another-experiment-name")
                .build();

        assertThat(await(sinceTheBound().matching(fullFilter()).matching(filter).findAll()))
                .hasSize(2);

        verifyListRequests(1);
        verifyExperimentNameRequested(1, "another-experiment-name");
        verifyExperimentNameRequested(0, "the-experiment-name");
    }

    @Test
    void matchingLeavesTheReceivingViewUnfiltered() {
        stubExperimentItems(2, 3);

        var experimentItems = sinceTheBound();
        experimentItems.matching(fullFilter());

        assertThat(await(experimentItems.findAll())).hasSize(2);

        verifyUnfilteredListRequests(1);
    }

    /**
     * The filter is validated eagerly, so a null one throws from the call rather than surfacing as a
     * failed {@link Uni} at subscription.
     */
    @Test
    void matchingRejectsANullFilterBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> sinceTheBound().matching(null))
                .withMessageContaining("Filter");

        verifyListRequests(0);
    }

    @Test
    void anEmptyFilterSendsNoCriteria() {
        stubExperimentItems(2, 3);

        assertThat(await(sinceTheBound().matching(ExperimentItemFilter.none()).findAll())).hasSize(2);

        verifyUnfilteredListRequests(1);
    }

    /**
     * Two of the groups are camel-cased on the wire, so their spelling is stated rather than derived
     * from the enum constant name.
     */
    @Test
    void fieldGroupsAreRenderedInTheirWireSpellingAndInAStableOrder() {
        stubExperimentItems(2, 3);

        var filter = ExperimentItemFilter.builder()
                .fields(ExperimentItemFieldGroup.ITEM_METADATA, ExperimentItemFieldGroup.CORE)
                .build();

        assertThat(await(sinceTheBound().matching(filter).findAll())).hasSize(2);

        wiremock().verifyThat(1, getRequestedFor(urlPathEqualTo(EXPERIMENT_ITEMS_PATH))
                .withQueryParam("fields", equalTo("core,itemMetadata")));
    }

    // --- error policy ----------------------------------------------------------------------

    @Test
    void findAllPropagatesNotFound() {
        stubListingFailure(404);

        assertThatThrownBy(() -> await(sinceTheBound().findAll()))
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    @Test
    void findAllPropagatesRejectedCredentials() {
        stubListingFailure(401);

        assertThatThrownBy(() -> await(sinceTheBound().findAll()))
                .isInstanceOf(LangfuseAuthenticationException.class);
    }

    private AsyncExperimentItemOperations sinceTheBound() {
        return this.asyncLangfuse.experimentItems().since(FROM_START_TIME);
    }

    private static ExperimentItemFilter fullFilter() {
        return ExperimentItemFilter.builder()
                .fields(ExperimentItemFieldGroup.CORE, ExperimentItemFieldGroup.DATASET,
                        ExperimentItemFieldGroup.IO, ExperimentItemFieldGroup.METADATA,
                        ExperimentItemFieldGroup.ITEM_METADATA, ExperimentItemFieldGroup.EXPERIMENT_METADATA,
                        ExperimentItemFieldGroup.SCORES)
                .scoreLimit(7)
                .experimentId("the-experiment-id")
                .experimentName("the-experiment-name")
                .experimentItemId("the-experiment-item-id")
                .datasetId("the-dataset-id")
                .filter("the-filter")
                .build();
    }

    private static <T> T await(Uni<T> uni) {
        return uni.await().atMost(TIMEOUT);
    }

    private static <T> List<T> collect(Multi<T> multi) {
        return multi.collect().asList().await().atMost(TIMEOUT);
    }
}
