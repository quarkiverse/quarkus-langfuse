package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.langfuse.api.model.CreateModelRequest;
import com.langfuse.api.model.Model;
import com.langfuse.api.model.ModelUsageUnit;

import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.Page;
import io.quarkiverse.langfuse.api.PageSelection;
import io.quarkiverse.langfuse.api.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;

class ModelOperationsTests extends ModelOperationsTestSupport {

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
    void findByNameStopsAtTheFirstPageWhenTheModelIsThere() {
        stubModels(7, 3);

        assertThat(langfuse.models().findByName("model-2"))
                .isPresent()
                .get()
                .extracting(Model::getModelName)
                .isEqualTo("model-2");

        verifyListRequests(1);
        verifyPageRequested(0, 2);
    }

    @Test
    void findByNameWalksOnlyAsFarAsItMust() {
        stubModels(7, 3);

        assertThat(langfuse.models().findByName("model-5"))
                .isPresent();

        verifyListRequests(2);
        verifyPageRequested(0, 3);
    }

    /**
     * Names are matched exactly on every domain, whether the match happens on the server or here, so
     * that no operation quietly behaves differently from its neighbours.
     */
    @Test
    void findByNameMatchesExactly() {
        stubModels(3, 3);

        assertThat(langfuse.models().findByName("model-1")).isPresent();
        assertThat(langfuse.models().findByName("MODEL-1")).isEmpty();
        assertThat(langfuse.models().findByName("model-1 ")).isEmpty();
    }

    @Test
    void findByNameWalksEveryPageBeforeReportingAbsence() {
        stubModels(7, 3);

        assertThat(langfuse.models().findByName("nope")).isEmpty();

        verifyListRequests(3);
    }

    @Test
    void existsReflectsPresence() {
        stubModels(3, 3);

        assertThat(langfuse.models().exists("model-1")).isTrue();
        assertThat(langfuse.models().exists("nope")).isFalse();
    }

    @Test
    void blankNamesAreRejectedBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.models().findByName("  "))
                .withMessageContaining("Model name");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.models().findByName(null));

        verifyListRequests(0);
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryPage() {
        stubModels(7, 3);

        assertThat(langfuse.models().findAll())
                .hasSize(7)
                .extracting(Model::getModelName)
                .startsWith("model-1")
                .endsWith("model-7");

        verifyListRequests(3);
    }

    @Test
    void findAllUsesTheConfiguredDefaultPageSize() {
        stubModels(7, 3);

        assertThat(langfuse.models().findAll()).hasSize(7);

        wiremock().verifyThat(3, getRequestedFor(urlPathEqualTo(MODELS_PATH))
                .withQueryParam("limit", equalTo("3")));
    }

    @Test
    void aSelectionOverridesTheConfiguredPageSize() {
        stubModels(7, 5);

        assertThat(langfuse.models().find(PageSelection.all(5))).hasSize(7);

        wiremock().verifyThat(2, getRequestedFor(urlPathEqualTo(MODELS_PATH))
                .withQueryParam("limit", equalTo("5")));
    }

    @Test
    void boundedSelectionsRequestOnlyTheirPages() {
        stubModels(20, 3);

        assertThat(langfuse.models().find(PageSelection.rangeClosed(2, 3, 3)))
                .extracting(Model::getModelName)
                .containsExactly("model-4", "model-5", "model-6", "model-7", "model-8", "model-9");

        verifyListRequests(2);
        verifyPageRequested(1, 2);
        verifyPageRequested(1, 3);
        verifyPageRequested(0, 1);
        verifyPageRequested(0, 4);
    }

    @Test
    void anEmptySelectionIssuesNoRequestAtAll() {
        stubModels(20, 3);

        assertThat(langfuse.models().find(PageSelection.range(2, 2, 3))).isEmpty();

        verifyListRequests(0);
    }

    @Test
    void streamsAreLazyUntilConsumed() {
        stubModels(7, 3);

        var stream = langfuse.models().streamAll();

        verifyListRequests(0);

        assertThat(stream.limit(4)).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findPageExposesTheReportedTotals() {
        stubModels(7, 3);

        assertThat(langfuse.models().findPage(Page.of(2, 3)))
                .satisfies(page -> assertThat(page.items()).hasSize(3))
                .satisfies(page -> assertThat(page.nextPage()).contains(Page.of(3, 3)))
                .extracting(PagedResult::totalItems, PagedResult::totalPages, PagedResult::hasNext)
                .containsExactly(7, 3, true);
    }

    @Test
    void streamPagesExposesEveryPage() {
        stubModels(7, 3);

        assertThat(langfuse.models().streamPages(PageSelection.all(3)))
                .hasSize(3)
                .extracting(PagedResult::hasNext)
                .containsExactly(true, true, false);
    }

    // --- error policy ----------------------------------------------------------------------

    @Test
    void findByNameTreatsNotFoundAsAbsence() {
        stubNotFound();

        assertThat(langfuse.models().findByName("model-1")).isEmpty();
        assertThat(langfuse.models().exists("model-1")).isFalse();
    }

    @Test
    void findAllPropagatesNotFound() {
        stubNotFound();

        assertThatThrownBy(() -> langfuse.models().findAll())
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    @Test
    void findPagePropagatesNotFound() {
        stubNotFound();

        assertThatThrownBy(() -> langfuse.models().findPage(Page.of(1, 3)))
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    @Test
    void serverErrorsPropagate() {
        stubModels(7, 3);
        wiremock().register(
                get(urlPathEqualTo(MODELS_PATH))
                        .withQueryParam("page", equalTo("2"))
                        .willReturn(aResponse().withStatus(500).withBody("boom")));

        assertThatThrownBy(() -> langfuse.models().findAll())
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void failuresDuringTraversalPropagateFromTheTerminalOperation() {
        stubModels(7, 3);
        wiremock().register(
                get(urlPathEqualTo(MODELS_PATH))
                        .withQueryParam("page", equalTo("3"))
                        .willReturn(aResponse().withStatus(500).withBody("boom")));

        var stream = langfuse.models().streamAll();

        assertThatThrownBy(stream::toList)
                .isInstanceOf(RuntimeException.class);
    }

    // --- writes ----------------------------------------------------------------------------

    @Test
    void createIfAbsentReturnsTheExistingModelWithoutCreating() {
        stubModels(3, 3);

        assertThat(langfuse.models().createIfAbsent(createRequest("model-2")))
                .extracting(Model::getId)
                .isEqualTo("model-2");

        verifyModelsCreated(0);
    }

    @Test
    void createIfAbsentCreatesWhenMissing() {
        stubModels(3, 3);
        wiremock().register(
                post(urlPathEqualTo(MODELS_PATH))
                        .willReturn(okJson("""
                                {
                                  "id": "model-new",
                                  "modelName": "model-new",
                                  "isLangfuseManaged": false
                                }
                                """)));

        assertThat(langfuse.models().createIfAbsent(createRequest("model-new")))
                .extracting(Model::getId)
                .isEqualTo("model-new");

        verifyModelsCreated(1);
    }

    private void stubNotFound() {
        wiremock().register(
                get(urlPathEqualTo(MODELS_PATH))
                        .willReturn(aResponse()
                                .withStatus(404)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"message\":\"Models not found\"}")));
    }

    private static CreateModelRequest createRequest(String modelName) {
        return CreateModelRequest.builder()
                .modelName(modelName)
                .matchPattern("(?i)^(%s)$".formatted(modelName))
                .unit(ModelUsageUnit.TOKENS)
                .build();
    }
}
