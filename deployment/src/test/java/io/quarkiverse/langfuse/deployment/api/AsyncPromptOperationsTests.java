package io.quarkiverse.langfuse.deployment.api;

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

import com.langfuse.api.model.CreateTextPromptRequest;
import com.langfuse.api.model.CreateTextPromptType;
import com.langfuse.api.model.Prompt;
import com.langfuse.api.model.PromptMeta;

import io.quarkiverse.langfuse.api.AsyncLangfuseOperations;
import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PageSelection;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseAuthenticationException;
import io.quarkiverse.langfuse.client.LangfuseAuthorizationException;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.mutiny.Uni;

class AsyncPromptOperationsTests extends PromptOperationsTestSupport {
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
    LangfuseOperations langfuse;

    @Inject
    AsyncLangfuseOperations asyncLangfuse;

    @BeforeEach
    void beforeEach() {
        resetAndGetWiremock();
    }

    // --- lookup ----------------------------------------------------------------------------

    @Test
    void findByNameResolvesInASingleRequestWithoutScanning() {
        stubPrompts(7, 3);
        stubPromptFound("prompt-2");

        assertThat(await(asyncLangfuse.prompts().findByName("prompt-2")))
                .isNotNull()
                .extracting(AsyncPromptOperationsTests::nameOf)
                .isEqualTo("prompt-2");

        verifyPromptGetRequests(1, "prompt-2");
        verifyListRequests(0);
    }

    @Test
    void findByNameEmitsNullWhenAbsent() {
        stubPromptFailure("nope", 404);

        assertThat(await(asyncLangfuse.prompts().findByName("nope"))).isNull();
    }

    /**
     * The asynchronous mirror of the absence-versus-failure rule: only a 404 is recovered, so a 401
     * fails the {@link Uni} rather than emitting {@code null}.
     */
    @Test
    void findByNameNeverMistakesARejectedCredentialForAbsence() {
        stubPromptFailure("prompt-1", 401);

        assertThatThrownBy(() -> await(asyncLangfuse.prompts().findByName("prompt-1")))
                .isInstanceOf(LangfuseAuthenticationException.class);
    }

    @Test
    void findByNameNeverMistakesARefusedActionForAbsence() {
        stubPromptFailure("prompt-1", 403);

        assertThatThrownBy(() -> await(asyncLangfuse.prompts().findByName("prompt-1")))
                .isInstanceOf(LangfuseAuthorizationException.class);
    }

    @Test
    void existsReflectsPresence() {
        stubPromptFound("prompt-1");
        stubPromptFailure("nope", 404);

        assertThat(await(asyncLangfuse.prompts().exists("prompt-1"))).isTrue();
        assertThat(await(asyncLangfuse.prompts().exists("nope"))).isFalse();
    }

    /**
     * Validation runs outside the deferred supplier, so a blank name throws from the call itself rather
     * than surfacing as a failed {@link Uni} at subscription.
     */
    @Test
    void blankNamesAreRejectedBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.prompts().findByName("  "))
                .withMessageContaining("Prompt name");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.prompts().findByName(null));

        verifyListRequests(0);
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryPage() {
        stubPrompts(7, 3);

        assertThat(await(asyncLangfuse.prompts().findAll()))
                .hasSize(7)
                .extracting(PromptMeta::getName)
                .startsWith("prompt-1")
                .endsWith("prompt-7");

        verifyListRequests(3);
    }

    @Test
    void boundedSelectionsRequestOnlyTheirPages() {
        stubPrompts(20, 3);

        assertThat(await(asyncLangfuse.prompts().find(PageSelection.rangeClosed(2, 3, 3))))
                .extracting(PromptMeta::getName)
                .containsExactly("prompt-4", "prompt-5", "prompt-6", "prompt-7", "prompt-8", "prompt-9");

        verifyListRequests(2);
        verifyPageRequested(0, 1);
    }

    @Test
    void findPageExposesTheReportedTotals() {
        stubPrompts(7, 3);

        assertThat(await(asyncLangfuse.prompts().findPage(Page.of(2, 3))))
                .satisfies(page -> assertThat(page.items()).hasSize(3))
                .extracting(PagedResult::totalItems, PagedResult::totalPages, PagedResult::hasNext)
                .containsExactly(7, 3, true);
    }

    @Test
    void findAllPropagatesNotFound() {
        stubListingFailure(404);

        assertThatThrownBy(() -> await(asyncLangfuse.prompts().findAll()))
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    // --- writes ----------------------------------------------------------------------------

    @Test
    void createIfAbsentReturnsTheExistingPromptWithoutCreating() {
        stubPromptFound("prompt-2");

        assertThat(await(asyncLangfuse.prompts().createIfAbsent(createRequest("prompt-2"))))
                .extracting(AsyncPromptOperationsTests::nameOf)
                .isEqualTo("prompt-2");

        verifyPromptsCreated(0);
    }

    @Test
    void createIfAbsentCreatesWhenMissing() {
        stubPromptFailure("prompt-new", 404);
        stubPromptCreated("prompt-new");

        assertThat(await(asyncLangfuse.prompts().createIfAbsent(createRequest("prompt-new"))))
                .extracting(AsyncPromptOperationsTests::nameOf)
                .isEqualTo("prompt-new");

        verifyPromptsCreated(1);
    }

    // --- deletion --------------------------------------------------------------------------

    /**
     * The whole point of this domain's delete: the endpoint is keyed on the prompt name, so no
     * name-to-id resolution - and therefore no listing request at all - is ever issued.
     */
    @Test
    void deleteByNameIssuesNoListRequest() {
        stubPrompts(7, 3);
        stubPromptDeleted("prompt-2");

        assertThat(await(asyncLangfuse.prompts().deleteByName("prompt-2")).deleted())
                .containsExactly("prompt-2");

        verifyPromptDeleteRequests(1, "prompt-2");
        verifyListRequests(0);
        verifyPromptGetRequests(0, "prompt-2");
    }

    @Test
    void deleteByNameReportsAbsenceRatherThanFailing() {
        stubPromptDeleteFailure("nope", 404);

        assertThat(await(asyncLangfuse.prompts().deleteByName("nope")))
                .satisfies(result -> assertThat(result.deleted()).isEmpty())
                .satisfies(result -> assertThat(result.notFound()).containsExactly("nope"));
    }

    @Test
    void deleteByNameNeverFailsFastAndReportsEachNameSeparately() {
        stubPromptDeleted("prompt-1");
        stubPromptDeleteFailure("prompt-2", 500);
        stubPromptDeleted("prompt-3");

        assertThat(await(asyncLangfuse.prompts().deleteByName(List.of("prompt-1", "prompt-2", "prompt-3"))))
                .satisfies(result -> assertThat(result.deleted()).containsExactly("prompt-1", "prompt-3"))
                .satisfies(result -> assertThat(result.outcomes()).hasSize(3))
                .satisfies(result -> assertThat(result.failed()).containsOnlyKeys("prompt-2"));
    }

    @Test
    void deleteByNameRejectsBlankNamesBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.prompts().deleteByName("  "))
                .withMessageContaining("Prompt name");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.prompts().deleteByName((String) null));

        verifyPromptDeleteRequests(0, "  ");
    }

    @Test
    void deletingNothingIssuesNoRequest() {
        assertThat(await(asyncLangfuse.prompts().deleteByName(List.of())).outcomes()).isEmpty();

        verifyListRequests(0);
    }

    // --- parity ----------------------------------------------------------------------------

    /**
     * The two trees are independent implementations, so they are checked against each other.
     */
    @Test
    void bothTreesReturnTheSamePromptsForTheSameRequests() {
        stubPrompts(7, 3);

        var sync = langfuse.prompts().findAll();

        resetRequests();

        assertThat(await(asyncLangfuse.prompts().findAll()))
                .extracting(PromptMeta::getName)
                .isEqualTo(sync.stream().map(PromptMeta::getName).toList());

        verifyListRequests(3);
    }

    private static <T> T await(Uni<T> uni) {
        return uni.await().atMost(TIMEOUT);
    }

    private static String nameOf(Prompt prompt) {
        return prompt.getTextPrompt1().getName();
    }

    private static CreateTextPromptRequest createRequest(String promptName) {
        return CreateTextPromptRequest.builder()
                .name(promptName)
                .prompt("hello")
                .type(CreateTextPromptType.TEXT)
                .build();
    }
}
