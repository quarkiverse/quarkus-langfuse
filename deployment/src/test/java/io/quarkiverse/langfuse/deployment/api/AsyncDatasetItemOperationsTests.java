package io.quarkiverse.langfuse.deployment.api;

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

import com.langfuse.api.model.CreateDatasetItemRequest;
import com.langfuse.api.model.DatasetItem;

import io.quarkiverse.langfuse.api.AsyncLangfuseOperations;
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
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

class AsyncDatasetItemOperationsTests extends DatasetItemOperationsTestSupport {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
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

    @Inject
    AsyncLangfuseOperations asyncLangfuse;

    @BeforeEach
    void beforeEach() {
        resetAndGetWiremock();
    }

    // --- lookup ----------------------------------------------------------------------------

    @Test
    void findByIdResolvesInASingleRequestWithoutScanning() {
        stubDatasetItems(7, 3);
        stubDatasetItemFound("item-2");

        assertThat(await(asyncLangfuse.datasetItems().findById("item-2")))
                .isNotNull()
                .extracting(DatasetItem::getId)
                .isEqualTo("item-2");

        verifyDatasetItemGetRequests(1, "item-2");
        verifyListRequests(0);
    }

    @Test
    void findByIdEmitsNullWhenAbsent() {
        stubDatasetItemFailure("nope", 404);

        assertThat(await(asyncLangfuse.datasetItems().findById("nope"))).isNull();
    }

    /**
     * The asynchronous mirror of the absence-versus-failure rule: only a 404 is recovered, so a 401
     * fails the {@link Uni} rather than emitting {@code null}.
     */
    @Test
    void findByIdNeverMistakesARejectedCredentialForAbsence() {
        stubDatasetItemFailure("item-1", 401);

        assertThatThrownBy(() -> await(asyncLangfuse.datasetItems().findById("item-1")))
                .isInstanceOf(LangfuseAuthenticationException.class);
    }

    @Test
    void findByIdNeverMistakesARefusedActionForAbsence() {
        stubDatasetItemFailure("item-1", 403);

        assertThatThrownBy(() -> await(asyncLangfuse.datasetItems().findById("item-1")))
                .isInstanceOf(LangfuseAuthorizationException.class);
    }

    /**
     * Validation runs outside the deferred supplier, so blank input is thrown from the call rather than
     * emitted as a failure at subscription time.
     */
    @Test
    void blankIdsAreRejectedBeforeTheUniIsEvenBuilt() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.datasetItems().findById("  "))
                .withMessageContaining("Dataset item id");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.datasetItems().findById(null));

        verifyListRequests(0);
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryPage() {
        stubDatasetItems(7, 3);

        assertThat(await(asyncLangfuse.datasetItems().findAll()))
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

        assertThat(await(asyncLangfuse.datasetItems().find(PageSelection.rangeClosed(2, 3, 3))))
                .extracting(DatasetItem::getId)
                .containsExactly("item-4", "item-5", "item-6", "item-7", "item-8", "item-9");

        verifyListRequests(2);
        verifyPageRequested(1, 2);
        verifyPageRequested(1, 3);
        verifyPageRequested(0, 1);
    }

    @Test
    void multisAreLazyUntilSubscribed() {
        stubDatasetItems(7, 3);

        var multi = asyncLangfuse.datasetItems().streamAll();

        verifyListRequests(0);

        assertThat(collect(multi.select().first(4))).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findPageExposesTheReportedTotals() {
        stubDatasetItems(7, 3);

        assertThat(await(asyncLangfuse.datasetItems().findPage(Page.of(2, 3))))
                .satisfies(page -> assertThat(page.items()).hasSize(3))
                .satisfies(page -> assertThat(page.nextPage()).contains(Page.of(3, 3)))
                .extracting(PagedResult::totalItems, PagedResult::totalPages, PagedResult::hasNext)
                .containsExactly(7, 3, true);
    }

    @Test
    void findAllPropagatesNotFound() {
        stubListingFailure(404);

        assertThatThrownBy(() -> await(asyncLangfuse.datasetItems().findAll()))
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    // --- filtering -------------------------------------------------------------------------

    @Test
    void filteredViewsSendTheirCriteriaOnEveryPageOfTheWalk() {
        stubDatasetItems(7, 3);

        assertThat(await(asyncLangfuse.datasetItems().matching(datasetFilter()).findAll()))
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

        assertThat(await(asyncLangfuse.datasetItems().matching(datasetFilter()).matching(filter).findAll())).hasSize(2);

        verifyListRequests(1);
        verifyDatasetNameRequested(1, "another-dataset");
        verifyDatasetNameRequested(0, "my-dataset");
    }

    @Test
    void matchingLeavesTheReceivingViewUnfiltered() {
        stubDatasetItems(2, 3);

        var datasetItems = asyncLangfuse.datasetItems();
        datasetItems.matching(datasetFilter());

        assertThat(await(datasetItems.findAll())).hasSize(2);

        verifyUnfilteredListRequests(1);
    }

    @Test
    void matchingRejectsANullFilterBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.datasetItems().matching(null))
                .withMessageContaining("Filter");

        verifyListRequests(0);
    }

    @Test
    void anEmptyFilterSendsNoCriteria() {
        stubDatasetItems(2, 3);

        assertThat(await(asyncLangfuse.datasetItems().matching(DatasetItemFilter.none()).findAll())).hasSize(2);

        verifyUnfilteredListRequests(1);
    }

    // --- writes ----------------------------------------------------------------------------

    @Test
    void createEmitsTheStoredItem() {
        stubDatasetItemCreated("item-new");

        assertThat(await(asyncLangfuse.datasetItems().create(createRequest())))
                .isNotNull()
                .extracting(DatasetItem::getId)
                .isEqualTo("item-new");

        verifyDatasetItemsCreated(1);
    }

    @Test
    void createRejectsANullRequestBeforeTheUniIsEvenBuilt() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.datasetItems().create(null));

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

        assertThat(await(asyncLangfuse.datasetItems().deleteById("item-1")))
                .satisfies(result -> assertThat(result.deleted()).containsExactly("item-1"))
                .satisfies(result -> assertThat(result.hasFailures()).isFalse());

        verifyDatasetItemDeleteRequests(1, "item-1");
        verifyListRequests(0);
    }

    @Test
    void deleteByIdReportsAbsenceRatherThanFailingTheUni() {
        stubDatasetItemDeleteFailure("gone", 404);

        assertThat(await(asyncLangfuse.datasetItems().deleteById("gone")))
                .satisfies(result -> assertThat(result.notFound()).containsExactly("gone"))
                .satisfies(result -> assertThat(result.hasFailures()).isFalse());
    }

    @Test
    void deleteByIdAttemptsEveryIdWhateverHappensToTheOthers() {
        stubDatasetItemDeleted("item-1");
        stubDatasetItemDeleteFailure("item-2", 500);
        stubDatasetItemDeleteFailure("item-3", 404);

        assertThat(await(asyncLangfuse.datasetItems().deleteById("item-1", "item-2", "item-3")))
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
        assertThat(await(asyncLangfuse.datasetItems().deleteById(List.of())).size()).isZero();

        verifyListRequests(0);
    }

    @Test
    void blankIdsAreRejectedBeforeTheDeleteUniIsEvenBuilt() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.datasetItems().deleteById("  "))
                .withMessageContaining("Dataset item id");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.datasetItems().deleteById((String[]) null));
    }

    // --- parity ----------------------------------------------------------------------------

    /**
     * The filtered view is proved across both trees too, since each builds its own request.
     */
    @Test
    void bothTreesSendTheSameFilterForTheSameCriteria() {
        stubDatasetItems(2, 3);

        langfuse.datasetItems().matching(datasetFilter()).findAll();

        resetRequests();

        assertThat(await(asyncLangfuse.datasetItems().matching(datasetFilter()).findAll())).hasSize(2);

        verifyFilteredListRequests(1, "my-dataset", "trace-1", "observation-1", "2024-01-02T03:04:05");
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

    private static <T> T await(Uni<T> uni) {
        return uni.await().atMost(TIMEOUT);
    }

    private static <T> List<T> collect(Multi<T> multi) {
        return multi.collect().asList().await().atMost(TIMEOUT);
    }
}
