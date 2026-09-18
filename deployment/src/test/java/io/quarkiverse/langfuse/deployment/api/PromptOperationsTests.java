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

import com.langfuse.api.model.CreateTextPromptRequest;
import com.langfuse.api.model.CreateTextPromptType;
import com.langfuse.api.model.Prompt;
import com.langfuse.api.model.PromptMeta;

import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PageSelection;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseAuthenticationException;
import io.quarkiverse.langfuse.client.LangfuseAuthorizationException;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;

class PromptOperationsTests extends PromptOperationsTestSupport {

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
     * Langfuse resolves a prompt name server-side, so this costs one request and never touches the
     * listing.
     */
    @Test
    void findByNameResolvesInASingleRequestWithoutScanning() {
        stubPrompts(7, 3);
        stubPromptFound("prompt-2");

        assertThat(langfuse.prompts().findByName("prompt-2"))
                .isPresent()
                .get()
                .extracting(PromptOperationsTests::nameOf)
                .isEqualTo("prompt-2");

        verifyPromptGetRequests(1, "prompt-2");
        verifyListRequests(0);
    }

    @Test
    void findByNameTreatsNotFoundAsAbsence() {
        stubPromptFailure("nope", 404);

        assertThat(langfuse.prompts().findByName("nope")).isEmpty();
    }

    /**
     * The absence-versus-failure rule: only a 404 is recovered, so a rejected credential must escape
     * rather than read as "no prompt with that name".
     */
    @Test
    void findByNameNeverMistakesARejectedCredentialForAbsence() {
        stubPromptFailure("prompt-1", 401);

        assertThatThrownBy(() -> langfuse.prompts().findByName("prompt-1"))
                .isInstanceOf(LangfuseAuthenticationException.class);
    }

    @Test
    void findByNameNeverMistakesARefusedActionForAbsence() {
        stubPromptFailure("prompt-1", 403);

        assertThatThrownBy(() -> langfuse.prompts().findByName("prompt-1"))
                .isInstanceOf(LangfuseAuthorizationException.class);
    }

    @Test
    void existsReflectsPresence() {
        stubPromptFound("prompt-1");
        stubPromptFailure("nope", 404);

        assertThat(langfuse.prompts().exists("prompt-1")).isTrue();
        assertThat(langfuse.prompts().exists("nope")).isFalse();
    }

    @Test
    void blankNamesAreRejectedBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.prompts().findByName("  "))
                .withMessageContaining("Prompt name");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.prompts().findByName(null));

        verifyListRequests(0);
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryPage() {
        stubPrompts(7, 3);

        assertThat(langfuse.prompts().findAll())
                .hasSize(7)
                .extracting(PromptMeta::getName)
                .startsWith("prompt-1")
                .endsWith("prompt-7");

        verifyListRequests(3);
    }

    @Test
    void boundedSelectionsRequestOnlyTheirPages() {
        stubPrompts(20, 3);

        assertThat(langfuse.prompts().find(PageSelection.rangeClosed(2, 3, 3)))
                .extracting(PromptMeta::getName)
                .containsExactly("prompt-4", "prompt-5", "prompt-6", "prompt-7", "prompt-8", "prompt-9");

        verifyListRequests(2);
        verifyPageRequested(1, 2);
        verifyPageRequested(1, 3);
        verifyPageRequested(0, 1);
    }

    @Test
    void streamsAreLazyUntilConsumed() {
        stubPrompts(7, 3);

        var stream = langfuse.prompts().streamAll();

        verifyListRequests(0);

        assertThat(stream.limit(4)).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findPageExposesTheReportedTotals() {
        stubPrompts(7, 3);

        assertThat(langfuse.prompts().findPage(Page.of(2, 3)))
                .satisfies(page -> assertThat(page.items()).hasSize(3))
                .satisfies(page -> assertThat(page.nextPage()).contains(Page.of(3, 3)))
                .extracting(PagedResult::totalItems, PagedResult::totalPages, PagedResult::hasNext)
                .containsExactly(7, 3, true);
    }

    @Test
    void findAllPropagatesNotFound() {
        stubListingFailure(404);

        assertThatThrownBy(() -> langfuse.prompts().findAll())
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    // --- writes ----------------------------------------------------------------------------

    @Test
    void createIfAbsentReturnsTheExistingPromptWithoutCreating() {
        stubPromptFound("prompt-2");

        assertThat(langfuse.prompts().createIfAbsent(createRequest("prompt-2")))
                .extracting(PromptOperationsTests::nameOf)
                .isEqualTo("prompt-2");

        verifyPromptsCreated(0);
    }

    @Test
    void createIfAbsentCreatesWhenMissing() {
        stubPromptFailure("prompt-new", 404);
        stubPromptCreated("prompt-new");

        assertThat(langfuse.prompts().createIfAbsent(createRequest("prompt-new")))
                .extracting(PromptOperationsTests::nameOf)
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

        assertThat(langfuse.prompts().deleteByName("prompt-2").deleted())
                .containsExactly("prompt-2");

        verifyPromptDeleteRequests(1, "prompt-2");
        verifyListRequests(0);
        verifyPromptGetRequests(0, "prompt-2");
    }

    @Test
    void deleteByNameReportsAbsenceRatherThanFailing() {
        stubPromptDeleteFailure("nope", 404);

        assertThat(langfuse.prompts().deleteByName("nope"))
                .satisfies(result -> assertThat(result.deleted()).isEmpty())
                .satisfies(result -> assertThat(result.notFound()).containsExactly("nope"));
    }

    @Test
    void deleteByNameNeverFailsFastAndReportsEachNameSeparately() {
        stubPromptDeleted("prompt-1");
        stubPromptDeleteFailure("prompt-2", 500);
        stubPromptDeleted("prompt-3");

        assertThat(langfuse.prompts().deleteByName(List.of("prompt-1", "prompt-2", "prompt-3")))
                .satisfies(result -> assertThat(result.deleted()).containsExactly("prompt-1", "prompt-3"))
                .satisfies(result -> assertThat(result.outcomes()).hasSize(3))
                .satisfies(result -> assertThat(result.failed()).containsOnlyKeys("prompt-2"));
    }

    @Test
    void deleteByNameRejectsBlankNamesBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.prompts().deleteByName("  "))
                .withMessageContaining("Prompt name");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.prompts().deleteByName((String) null));

        verifyPromptDeleteRequests(0, "  ");
    }

    @Test
    void deletingNothingIssuesNoRequest() {
        assertThat(langfuse.prompts().deleteByName(List.of()).outcomes()).isEmpty();

        verifyListRequests(0);
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
