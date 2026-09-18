package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
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

import com.langfuse.api.model.AnnotationQueue;
import com.langfuse.api.model.CreateAnnotationQueueRequest;

import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PageSelection;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseAuthenticationException;
import io.quarkiverse.langfuse.client.LangfuseAuthorizationException;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;

class AnnotationQueueOperationsTests extends AnnotationQueueOperationsTestSupport {

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

    /**
     * {@code findById} is a direct GET, so it costs one request and never touches the listing - the
     * whole point of the method next to the scanning {@code findByName}.
     */
    @Test
    void findByIdResolvesInASingleRequestWithoutScanning() {
        stubQueues(7, 3);
        stubQueueFound("queue-2");

        assertThat(langfuse.annotationQueues().findById("queue-2"))
                .isPresent()
                .get()
                .extracting(AnnotationQueue::getId)
                .isEqualTo("queue-2");

        verifyQueueGetRequests(1, "queue-2");
        verifyListRequests(0);
    }

    @Test
    void findByIdIsEmptyWhenAbsent() {
        stubQueueFailure("nope", 404);

        assertThat(langfuse.annotationQueues().findById("nope")).isEmpty();
    }

    /**
     * Only a 404 is recovered: a rejected credential must propagate rather than read as absence.
     */
    @Test
    void findByIdNeverMistakesARejectedCredentialForAbsence() {
        stubQueueFailure("queue-1", 401);

        assertThatThrownBy(() -> langfuse.annotationQueues().findById("queue-1"))
                .isInstanceOf(LangfuseAuthenticationException.class);
    }

    @Test
    void findByIdNeverMistakesARefusedActionForAbsence() {
        stubQueueFailure("queue-1", 403);

        assertThatThrownBy(() -> langfuse.annotationQueues().findById("queue-1"))
                .isInstanceOf(LangfuseAuthorizationException.class);
    }

    @Test
    void findByNameStopsAtTheFirstPageWhenTheQueueIsThere() {
        stubQueues(7, 3);

        assertThat(langfuse.annotationQueues().findByName("queue-2"))
                .isPresent()
                .get()
                .extracting(AnnotationQueue::getName)
                .isEqualTo("queue-2");

        verifyListRequests(1);
        verifyPageRequested(0, 2);
    }

    @Test
    void findByNameWalksEveryPageBeforeReportingAbsence() {
        stubQueues(7, 3);

        assertThat(langfuse.annotationQueues().findByName("nope")).isEmpty();

        verifyListRequests(3);
    }

    @Test
    void existsReflectsPresence() {
        stubQueues(3, 3);

        assertThat(langfuse.annotationQueues().exists("queue-1")).isTrue();
        assertThat(langfuse.annotationQueues().exists("nope")).isFalse();
    }

    @Test
    void blankIdentifiersAreRejectedBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.annotationQueues().findById("  "))
                .withMessageContaining("Annotation queue id");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.annotationQueues().findByName("  "))
                .withMessageContaining("Annotation queue name");

        verifyListRequests(0);
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryPage() {
        stubQueues(7, 3);

        assertThat(langfuse.annotationQueues().findAll())
                .hasSize(7)
                .extracting(AnnotationQueue::getName)
                .startsWith("queue-1")
                .endsWith("queue-7");

        verifyListRequests(3);
    }

    @Test
    void boundedSelectionsRequestOnlyTheirPages() {
        stubQueues(20, 3);

        assertThat(langfuse.annotationQueues().find(PageSelection.rangeClosed(2, 3, 3)))
                .extracting(AnnotationQueue::getName)
                .containsExactly("queue-4", "queue-5", "queue-6", "queue-7", "queue-8", "queue-9");

        verifyListRequests(2);
        verifyPageRequested(0, 1);
        verifyPageRequested(0, 4);
    }

    @Test
    void streamsAreLazyUntilConsumed() {
        stubQueues(7, 3);

        var stream = langfuse.annotationQueues().streamAll();

        verifyListRequests(0);

        assertThat(stream.limit(4)).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findPageExposesTheReportedTotals() {
        stubQueues(7, 3);

        assertThat(langfuse.annotationQueues().findPage(Page.of(2, 3)))
                .satisfies(page -> assertThat(page.items()).hasSize(3))
                .satisfies(page -> assertThat(page.nextPage()).contains(Page.of(3, 3)))
                .extracting(PagedResult::totalItems, PagedResult::totalPages, PagedResult::hasNext)
                .containsExactly(7, 3, true);
    }

    // --- error policy ----------------------------------------------------------------------

    @Test
    void findByNameTreatsNotFoundAsAbsence() {
        stubListingFailure(404);

        assertThat(langfuse.annotationQueues().findByName("queue-1")).isEmpty();
    }

    @Test
    void findAllPropagatesNotFound() {
        stubListingFailure(404);

        assertThatThrownBy(() -> langfuse.annotationQueues().findAll())
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    // --- writes ----------------------------------------------------------------------------

    @Test
    void createIfAbsentReturnsTheExistingQueueWithoutCreating() {
        stubQueues(3, 3);

        assertThat(langfuse.annotationQueues().createIfAbsent(createRequest("queue-2")))
                .extracting(AnnotationQueue::getName)
                .isEqualTo("queue-2");

        verifyQueuesCreated(0);
    }

    @Test
    void createIfAbsentCreatesWhenMissing() {
        stubQueues(3, 3);
        wiremock().register(post(urlPathEqualTo(QUEUES_PATH))
                .willReturn(okJson("""
                        {
                          "id": "queue-new",
                          "name": "queue-new",
                          "description": "a queue",
                          "scoreConfigIds": [],
                          "createdAt": "2024-01-01T00:00:00Z",
                          "updatedAt": "2024-01-01T00:00:00Z"
                        }
                        """)));

        assertThat(langfuse.annotationQueues().createIfAbsent(createRequest("queue-new")))
                .extracting(AnnotationQueue::getName)
                .isEqualTo("queue-new");

        verifyQueuesCreated(1);
    }

    private static CreateAnnotationQueueRequest createRequest(String name) {
        return CreateAnnotationQueueRequest.builder()
                .name(name)
                .scoreConfigIds(List.of("config-1"))
                .build();
    }
}
