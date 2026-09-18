package io.quarkiverse.langfuse.deployment.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.langfuse.api.model.AnnotationQueueItem;
import com.langfuse.api.model.AnnotationQueueObjectType;
import com.langfuse.api.model.AnnotationQueueStatus;
import com.langfuse.api.model.CreateAnnotationQueueItemRequest;
import com.langfuse.api.model.UpdateAnnotationQueueItemRequest;

import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PageSelection;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseAuthenticationException;
import io.quarkiverse.langfuse.client.LangfuseAuthorizationException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;

class AnnotationQueueItemOperationsTests extends AnnotationQueueItemOperationsTestSupport {

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

    // --- parent scoping --------------------------------------------------------------------

    /**
     * The point of the parent-scoped view: every page of the walk is addressed to the queue the view
     * was opened on, and nothing leaks to any other queue.
     */
    @Test
    void everyPageOfTheWalkIsAddressedToTheParentQueue() {
        stubItems(7, 3);

        assertThat(langfuse.annotationQueues().items(QUEUE_ID).findAll())
                .hasSize(7)
                .extracting(AnnotationQueueItem::getQueueId)
                .containsOnly(QUEUE_ID);

        verifyListRequests(3);
        verifyAnyItemsListRequests(3);
        verifyOtherQueueListRequests(0);
    }

    /**
     * Two views over different parents are independent: opening one on another queue does not
     * retarget the first, because each captures its own parent in its own fetcher.
     */
    @Test
    void viewsOverDifferentQueuesDoNotShareTheirParent() {
        stubItems(3, 3);

        var items = langfuse.annotationQueues().items(QUEUE_ID);
        langfuse.annotationQueues().items(OTHER_QUEUE_ID);

        assertThat(items.findAll()).hasSize(3);

        verifyListRequests(1);
        verifyOtherQueueListRequests(0);
    }

    /**
     * The parent id is validated in the accessor, so an invalid queue fails at {@code items(...)}
     * rather than later at the first traversal.
     */
    @Test
    void blankParentQueueIdsAreRejectedBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.annotationQueues().items("  "))
                .withMessageContaining("Queue id");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.annotationQueues().items(""))
                .withMessageContaining("Queue id");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.annotationQueues().items(null))
                .withMessageContaining("Queue id");

        verifyAnyItemsListRequests(0);
    }

    // --- lookup ----------------------------------------------------------------------------

    @Test
    void findByIdResolvesInASingleRequestWithoutScanning() {
        stubItems(7, 3);
        stubItemFound("item-2");

        assertThat(langfuse.annotationQueues().items(QUEUE_ID).findById("item-2"))
                .isPresent()
                .get()
                .extracting(AnnotationQueueItem::getId, AnnotationQueueItem::getQueueId)
                .containsExactly("item-2", QUEUE_ID);

        verifyItemGetRequests(1, "item-2");
        verifyListRequests(0);
    }

    @Test
    void findByIdIsEmptyWhenAbsent() {
        stubItemFailure("nope", 404);

        assertThat(langfuse.annotationQueues().items(QUEUE_ID).findById("nope")).isEmpty();
    }

    /**
     * Only a 404 is recovered: a rejected credential must propagate rather than read as absence.
     */
    @Test
    void findByIdNeverMistakesARejectedCredentialForAbsence() {
        stubItemFailure("item-1", 401);

        assertThatThrownBy(() -> langfuse.annotationQueues().items(QUEUE_ID).findById("item-1"))
                .isInstanceOf(LangfuseAuthenticationException.class);
    }

    @Test
    void findByIdNeverMistakesARefusedActionForAbsence() {
        stubItemFailure("item-1", 403);

        assertThatThrownBy(() -> langfuse.annotationQueues().items(QUEUE_ID).findById("item-1"))
                .isInstanceOf(LangfuseAuthorizationException.class);
    }

    @Test
    void blankItemIdsAreRejectedBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.annotationQueues().items(QUEUE_ID).findById("  "))
                .withMessageContaining("Queue item id");

        verifyItemGetRequests(0, "  ");
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void boundedSelectionsRequestOnlyTheirPages() {
        stubItems(20, 3);

        assertThat(langfuse.annotationQueues().items(QUEUE_ID).find(PageSelection.rangeClosed(2, 3, 3)))
                .extracting(AnnotationQueueItem::getId)
                .containsExactly("item-4", "item-5", "item-6", "item-7", "item-8", "item-9");

        verifyListRequests(2);
        verifyPageRequested(0, 1);
        verifyPageRequested(0, 4);
    }

    @Test
    void streamsAreLazyUntilConsumed() {
        stubItems(7, 3);

        var stream = langfuse.annotationQueues().items(QUEUE_ID).streamAll();

        verifyListRequests(0);

        assertThat(stream.limit(4)).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findPageExposesTheReportedTotals() {
        stubItems(7, 3);

        assertThat(langfuse.annotationQueues().items(QUEUE_ID).findPage(Page.of(2, 3)))
                .satisfies(page -> assertThat(page.items()).hasSize(3))
                .satisfies(page -> assertThat(page.nextPage()).contains(Page.of(3, 3)))
                .extracting(PagedResult::totalItems, PagedResult::totalPages, PagedResult::hasNext)
                .containsExactly(7, 3, true);
    }

    // --- writes ----------------------------------------------------------------------------

    @Test
    void createAddsAnItemToTheParentQueue() {
        stubItemCreated("item-9");

        assertThat(langfuse.annotationQueues().items(QUEUE_ID).create(createRequest()))
                .extracting(AnnotationQueueItem::getId, AnnotationQueueItem::getQueueId)
                .containsExactly("item-9", QUEUE_ID);

        verifyItemsCreated(1);
    }

    @Test
    void createRejectsANullRequestBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.annotationQueues().items(QUEUE_ID).create(null));

        verifyItemsCreated(0);
    }

    @Test
    void updatePatchesOnlyTheRequestedProperties() {
        stubItemUpdated("item-1", "COMPLETED");

        assertThat(langfuse.annotationQueues().items(QUEUE_ID).update("item-1", updateRequest()))
                .extracting(AnnotationQueueItem::getId, AnnotationQueueItem::getStatus)
                .containsExactly("item-1", AnnotationQueueStatus.COMPLETED);

        verifyItemUpdateRequests(1, "item-1");
    }

    @Test
    void updateRejectsBlankIdsAndNullRequestsBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.annotationQueues().items(QUEUE_ID).update("  ", updateRequest()))
                .withMessageContaining("Queue item id");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.annotationQueues().items(QUEUE_ID).update("item-1", null));

        verifyItemUpdateRequests(0, "item-1");
    }

    // --- deletion --------------------------------------------------------------------------

    /**
     * The endpoint needs both ids, but the queue id is fixed by the view, so the caller supplies the
     * item id alone and the delete is still addressed to the right queue.
     */
    @Test
    void deleteByIdAddressesTheParentQueueWithoutAnyListRequest() {
        stubItems(7, 3);
        stubItemDeleted("item-2");

        assertThat(langfuse.annotationQueues().items(QUEUE_ID).deleteById("item-2").deleted())
                .containsExactly("item-2");

        verifyItemDeleteRequests(1, "item-2");
        verifyListRequests(0);
    }

    @Test
    void deleteByIdReportsAbsenceRatherThanFailing() {
        stubItemDeleteFailure("nope", 404);

        assertThat(langfuse.annotationQueues().items(QUEUE_ID).deleteById("nope"))
                .satisfies(result -> assertThat(result.deleted()).isEmpty())
                .satisfies(result -> assertThat(result.notFound()).containsExactly("nope"));
    }

    @Test
    void deleteByIdNeverFailsFastAndReportsEachIdSeparately() {
        stubItemDeleted("item-1");
        stubItemDeleteFailure("item-2", 500);
        stubItemDeleted("item-3");

        assertThat(langfuse.annotationQueues().items(QUEUE_ID).deleteById(List.of("item-1", "item-2", "item-3")))
                .satisfies(result -> assertThat(result.deleted()).containsExactly("item-1", "item-3"))
                .satisfies(result -> assertThat(result.outcomes()).hasSize(3))
                .satisfies(result -> assertThat(result.failed()).containsOnlyKeys("item-2"));
    }

    @Test
    void deleteByIdRejectsBlankIdsBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.annotationQueues().items(QUEUE_ID).deleteById("  "))
                .withMessageContaining("Queue item id");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.annotationQueues().items(QUEUE_ID).deleteById((String) null));

        verifyItemDeleteRequests(0, "  ");
    }

    @Test
    void deletingNothingIssuesNoRequest() {
        assertThat(langfuse.annotationQueues().items(QUEUE_ID).deleteById(List.of()).outcomes()).isEmpty();

        verifyAnyItemsListRequests(0);
    }

    private static CreateAnnotationQueueItemRequest createRequest() {
        return CreateAnnotationQueueItemRequest.builder()
                .objectId("trace-9")
                .objectType(AnnotationQueueObjectType.TRACE)
                .build();
    }

    private static UpdateAnnotationQueueItemRequest updateRequest() {
        return UpdateAnnotationQueueItemRequest.builder()
                .status(AnnotationQueueStatus.COMPLETED)
                .build();
    }
}
