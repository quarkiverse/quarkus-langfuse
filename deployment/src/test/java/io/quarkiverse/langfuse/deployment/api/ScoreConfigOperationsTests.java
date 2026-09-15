package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
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

import com.langfuse.api.model.CreateScoreConfigRequest;
import com.langfuse.api.model.ScoreConfig;
import com.langfuse.api.model.ScoreConfigDataType;

import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.Page;
import io.quarkiverse.langfuse.api.PageSelection;
import io.quarkiverse.langfuse.api.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;

class ScoreConfigOperationsTests extends ScoreConfigOperationsTestSupport {

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
    void findByNameStopsAtTheFirstPageWhenTheConfigIsThere() {
        stubConfigs(7, 3);

        assertThat(langfuse.scoreConfigs().findByName("config-2"))
                .isPresent()
                .get()
                .extracting(ScoreConfig::getName)
                .isEqualTo("config-2");

        verifyListRequests(1);
        verifyPageRequested(0, 2);
    }

    @Test
    void findByNameWalksEveryPageBeforeReportingAbsence() {
        stubConfigs(7, 3);

        assertThat(langfuse.scoreConfigs().findByName("nope")).isEmpty();

        verifyListRequests(3);
    }

    @Test
    void existsReflectsPresence() {
        stubConfigs(3, 3);

        assertThat(langfuse.scoreConfigs().exists("config-1")).isTrue();
        assertThat(langfuse.scoreConfigs().exists("nope")).isFalse();
    }

    @Test
    void blankNamesAreRejectedBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.scoreConfigs().findByName("  "))
                .withMessageContaining("Score config name");

        verifyListRequests(0);
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryPage() {
        stubConfigs(7, 3);

        assertThat(langfuse.scoreConfigs().findAll())
                .hasSize(7)
                .extracting(ScoreConfig::getName)
                .startsWith("config-1")
                .endsWith("config-7");

        verifyListRequests(3);
    }

    @Test
    void boundedSelectionsRequestOnlyTheirPages() {
        stubConfigs(20, 3);

        assertThat(langfuse.scoreConfigs().find(PageSelection.rangeClosed(2, 3, 3)))
                .extracting(ScoreConfig::getName)
                .containsExactly("config-4", "config-5", "config-6", "config-7", "config-8", "config-9");

        verifyListRequests(2);
        verifyPageRequested(0, 1);
        verifyPageRequested(0, 4);
    }

    @Test
    void anEmptySelectionIssuesNoRequestAtAll() {
        stubConfigs(20, 3);

        assertThat(langfuse.scoreConfigs().find(PageSelection.range(2, 2, 3))).isEmpty();

        verifyListRequests(0);
    }

    @Test
    void streamsAreLazyUntilConsumed() {
        stubConfigs(7, 3);

        var stream = langfuse.scoreConfigs().streamAll();

        verifyListRequests(0);

        assertThat(stream.limit(4)).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findPageExposesTheReportedTotals() {
        stubConfigs(7, 3);

        assertThat(langfuse.scoreConfigs().findPage(Page.of(2, 3)))
                .satisfies(page -> assertThat(page.items()).hasSize(3))
                .satisfies(page -> assertThat(page.nextPage()).contains(Page.of(3, 3)))
                .extracting(PagedResult::totalItems, PagedResult::totalPages, PagedResult::hasNext)
                .containsExactly(7, 3, true);
    }

    @Test
    void streamPagesExposesEveryPage() {
        stubConfigs(7, 3);

        assertThat(langfuse.scoreConfigs().streamPages(PageSelection.all(3)))
                .hasSize(3)
                .extracting(PagedResult::hasNext)
                .containsExactly(true, true, false);
    }

    // --- error policy ----------------------------------------------------------------------

    @Test
    void findByNameTreatsNotFoundAsAbsence() {
        stubListingFailure(404);

        assertThat(langfuse.scoreConfigs().findByName("config-1")).isEmpty();
        assertThat(langfuse.scoreConfigs().exists("config-1")).isFalse();
    }

    @Test
    void findAllPropagatesNotFound() {
        stubListingFailure(404);

        assertThatThrownBy(() -> langfuse.scoreConfigs().findAll())
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    // --- writes ----------------------------------------------------------------------------

    @Test
    void createIfAbsentReturnsTheExistingConfigWithoutCreating() {
        stubConfigs(3, 3);

        assertThat(langfuse.scoreConfigs().createIfAbsent(createRequest("config-2")))
                .extracting(ScoreConfig::getName)
                .isEqualTo("config-2");

        verifyConfigsCreated(0);
    }

    @Test
    void createIfAbsentCreatesWhenMissing() {
        stubConfigs(3, 3);
        wiremock().register(post(urlPathEqualTo(SCORE_CONFIGS_PATH))
                .willReturn(okJson("""
                        {
                          \"id\": \"config-new\",
                          \"name\": \"config-new\",
                          \"dataType\": \"NUMERIC\",
                          \"projectId\": \"project-1\"
                        }
                        """)));

        assertThat(langfuse.scoreConfigs().createIfAbsent(createRequest("config-new")))
                .extracting(ScoreConfig::getName)
                .isEqualTo("config-new");

        verifyConfigsCreated(1);
    }

    private void stubListingFailure(int status) {
        wiremock().register(get(urlPathEqualTo(SCORE_CONFIGS_PATH))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"not found\"}")));
    }

    private static CreateScoreConfigRequest createRequest(String name) {
        return CreateScoreConfigRequest.builder()
                .name(name)
                .dataType(ScoreConfigDataType.NUMERIC)
                .build();
    }
}
