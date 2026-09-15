package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
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

import com.langfuse.api.model.LlmAdapter;
import com.langfuse.api.model.LlmConnection;
import com.langfuse.api.model.UpsertLlmConnectionRequest;

import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.Page;
import io.quarkiverse.langfuse.api.PageSelection;
import io.quarkiverse.langfuse.api.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;

class LlmConnectionOperationsTests extends LlmConnectionOperationsTestSupport {

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
    void findByProviderStopsAtTheFirstPageWhenTheProviderIsThere() {
        stubConnections(7, 3);

        assertThat(langfuse.llmConnections().findByProvider("provider-2"))
                .isPresent()
                .get()
                .extracting(LlmConnection::getProvider)
                .isEqualTo("provider-2");

        verifyListRequests(1);
        verifyPageRequested(0, 2);
    }

    @Test
    void findByProviderWalksEveryPageBeforeReportingAbsence() {
        stubConnections(7, 3);

        assertThat(langfuse.llmConnections().findByProvider("nope")).isEmpty();

        verifyListRequests(3);
    }

    @Test
    void existsReflectsPresence() {
        stubConnections(3, 3);

        assertThat(langfuse.llmConnections().exists("provider-1")).isTrue();
        assertThat(langfuse.llmConnections().exists("nope")).isFalse();
    }

    @Test
    void blankProvidersAreRejectedBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.llmConnections().findByProvider("  "))
                .withMessageContaining("Provider");

        verifyListRequests(0);
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryPage() {
        stubConnections(7, 3);

        assertThat(langfuse.llmConnections().findAll())
                .hasSize(7)
                .extracting(LlmConnection::getProvider)
                .startsWith("provider-1")
                .endsWith("provider-7");

        verifyListRequests(3);
    }

    @Test
    void boundedSelectionsRequestOnlyTheirPages() {
        stubConnections(20, 3);

        assertThat(langfuse.llmConnections().find(PageSelection.rangeClosed(2, 3, 3)))
                .extracting(LlmConnection::getProvider)
                .containsExactly("provider-4", "provider-5", "provider-6", "provider-7", "provider-8", "provider-9");

        verifyListRequests(2);
        verifyPageRequested(0, 1);
        verifyPageRequested(0, 4);
    }

    @Test
    void anEmptySelectionIssuesNoRequestAtAll() {
        stubConnections(20, 3);

        assertThat(langfuse.llmConnections().find(PageSelection.range(2, 2, 3))).isEmpty();

        verifyListRequests(0);
    }

    @Test
    void streamsAreLazyUntilConsumed() {
        stubConnections(7, 3);

        var stream = langfuse.llmConnections().streamAll();

        verifyListRequests(0);

        assertThat(stream.limit(4)).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findPageExposesTheReportedTotals() {
        stubConnections(7, 3);

        assertThat(langfuse.llmConnections().findPage(Page.of(2, 3)))
                .satisfies(page -> assertThat(page.items()).hasSize(3))
                .satisfies(page -> assertThat(page.nextPage()).contains(Page.of(3, 3)))
                .extracting(PagedResult::totalItems, PagedResult::totalPages, PagedResult::hasNext)
                .containsExactly(7, 3, true);
    }

    @Test
    void streamPagesExposesEveryPage() {
        stubConnections(7, 3);

        assertThat(langfuse.llmConnections().streamPages(PageSelection.all(3)))
                .hasSize(3)
                .extracting(PagedResult::hasNext)
                .containsExactly(true, true, false);
    }

    // --- error policy ----------------------------------------------------------------------

    @Test
    void findByProviderTreatsNotFoundAsAbsence() {
        stubListingFailure(404);

        assertThat(langfuse.llmConnections().findByProvider("provider-1")).isEmpty();
        assertThat(langfuse.llmConnections().exists("provider-1")).isFalse();
    }

    @Test
    void findAllPropagatesNotFound() {
        stubListingFailure(404);

        assertThatThrownBy(() -> langfuse.llmConnections().findAll())
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    // --- writes: the atomic upsert ----------------------------------------------------------

    @Test
    void upsertCreatesTheConnection() {
        stubUpsert("openai", "openai");

        assertThat(langfuse.llmConnections().upsert(upsertRequest("openai", LlmAdapter.OPENAI)))
                .extracting(LlmConnection::getProvider, LlmConnection::getAdapter)
                .containsExactly("openai", "openai");

        verifyUpsertRequests(1);
    }

    /**
     * The distinguishing behaviour of this domain: unlike every {@code createIfAbsent} elsewhere in the
     * layer, upsert is a single request regardless of whether the connection already existed - there is
     * no preceding lookup to make it non-atomic.
     */
    @Test
    void upsertReplacesAnExistingConnectionWithoutLookingItUpFirst() {
        stubUpsert("openai", "azure");

        assertThat(langfuse.llmConnections().upsert(upsertRequest("openai", LlmAdapter.AZURE)))
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
}
