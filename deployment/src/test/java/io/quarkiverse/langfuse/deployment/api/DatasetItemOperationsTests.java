package io.quarkiverse.langfuse.deployment.api;

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

import com.langfuse.api.model.CreateDatasetItemRequest;
import com.langfuse.api.model.DatasetItem;

import io.quarkiverse.langfuse.api.DatasetItemFilter;
import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.deletion.DeletionOutcome;
import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PageSelection;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseAuthenticationException;
import io.quarkiverse.langfuse.client.LangfuseAuthorizationException;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;

class DatasetItemOperationsTests extends DatasetItemOperationsTestSupport {
    // Seconds are non-zero on purpose: OffsetDateTime.toString() elides a zero seconds field, so a
    // value like 00:00:00Z would render differently depending on which formatter the client uses.
    private static final OffsetDateTime VERSION = OffsetDateTime.parse("2024-01-02T03:04:05Z");

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideRuntimeConfigKey("quarkus.langfuse.public-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.secret-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.api.default-page-size", "3")
            .overrideRuntimeConfigKey(LangfuseConfig.BASE_URL_KEY, wiremockUrlForConfig());

    @Inject
    LangfuseOperations langfuse;

    @BeforeEach
    void beforeEach() {
        resetAndGetWiremock();
    }

    // --- lookup ----------------------------------------------------------------------------

    @Test
    void findByIdResolvesInASingleRequestWithoutScanning() {
        stubDatasetItems(7, 3);
        stubDatasetItemFound("item-2");

        assertThat(langfuse.datasetItems().findById("item-2"))
                .isPresent()
                .get()
                .extracting(DatasetItem::getId)
                .isEqualTo("item-2");

        verifyDatasetItemGetRequests(1, "item-2");
        verifyListRequests(0);
    }

    @Test
    void findByIdTreatsNotFoundAsAbsence() {
        stubDatasetItemFailure("nope", 404);

        assertThat(langfuse.datasetItems().findById("nope")).isEmpty();
    }

    /**
     * The absence-versus-failure rule: only a 404 is recovered, so a rejected credential must escape
     * rather than read as "no dataset item with that id".
     */
    @Test
    void findByIdNeverMistakesARejectedCredentialForAbsence() {
        stubDatasetItemFailure("item-1", 401);

        assertThatThrownBy(() -> langfuse.datasetItems().findById("item-1"))
                .isInstanceOf(LangfuseAuthenticationException.class);
    }

    @Test
    void findByIdNeverMistakesARefusedActionForAbsence() {
        stubDatasetItemFailure("item-1", 403);

        assertThatThrownBy(() -> langfuse.datasetItems().findById("item-1"))
                .isInstanceOf(LangfuseAuthorizationException.class);
    }

    @Test
    void blankIdsAreRejectedBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.datasetItems().findById("  "))
                .withMessageContaining("Dataset item id");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.datasetItems().findById(null));

        verifyListRequests(0);
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryPage() {
        stubDatasetItems(7, 3);

        assertThat(langfuse.datasetItems().findAll())
                .hasSize(7)
                .extracting(DatasetItem::getId)
                .startsWith("item-1")
                .endsWith("item-7");

        verifyListRequests(3);
        verifyUnfilteredListRequests(3);
    }

    @Test
    void boundedSelectionsRequestOnlyTheirPages() {
        stubDatasetItems(20, 3);

        assertThat(langfuse.datasetItems().find(PageSelection.rangeClosed(2, 3, 3)))
                .extracting(DatasetItem::getId)
                .containsExactly("item-4", "item-5", "item-6", "item-7", "item-8", "item-9");

        verifyListRequests(2);
        verifyPageRequested(1, 2);
        verifyPageRequested(1, 3);
        verifyPageRequested(0, 1);
    }

    @Test
    void streamsAreLazyUntilConsumed() {
        stubDatasetItems(7, 3);

        var stream = langfuse.datasetItems().streamAll();

        verifyListRequests(0);

        assertThat(stream.limit(4)).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findPageExposesTheReportedTotals() {
        stubDatasetItems(7, 3);

        assertThat(langfuse.datasetItems().findPage(Page.of(2, 3)))
                .satisfies(page -> assertThat(page.items()).hasSize(3))
                .satisfies(page -> assertThat(page.nextPage()).contains(Page.of(3, 3)))
                .extracting(PagedResult::totalItems, PagedResult::totalPages, PagedResult::hasNext)
                .containsExactly(7, 3, true);
    }

    @Test
    void findAllPropagatesNotFound() {
        stubListingFailure(404);

        assertThatThrownBy(() -> langfuse.datasetItems().findAll())
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    // --- filtering -------------------------------------------------------------------------

    /**
     * A filtered view pages exactly like the unfiltered one, and carries its criteria on every request
     * of the walk rather than only the first.
     */
    @Test
    void filteredViewsSendTheirCriteriaOnEveryPageOfTheWalk() {
        stubDatasetItems(7, 3);

        assertThat(langfuse.datasetItems().matching(datasetFilter()).findAll())
                .hasSize(7)
                .extracting(DatasetItem::getId)
                .startsWith("item-1")
                .endsWith("item-7");

        verifyListRequests(3);
        verifyFilteredListRequests(3, "my-dataset", "trace-1", "observation-1", "2024-01-02T03:04:05");
    }

    /**
     * {@code matching} replaces rather than composes, so only the second filter's criteria are sent.
     */
    @Test
    void matchingReplacesTheFilterRatherThanCombiningIt() {
        stubDatasetItems(2, 3);

        var filter = DatasetItemFilter.builder()
                .datasetName("another-dataset")
                .build();

        assertThat(langfuse.datasetItems().matching(datasetFilter()).matching(filter).findAll()).hasSize(2);

        verifyListRequests(1);
        verifyDatasetNameRequested(1, "another-dataset");
        verifyDatasetNameRequested(0, "my-dataset");
    }

    @Test
    void matchingLeavesTheReceivingViewUnfiltered() {
        stubDatasetItems(2, 3);

        var datasetItems = langfuse.datasetItems();
        datasetItems.matching(datasetFilter());

        assertThat(datasetItems.findAll()).hasSize(2);

        verifyUnfilteredListRequests(1);
    }

    @Test
    void matchingRejectsANullFilterBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.datasetItems().matching(null))
                .withMessageContaining("Filter");

        verifyListRequests(0);
    }

    @Test
    void anEmptyFilterSendsNoCriteria() {
        stubDatasetItems(2, 3);

        assertThat(langfuse.datasetItems().matching(DatasetItemFilter.none()).findAll()).hasSize(2);

        verifyUnfilteredListRequests(1);
    }

    @Test
    void findByIdOnAFilteredViewIsNotScopedByTheFilter() {
        stubDatasetItemFound("item-2");

        assertThat(langfuse.datasetItems().matching(datasetFilter()).findById("item-2"))
                .isPresent()
                .get()
                .extracting(DatasetItem::getId)
                .isEqualTo("item-2");

        verifyDatasetItemGetRequests(1, "item-2");
        verifyListRequests(0);
    }

    // --- writes ----------------------------------------------------------------------------

    @Test
    void createReturnsTheStoredItem() {
        stubDatasetItemCreated("item-new");

        assertThat(langfuse.datasetItems().create(createRequest()))
                .isNotNull()
                .extracting(DatasetItem::getId)
                .isEqualTo("item-new");

        verifyDatasetItemsCreated(1);
    }

    @Test
    void createRejectsANullRequestBeforeAnyCall() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.datasetItems().create(null));

        verifyDatasetItemsCreated(0);
    }

    // --- deletion --------------------------------------------------------------------------

    /**
     * The delete endpoint is keyed on the item id, so nothing is resolved first and no listing request
     * is issued.
     */
    @Test
    void deleteByIdIssuesOneRequestPerIdAndNoListing() {
        stubDatasetItemDeleted("item-1");

        assertThat(langfuse.datasetItems().deleteById("item-1"))
                .satisfies(result -> assertThat(result.deleted()).containsExactly("item-1"))
                .satisfies(result -> assertThat(result.hasFailures()).isFalse());

        verifyDatasetItemDeleteRequests(1, "item-1");
        verifyListRequests(0);
    }

    @Test
    void deleteByIdReportsAbsenceRatherThanFailing() {
        stubDatasetItemDeleteFailure("gone", 404);

        assertThat(langfuse.datasetItems().deleteById("gone"))
                .satisfies(result -> assertThat(result.notFound()).containsExactly("gone"))
                .satisfies(result -> assertThat(result.hasFailures()).isFalse());
    }

    /**
     * Never fails fast: a mixed batch reports every identifier's own outcome.
     */
    @Test
    void deleteByIdAttemptsEveryIdWhateverHappensToTheOthers() {
        stubDatasetItemDeleted("item-1");
        stubDatasetItemDeleteFailure("item-2", 500);
        stubDatasetItemDeleteFailure("item-3", 404);

        assertThat(langfuse.datasetItems().deleteById("item-1", "item-2", "item-3"))
                .satisfies(result -> assertThat(result.deleted()).containsExactly("item-1"))
                .satisfies(result -> assertThat(result.failed()).containsOnlyKeys("item-2"))
                .satisfies(result -> assertThat(result.notFound()).containsExactly("item-3"))
                .satisfies(result -> assertThat(result.outcome("item-2"))
                        .get()
                        .isInstanceOf(DeletionOutcome.Failed.class))
                .extracting(result -> result.size())
                .isEqualTo(3);

        verifyDatasetItemDeleteRequests(1, "item-1");
        verifyDatasetItemDeleteRequests(1, "item-2");
        verifyDatasetItemDeleteRequests(1, "item-3");
    }

    @Test
    void deletingNothingIssuesNoRequest() {
        assertThat(langfuse.datasetItems().deleteById(List.of()).size()).isZero();

        verifyListRequests(0);
    }

    @Test
    void blankIdsAreRejectedBeforeAnyDelete() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.datasetItems().deleteById("  "))
                .withMessageContaining("Dataset item id");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.datasetItems().deleteById((String[]) null));
    }

    private static DatasetItemFilter datasetFilter() {
        return DatasetItemFilter.builder()
                .datasetName("my-dataset")
                .sourceTraceId("trace-1")
                .sourceObservationId("observation-1")
                .version(VERSION)
                .build();
    }

    private static CreateDatasetItemRequest createRequest() {
        return CreateDatasetItemRequest.builder()
                .datasetName("my-dataset")
                .build();
    }
}
