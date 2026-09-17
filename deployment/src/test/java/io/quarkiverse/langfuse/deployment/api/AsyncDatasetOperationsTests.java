package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.langfuse.api.LangfuseApiException;
import com.langfuse.api.model.CreateDatasetRequest;
import com.langfuse.api.model.Dataset;

import io.quarkiverse.langfuse.api.AsyncLangfuseOperations;
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

class AsyncDatasetOperationsTests extends DatasetOperationsTestSupport {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideRuntimeConfigKey("quarkus.langfuse.public-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.secret-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.api.default-page-size", "3")
            .overrideRuntimeConfigKey(LangfuseConfig.BASE_URL_KEY, wiremockUrlForConfig());

    @Inject
    AsyncLangfuseOperations asyncLangfuse;

    @BeforeEach
    void beforeEach() {
        resetMappings();
        resetRequests();
    }

    // --- lookup ----------------------------------------------------------------------------

    @Test
    void findByNameCostsExactlyOneRequest() {
        stubDatasetFound("my-dataset");

        assertThat(await(asyncLangfuse.datasets().findByName("my-dataset")))
                .extracting(Dataset::getName)
                .isEqualTo("my-dataset");

        verifyDatasetGetRequests(1, "my-dataset");
    }

    @Test
    void findByNameEmitsNullWhenAbsent() {
        stubDatasetNotFound("nope");

        assertThat(await(asyncLangfuse.datasets().findByName("nope"))).isNull();
    }

    @Test
    void existsReflectsPresence() {
        stubDatasetFound("my-dataset");
        stubDatasetNotFound("nope");

        assertThat(await(asyncLangfuse.datasets().exists("my-dataset"))).isTrue();
        assertThat(await(asyncLangfuse.datasets().exists("nope"))).isFalse();
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryPage() {
        stubDatasets(7, 3);

        assertThat(await(asyncLangfuse.datasets().findAll()))
                .hasSize(7)
                .extracting(Dataset::getName)
                .startsWith("dataset-1")
                .endsWith("dataset-7");

        verifyListRequests(3);
    }

    @Test
    void boundedSelectionsRequestOnlyTheirPages() {
        stubDatasets(20, 3);

        assertThat(await(asyncLangfuse.datasets().find(PageSelection.rangeClosed(2, 3, 3))))
                .extracting(Dataset::getName)
                .containsExactly("dataset-4", "dataset-5", "dataset-6", "dataset-7", "dataset-8", "dataset-9");

        verifyListRequests(2);
    }

    @Test
    void multisAreLazyUntilSubscribed() {
        stubDatasets(7, 3);

        var multi = asyncLangfuse.datasets().streamAll();

        verifyListRequests(0);

        assertThat(collect(multi.select().first(4))).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findPageExposesTheReportedTotals() {
        stubDatasets(7, 3);

        assertThat(await(asyncLangfuse.datasets().findPage(Page.of(2, 3))))
                .extracting(PagedResult::totalItems, PagedResult::totalPages, PagedResult::hasNext)
                .containsExactly(7, 3, true);
    }

    @Test
    void streamPagesEmitsEveryPageIncludingTheLast() {
        stubDatasets(7, 3);

        assertThat(collect(asyncLangfuse.datasets().streamPages(PageSelection.all(3))))
                .hasSize(3)
                .extracting(PagedResult::hasNext)
                .containsExactly(true, true, false);
    }

    // --- error policy ----------------------------------------------------------------------

    @Test
    void findAllPropagatesFailuresFromTheListing() {
        stubListingFailure();

        assertThatThrownBy(() -> await(asyncLangfuse.datasets().findAll()))
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    @Test
    void rejectedCredentialsFailTheUniRatherThanEmittingAnEmptyResult() {
        stubListingFailure(401);

        assertThatThrownBy(() -> await(asyncLangfuse.datasets().findAll()))
                .isInstanceOf(LangfuseAuthenticationException.class)
                .extracting(t -> ((LangfuseApiException) t).getStatusCode())
                .isEqualTo(401);
    }

    @Test
    void aRefusedActionFailsTheUniWithAnAuthorizationFailure() {
        stubListingFailure(403);

        assertThatThrownBy(() -> await(asyncLangfuse.datasets().findAll()))
                .isInstanceOf(LangfuseAuthorizationException.class);
    }

    /**
     * The asynchronous mirror of the absence-versus-failure separation: a 401 must fail the
     * {@code Uni} rather than emitting {@code null}, which is how absence is signalled here.
     */
    @Test
    void aRejectedCredentialIsNeverEmittedAsAnAbsentDataset() {
        stubDatasetFailure("my-dataset", 401);

        assertThatThrownBy(() -> await(asyncLangfuse.datasets().findByName("my-dataset")))
                .isInstanceOf(LangfuseAuthenticationException.class);
    }

    // --- writes ----------------------------------------------------------------------------

    @Test
    void createIfAbsentReturnsTheExistingDatasetWithoutCreating() {
        stubDatasetFound("my-dataset");

        assertThat(await(asyncLangfuse.datasets().createIfAbsent(createRequest("my-dataset"))))
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

        assertThat(await(asyncLangfuse.datasets().createIfAbsent(createRequest("new-dataset"))))
                .extracting(Dataset::getName)
                .isEqualTo("new-dataset");

        verifyDatasetsCreated(1);
    }

    private static CreateDatasetRequest createRequest(String name) {
        return CreateDatasetRequest.builder()
                .name(name)
                .build();
    }

    private static <T> T await(Uni<T> uni) {
        return uni.await().atMost(TIMEOUT);
    }

    private static <T> List<T> collect(Multi<T> multi) {
        return multi.collect().asList().await().atMost(TIMEOUT);
    }
}
