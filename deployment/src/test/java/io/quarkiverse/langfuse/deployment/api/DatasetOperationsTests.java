package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.langfuse.api.model.CreateDatasetRequest;
import com.langfuse.api.model.Dataset;

import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.Page;
import io.quarkiverse.langfuse.api.PageSelection;
import io.quarkiverse.langfuse.api.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Unlike the other domains, {@code datasets().findByName} and {@code exists} resolve in a single
 * request, because Langfuse can look a dataset up by name directly. Everything else - traversal,
 * pagination and error policy - behaves the same as every other page-addressed domain, so this test
 * class mirrors {@code ModelOperationsTests} for those operations.
 */
class DatasetOperationsTests extends DatasetOperationsTestSupport {

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
        resetMappings();
        resetRequests();
    }

    // --- lookup ----------------------------------------------------------------------------

    @Test
    void findByNameCostsExactlyOneRequest() {
        stubDatasetFound("my-dataset");

        assertThat(langfuse.datasets().findByName("my-dataset"))
                .isPresent()
                .get()
                .extracting(Dataset::getName)
                .isEqualTo("my-dataset");

        verifyDatasetGetRequests(1, "my-dataset");
    }

    @Test
    void findByNameMatchesExactly() {
        stubDatasetFound("my-dataset");
        stubDatasetNotFound("MY-DATASET");

        assertThat(langfuse.datasets().findByName("MY-DATASET")).isEmpty();
    }

    @Test
    void existsReflectsPresence() {
        stubDatasetFound("my-dataset");
        stubDatasetNotFound("nope");

        assertThat(langfuse.datasets().exists("my-dataset")).isTrue();
        assertThat(langfuse.datasets().exists("nope")).isFalse();
    }

    @Test
    void aMissingDatasetIsAbsence() {
        stubDatasetNotFound("nope");

        assertThat(langfuse.datasets().findByName("nope")).isEmpty();
        assertThat(langfuse.datasets().exists("nope")).isFalse();
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryPage() {
        stubDatasets(7, 3);

        assertThat(langfuse.datasets().findAll())
                .hasSize(7)
                .extracting(Dataset::getName)
                .startsWith("dataset-1")
                .endsWith("dataset-7");

        verifyListRequests(3);
    }

    @Test
    void boundedSelectionsRequestOnlyTheirPages() {
        stubDatasets(20, 3);

        assertThat(langfuse.datasets().find(PageSelection.rangeClosed(2, 3, 3)))
                .extracting(Dataset::getName)
                .containsExactly("dataset-4", "dataset-5", "dataset-6", "dataset-7", "dataset-8", "dataset-9");

        verifyListRequests(2);
        verifyPageRequested(0, 1);
        verifyPageRequested(0, 4);
    }

    @Test
    void anEmptySelectionIssuesNoRequestAtAll() {
        stubDatasets(20, 3);

        assertThat(langfuse.datasets().find(PageSelection.range(2, 2, 3))).isEmpty();

        verifyListRequests(0);
    }

    @Test
    void streamsAreLazyUntilConsumed() {
        stubDatasets(7, 3);

        var stream = langfuse.datasets().streamAll();

        verifyListRequests(0);

        assertThat(stream.limit(4)).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findPageExposesTheReportedTotals() {
        stubDatasets(7, 3);

        assertThat(langfuse.datasets().findPage(Page.of(2, 3)))
                .satisfies(page -> assertThat(page.items()).hasSize(3))
                .satisfies(page -> assertThat(page.nextPage()).contains(Page.of(3, 3)))
                .extracting(PagedResult::totalItems, PagedResult::totalPages, PagedResult::hasNext)
                .containsExactly(7, 3, true);
    }

    @Test
    void streamPagesExposesEveryPage() {
        stubDatasets(7, 3);

        assertThat(langfuse.datasets().streamPages(PageSelection.all(3)))
                .hasSize(3)
                .extracting(PagedResult::hasNext)
                .containsExactly(true, true, false);
    }

    // --- error policy ----------------------------------------------------------------------

    @Test
    void findAllPropagatesFailuresFromTheListing() {
        stubListingFailure();

        assertThatThrownBy(() -> langfuse.datasets().findAll())
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    @Test
    void findPagePropagatesFailuresFromTheListing() {
        stubListingFailure();

        assertThatThrownBy(() -> langfuse.datasets().findPage(Page.of(1, 3)))
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    // --- writes ----------------------------------------------------------------------------

    @Test
    void createIfAbsentReturnsTheExistingDatasetWithoutCreating() {
        stubDatasetFound("my-dataset");

        assertThat(langfuse.datasets().createIfAbsent(createRequest("my-dataset")))
                .extracting(Dataset::getName)
                .isEqualTo("my-dataset");

        verifyDatasetsCreated(0);
    }

    @Test
    void createIfAbsentCreatesWhenMissing() {
        stubDatasetNotFound("new-dataset");
        wiremock().register(post(urlPathEqualTo(DATASETS_PATH))
                .willReturn(okJson("""
                        {
                          "id": "new-dataset",
                          "name": "new-dataset",
                          "projectId": "project-1"
                        }
                        """)));

        assertThat(langfuse.datasets().createIfAbsent(createRequest("new-dataset")))
                .extracting(Dataset::getName)
                .isEqualTo("new-dataset");

        verifyDatasetsCreated(1);
    }

    private void stubListingFailure() {
        wiremock().register(get(urlPathEqualTo(DATASETS_PATH))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"not found\"}")));
    }

    private static CreateDatasetRequest createRequest(String name) {
        return CreateDatasetRequest.builder()
                .name(name)
                .build();
    }
}
