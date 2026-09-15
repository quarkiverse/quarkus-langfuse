package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.langfuse.api.model.CreateScoreConfigRequest;
import com.langfuse.api.model.ScoreConfig;
import com.langfuse.api.model.ScoreConfigDataType;

import io.quarkiverse.langfuse.api.AsyncLangfuseOperations;
import io.quarkiverse.langfuse.api.Page;
import io.quarkiverse.langfuse.api.PageSelection;
import io.quarkiverse.langfuse.api.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

class AsyncScoreConfigOperationsTests extends ScoreConfigOperationsTestSupport {
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
    void findByNameStopsAtTheFirstPageWhenTheConfigIsThere() {
        stubConfigs(7, 3);

        assertThat(await(asyncLangfuse.scoreConfigs().findByName("config-2")))
                .isNotNull()
                .extracting(ScoreConfig::getName)
                .isEqualTo("config-2");

        verifyListRequests(1);
        verifyPageRequested(0, 2);
    }

    @Test
    void findByNameEmitsNullWhenAbsent() {
        stubConfigs(7, 3);

        assertThat(await(asyncLangfuse.scoreConfigs().findByName("nope"))).isNull();

        verifyListRequests(3);
    }

    @Test
    void existsReflectsPresence() {
        stubConfigs(3, 3);

        assertThat(await(asyncLangfuse.scoreConfigs().exists("config-1"))).isTrue();
        assertThat(await(asyncLangfuse.scoreConfigs().exists("nope"))).isFalse();
    }

    @Test
    void blankNamesAreRejectedBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.scoreConfigs().findByName("  "))
                .withMessageContaining("Score config name");

        verifyListRequests(0);
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryPage() {
        stubConfigs(7, 3);

        assertThat(await(asyncLangfuse.scoreConfigs().findAll()))
                .hasSize(7)
                .extracting(ScoreConfig::getName)
                .startsWith("config-1")
                .endsWith("config-7");

        verifyListRequests(3);
    }

    @Test
    void boundedSelectionsRequestOnlyTheirPages() {
        stubConfigs(20, 3);

        assertThat(await(asyncLangfuse.scoreConfigs().find(PageSelection.rangeClosed(2, 3, 3))))
                .extracting(ScoreConfig::getName)
                .containsExactly("config-4", "config-5", "config-6", "config-7", "config-8", "config-9");

        verifyListRequests(2);
        verifyPageRequested(0, 1);
        verifyPageRequested(0, 4);
    }

    @Test
    void multisAreLazyUntilSubscribed() {
        stubConfigs(7, 3);

        var multi = asyncLangfuse.scoreConfigs().streamAll();

        verifyListRequests(0);

        assertThat(collect(multi.select().first(4))).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findPageExposesTheReportedTotals() {
        stubConfigs(7, 3);

        assertThat(await(asyncLangfuse.scoreConfigs().findPage(Page.of(2, 3))))
                .extracting(PagedResult::totalItems, PagedResult::totalPages, PagedResult::hasNext)
                .containsExactly(7, 3, true);
    }

    // --- error policy ----------------------------------------------------------------------

    @Test
    void findByNameTreatsNotFoundAsAbsence() {
        stubListingFailure(404);

        assertThat(await(asyncLangfuse.scoreConfigs().findByName("config-1"))).isNull();
        assertThat(await(asyncLangfuse.scoreConfigs().exists("config-1"))).isFalse();
    }

    @Test
    void findAllPropagatesNotFound() {
        stubListingFailure(404);

        assertThatThrownBy(() -> await(asyncLangfuse.scoreConfigs().findAll()))
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    // --- writes ----------------------------------------------------------------------------

    @Test
    void createIfAbsentReturnsTheExistingConfigWithoutCreating() {
        stubConfigs(3, 3);

        assertThat(await(asyncLangfuse.scoreConfigs().createIfAbsent(createRequest("config-2"))))
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

        assertThat(await(asyncLangfuse.scoreConfigs().createIfAbsent(createRequest("config-new"))))
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

    private static <T> T await(Uni<T> uni) {
        return uni.await().atMost(TIMEOUT);
    }

    private static <T> List<T> collect(Multi<T> multi) {
        return multi.collect().asList().await().atMost(TIMEOUT);
    }
}
