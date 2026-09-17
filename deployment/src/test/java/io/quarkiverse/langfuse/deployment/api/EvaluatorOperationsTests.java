package io.quarkiverse.langfuse.deployment.api;

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

import com.langfuse.api.model.CodeEvaluator1;
import com.langfuse.api.model.CodeEvaluatorSourceCodeLanguage;
import com.langfuse.api.model.CreateCodeEvaluatorRequest1;
import com.langfuse.api.model.CreateEvaluatorRequest;
import com.langfuse.api.model.CreateLlmAsJudgeEvaluatorRequest1;

import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorResult;
import io.quarkiverse.langfuse.api.cursor.CursorSelection;
import io.quarkiverse.langfuse.client.LangfuseAuthenticationException;
import io.quarkiverse.langfuse.client.LangfuseAuthorizationException;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;

class EvaluatorOperationsTests extends EvaluatorOperationsTestSupport {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideRuntimeConfigKey("quarkus.langfuse.public-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.secret-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.api.default-batch-size", "3")
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
    void findByNameStopsAtTheFirstBatchWhenTheEvaluatorIsThere() {
        stubEvaluators(7, 3);

        assertThat(langfuse.evaluators().findByName("evaluator-2"))
                .isPresent()
                .get()
                .extracting(evaluator -> ((CodeEvaluator1) evaluator.getActualInstance()).getName())
                .isEqualTo("evaluator-2");

        verifyListRequests(1);
        verifyInitialBatchRequested(1);
    }

    @Test
    void findByNameWalksBatchesViaCursors() {
        stubEvaluators(7, 3);

        assertThat(langfuse.evaluators().findByName("evaluator-5")).isPresent();

        verifyListRequests(2);
        verifyInitialBatchRequested(1);
        verifyResumedBatchRequested(1, "3");
    }

    @Test
    void findByNameWalksEveryBatchBeforeReportingAbsence() {
        stubEvaluators(7, 3);

        assertThat(langfuse.evaluators().findByName("nope")).isEmpty();

        verifyListRequests(3);
    }

    @Test
    void existsReflectsPresence() {
        stubEvaluators(3, 3);

        assertThat(langfuse.evaluators().exists("evaluator-1")).isTrue();
        assertThat(langfuse.evaluators().exists("nope")).isFalse();
    }

    @Test
    void blankNamesAreRejectedBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.evaluators().findByName("  "))
                .withMessageContaining("Evaluator name");

        verifyListRequests(0);
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryBatch() {
        stubEvaluators(7, 3);

        assertThat(langfuse.evaluators().findAll())
                .hasSize(7)
                .extracting(evaluator -> ((CodeEvaluator1) evaluator.getActualInstance()).getName())
                .startsWith("evaluator-1")
                .endsWith("evaluator-7");

        verifyListRequests(3);
    }

    @Test
    void findAllUsesTheConfiguredDefaultBatchSize() {
        stubEvaluators(7, 3);

        assertThat(langfuse.evaluators().findAll()).hasSize(7);

        wiremock().verifyThat(3, getRequestedFor(urlPathEqualTo(EVALUATORS_PATH))
                .withQueryParam("limit", equalTo("3")));
    }

    @Test
    void aSelectionOverridesTheConfiguredBatchSize() {
        stubEvaluators(7, 5);

        assertThat(langfuse.evaluators().find(CursorSelection.all(5))).hasSize(7);

        wiremock().verifyThat(2, getRequestedFor(urlPathEqualTo(EVALUATORS_PATH))
                .withQueryParam("limit", equalTo("5")));
    }

    @Test
    void boundedCursorSelectionsStopAfterTheirLastBatch() {
        stubEvaluators(20, 3);

        assertThat(langfuse.evaluators().find(CursorSelection.first(2, 3)))
                .extracting(evaluator -> ((CodeEvaluator1) evaluator.getActualInstance()).getName())
                .containsExactly("evaluator-1", "evaluator-2", "evaluator-3", "evaluator-4", "evaluator-5", "evaluator-6");

        verifyListRequests(2);
    }

    @Test
    void anEmptyCursorSelectionIssuesNoRequestAtAll() {
        stubEvaluators(20, 3);

        assertThat(langfuse.evaluators().find(CursorSelection.first(0, 3))).isEmpty();

        verifyListRequests(0);
    }

    @Test
    void streamsAreLazyUntilConsumed() {
        stubEvaluators(7, 3);

        var stream = langfuse.evaluators().streamAll();

        verifyListRequests(0);

        assertThat(stream.limit(4)).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findBatchExposesTheNextCursor() {
        stubEvaluators(7, 3);

        assertThat(langfuse.evaluators().findBatch(Cursor.first(3)))
                .satisfies(batch -> assertThat(batch.items()).hasSize(3))
                .satisfies(batch -> assertThat(batch.nextCursor()).contains(Cursor.at("3", 3)))
                .extracting(CursorResult::hasNext)
                .isEqualTo(true);
    }

    @Test
    void streamBatchesExposesEveryBatchIncludingTheLast() {
        stubEvaluators(7, 3);

        assertThat(langfuse.evaluators().streamBatches(CursorSelection.all(3)))
                .hasSize(3)
                .extracting(CursorResult::hasNext)
                .containsExactly(true, true, false);
    }

    // --- error policy ----------------------------------------------------------------------

    @Test
    void findByNameTreatsNotFoundAsAbsence() {
        stubListingFailure(404);

        assertThat(langfuse.evaluators().findByName("evaluator-1")).isEmpty();
        assertThat(langfuse.evaluators().exists("evaluator-1")).isFalse();
    }

    @Test
    void findAllPropagatesNotFound() {
        stubListingFailure(404);

        assertThatThrownBy(() -> langfuse.evaluators().findAll())
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    /**
     * The cursor-addressed counterpart of the paged absence-versus-failure separation: unlike the
     * 404 above, a 401 must escape the scan rather than reading as "no such evaluator".
     */
    @Test
    void rejectedCredentialsAreNeverMistakenForAbsence() {
        stubListingFailure(401);

        assertThatThrownBy(() -> langfuse.evaluators().findByName("evaluator-1"))
                .isInstanceOf(LangfuseAuthenticationException.class);

        assertThatThrownBy(() -> langfuse.evaluators().exists("evaluator-1"))
                .isInstanceOf(LangfuseAuthenticationException.class);
    }

    @Test
    void aRefusedActionBecomesAnAuthorizationFailure() {
        stubListingFailure(403);

        assertThatThrownBy(() -> langfuse.evaluators().findAll())
                .isInstanceOf(LangfuseAuthorizationException.class);
    }

    // --- writes ----------------------------------------------------------------------------

    @Test
    void createIfAbsentReturnsTheExistingEvaluatorWithoutCreating() {
        stubEvaluators(3, 3);

        assertThat(langfuse.evaluators().createIfAbsent(createRequest("evaluator-2")))
                .extracting(evaluator -> ((CodeEvaluator1) evaluator.getActualInstance()).getName())
                .isEqualTo("evaluator-2");

        verifyEvaluatorsCreated(0);
    }

    @Test
    void createIfAbsentCreatesWhenMissing() {
        stubEvaluators(3, 3);
        wiremock().register(post(urlPathEqualTo(EVALUATORS_PATH))
                .willReturn(okJson("""
                        {
                          \"id\": \"evaluator-new\",\
                          \"name\": \"evaluator-new\",\
                          \"projectId\": \"project-1\",\
                          \"type\": \"code\",\
                          \"sourceCode\": \"return 1\",\
                          \"sourceCodeLanguage\": \"PYTHON\"\
                        }""")));

        assertThat(langfuse.evaluators().createIfAbsent(createRequest("evaluator-new")))
                .extracting(evaluator -> ((CodeEvaluator1) evaluator.getActualInstance()).getName())
                .isEqualTo("evaluator-new");

        verifyEvaluatorsCreated(1);
    }

    @Test
    void createIfAbsentWithTypedRequestCreatesWhenMissing() {
        stubEvaluators(3, 3);
        wiremock().register(post(urlPathEqualTo(EVALUATORS_PATH))
                .willReturn(okJson("""
                        {
                          \"id\": \"evaluator-typed\",\
                          \"name\": \"evaluator-typed\",\
                          \"projectId\": \"project-1\",\
                          \"type\": \"code\",\
                          \"sourceCode\": \"return 1\",\
                          \"sourceCodeLanguage\": \"PYTHON\"\
                        }""")));

        var typedRequest = CreateCodeEvaluatorRequest1.builder()
                .name("evaluator-typed")
                .type(CreateCodeEvaluatorRequest1.TypeEnum.CODE)
                .sourceCode("return 1")
                .sourceCodeLanguage(CodeEvaluatorSourceCodeLanguage.PYTHON)
                .build();

        assertThat(langfuse.evaluators().createIfAbsent(typedRequest))
                .extracting(evaluator -> ((CodeEvaluator1) evaluator.getActualInstance()).getName())
                .isEqualTo("evaluator-typed");

        verifyEvaluatorsCreated(1);
    }

    @Test
    void createIfAbsentRejectsNullRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.evaluators().createIfAbsent((CreateEvaluatorRequest) null))
                .withMessage("request must not be null");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.evaluators().createIfAbsent((CreateCodeEvaluatorRequest1) null))
                .withMessage("request must not be null");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.evaluators().createIfAbsent((CreateLlmAsJudgeEvaluatorRequest1) null))
                .withMessage("request must not be null");
    }

    private static CreateEvaluatorRequest createRequest(String name) {
        return new CreateEvaluatorRequest(CreateCodeEvaluatorRequest1.builder()
                .name(name)
                .type(CreateCodeEvaluatorRequest1.TypeEnum.CODE)
                .sourceCode("return 1")
                .sourceCodeLanguage(CodeEvaluatorSourceCodeLanguage.PYTHON)
                .build());
    }
}
