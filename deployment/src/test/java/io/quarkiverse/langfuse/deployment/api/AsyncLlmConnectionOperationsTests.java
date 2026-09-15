package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
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

import com.langfuse.api.model.LlmAdapter;
import com.langfuse.api.model.LlmConnection;
import com.langfuse.api.model.UpsertLlmConnectionRequest;

import io.quarkiverse.langfuse.api.AsyncLangfuseOperations;
import io.quarkiverse.langfuse.api.Page;
import io.quarkiverse.langfuse.api.PageSelection;
import io.quarkiverse.langfuse.api.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

class AsyncLlmConnectionOperationsTests extends LlmConnectionOperationsTestSupport {
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
    void findByProviderStopsAtTheFirstPageWhenTheProviderIsThere() {
        stubConnections(7, 3);

        assertThat(await(asyncLangfuse.llmConnections().findByProvider("provider-2")))
                .isNotNull()
                .extracting(LlmConnection::getProvider)
                .isEqualTo("provider-2");

        verifyListRequests(1);
        verifyPageRequested(0, 2);
    }

    @Test
    void findByProviderEmitsNullWhenAbsent() {
        stubConnections(7, 3);

        assertThat(await(asyncLangfuse.llmConnections().findByProvider("nope"))).isNull();

        verifyListRequests(3);
    }

    @Test
    void existsReflectsPresence() {
        stubConnections(3, 3);

        assertThat(await(asyncLangfuse.llmConnections().exists("provider-1"))).isTrue();
        assertThat(await(asyncLangfuse.llmConnections().exists("nope"))).isFalse();
    }

    @Test
    void blankProvidersAreRejectedBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.llmConnections().findByProvider("  "))
                .withMessageContaining("Provider");

        verifyListRequests(0);
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryPage() {
        stubConnections(7, 3);

        assertThat(await(asyncLangfuse.llmConnections().findAll()))
                .hasSize(7)
                .extracting(LlmConnection::getProvider)
                .startsWith("provider-1")
                .endsWith("provider-7");

        verifyListRequests(3);
    }

    @Test
    void boundedSelectionsRequestOnlyTheirPages() {
        stubConnections(20, 3);

        assertThat(await(asyncLangfuse.llmConnections().find(PageSelection.rangeClosed(2, 3, 3))))
                .extracting(LlmConnection::getProvider)
                .containsExactly("provider-4", "provider-5", "provider-6", "provider-7", "provider-8", "provider-9");

        verifyListRequests(2);
        verifyPageRequested(0, 1);
        verifyPageRequested(0, 4);
    }

    @Test
    void multisAreLazyUntilSubscribed() {
        stubConnections(7, 3);

        var multi = asyncLangfuse.llmConnections().streamAll();

        verifyListRequests(0);

        assertThat(collect(multi.select().first(4))).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findPageExposesTheReportedTotals() {
        stubConnections(7, 3);

        assertThat(await(asyncLangfuse.llmConnections().findPage(Page.of(2, 3))))
                .extracting(PagedResult::totalItems, PagedResult::totalPages, PagedResult::hasNext)
                .containsExactly(7, 3, true);
    }

    // --- error policy ----------------------------------------------------------------------

    @Test
    void findByProviderTreatsNotFoundAsAbsence() {
        stubListingFailure(404);

        assertThat(await(asyncLangfuse.llmConnections().findByProvider("provider-1"))).isNull();
        assertThat(await(asyncLangfuse.llmConnections().exists("provider-1"))).isFalse();
    }

    @Test
    void findAllPropagatesNotFound() {
        stubListingFailure(404);

        assertThatThrownBy(() -> await(asyncLangfuse.llmConnections().findAll()))
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    // --- writes: atomic upsert -------------------------------------------------------------

    @Test
    void upsertCreatesTheConnection() {
        stubUpsert("openai", "openai");

        assertThat(await(asyncLangfuse.llmConnections().upsert(upsertRequest("openai", LlmAdapter.OPENAI))))
                .extracting(LlmConnection::getProvider, LlmConnection::getAdapter)
                .containsExactly("openai", "openai");

        verifyUpsertRequests(1);
    }

    @Test
    void upsertReplacesAnExistingConnectionWithoutLookingItUpFirst() {
        stubUpsert("openai", "azure");

        assertThat(await(asyncLangfuse.llmConnections().upsert(upsertRequest("openai", LlmAdapter.AZURE))))
                .extracting(LlmConnection::getAdapter)
                .isEqualTo("azure");

        verifyListRequests(0);
        verifyUpsertRequests(1);
    }

    private void stubListingFailure(int status) {
        wiremock().register(get(urlPathEqualTo(LLM_CONNECTIONS_PATH))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"not found\"}")));
    }

    private static UpsertLlmConnectionRequest upsertRequest(String provider, LlmAdapter adapter) {
        return UpsertLlmConnectionRequest.builder()
                .provider(provider)
                .adapter(adapter)
                .secretKey("sk-test")
                .build();
    }

    private static <T> T await(Uni<T> uni) {
        return uni.await().atMost(TIMEOUT);
    }

    private static <T> List<T> collect(Multi<T> multi) {
        return multi.collect().asList().await().atMost(TIMEOUT);
    }
}
