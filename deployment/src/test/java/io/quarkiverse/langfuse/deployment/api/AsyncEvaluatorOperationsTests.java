package io.quarkiverse.langfuse.deployment.api;

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

import com.langfuse.api.model.CodeEvaluator1;
import com.langfuse.api.model.CodeEvaluatorSourceCodeLanguage;
import com.langfuse.api.model.CreateCodeEvaluatorRequest1;
import com.langfuse.api.model.CreateEvaluatorRequest;
import com.langfuse.api.model.CreateLlmAsJudgeEvaluatorRequest1;

import io.quarkiverse.langfuse.api.AsyncLangfuseOperations;
import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorResult;
import io.quarkiverse.langfuse.api.cursor.CursorSelection;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusExtensionTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

class AsyncEvaluatorOperationsTests extends EvaluatorOperationsTestSupport {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @RegisterExtension
    static final QuarkusExtensionTest extensionTest = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideRuntimeConfigKey("quarkus.langfuse.public-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.secret-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.api.default-batch-size", "3")
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
    void findByNameStopsAtTheFirstBatchWhenTheEvaluatorIsThere() {
        stubEvaluators(7, 3);

        assertThat(await(asyncLangfuse.evaluators().findByName("evaluator-2")))
                .isNotNull()
                .extracting(evaluator -> ((CodeEvaluator1) evaluator.getActualInstance()).getName())
                .isEqualTo("evaluator-2");

        verifyListRequests(1);
        verifyInitialBatchRequested(1);
    }

    @Test
    void findByNameWalksBatchesViaCursors() {
        stubEvaluators(7, 3);

        assertThat(await(asyncLangfuse.evaluators().findByName("evaluator-5"))).isNotNull();

        verifyListRequests(2);
        verifyInitialBatchRequested(1);
        verifyResumedBatchRequested(1, "3");
    }

    @Test
    void findByNameEmitsNullWhenAbsent() {
        stubEvaluators(7, 3);

        assertThat(await(asyncLangfuse.evaluators().findByName("nope"))).isNull();

        verifyListRequests(3);
    }

    @Test
    void existsReflectsPresence() {
        stubEvaluators(3, 3);

        assertThat(await(asyncLangfuse.evaluators().exists("evaluator-1"))).isTrue();
        assertThat(await(asyncLangfuse.evaluators().exists("nope"))).isFalse();
    }

    @Test
    void blankNamesAreRejectedBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.evaluators().findByName("  "))
                .withMessageContaining("Evaluator name");

        verifyListRequests(0);
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryBatch() {
        stubEvaluators(7, 3);

        assertThat(await(asyncLangfuse.evaluators().findAll()))
                .hasSize(7)
                .extracting(evaluator -> ((CodeEvaluator1) evaluator.getActualInstance()).getName())
                .startsWith("evaluator-1")
                .endsWith("evaluator-7");

        verifyListRequests(3);
    }

    @Test
    void boundedCursorSelectionsStopAfterTheirLastBatch() {
        stubEvaluators(20, 3);

        assertThat(await(asyncLangfuse.evaluators().find(CursorSelection.first(2, 3))))
                .extracting(evaluator -> ((CodeEvaluator1) evaluator.getActualInstance()).getName())
                .containsExactly("evaluator-1", "evaluator-2", "evaluator-3", "evaluator-4", "evaluator-5", "evaluator-6");

        verifyListRequests(2);
    }

    @Test
    void multisAreLazyUntilSubscribed() {
        stubEvaluators(7, 3);

        var multi = asyncLangfuse.evaluators().streamAll();

        verifyListRequests(0);

        assertThat(collect(multi.select().first(4))).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findBatchExposesTheNextCursor() {
        stubEvaluators(7, 3);

        assertThat(await(asyncLangfuse.evaluators().findBatch(Cursor.first(3))))
                .satisfies(batch -> assertThat(batch.items()).hasSize(3))
                .satisfies(batch -> assertThat(batch.nextCursor()).contains(Cursor.at("3", 3)))
                .extracting(CursorResult::hasNext)
                .isEqualTo(true);
    }

    @Test
    void streamBatchesExposesEveryBatchIncludingTheLast() {
        stubEvaluators(7, 3);

        assertThat(collect(asyncLangfuse.evaluators().streamBatches(CursorSelection.all(3))))
                .hasSize(3)
                .extracting(CursorResult::hasNext)
                .containsExactly(true, true, false);
    }

    // --- error policy ----------------------------------------------------------------------

    @Test
    void findByNameTreatsNotFoundAsAbsence() {
        stubListingFailure(404);

        assertThat(await(asyncLangfuse.evaluators().findByName("evaluator-1"))).isNull();
        assertThat(await(asyncLangfuse.evaluators().exists("evaluator-1"))).isFalse();
    }

    @Test
    void findAllPropagatesNotFound() {
        stubListingFailure(404);

        assertThatThrownBy(() -> await(asyncLangfuse.evaluators().findAll()))
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    // --- writes ----------------------------------------------------------------------------

    @Test
    void createIfAbsentReturnsTheExistingEvaluatorWithoutCreating() {
        stubEvaluators(3, 3);

        assertThat(await(asyncLangfuse.evaluators().createIfAbsent(createRequest("evaluator-2"))))
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
                          \"id\": \"evaluator-new\",
                          \"name\": \"evaluator-new\",
                          \"projectId\": \"project-1\",
                          \"type\": \"code\",
                          \"sourceCode\": \"return 1\",
                          \"sourceCodeLanguage\": \"PYTHON\"
                        }""")));

        assertThat(await(asyncLangfuse.evaluators().createIfAbsent(createRequest("evaluator-new"))))
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
                          \"id\": \"evaluator-typed\",
                          \"name\": \"evaluator-typed\",
                          \"projectId\": \"project-1\",
                          \"type\": \"code\",
                          \"sourceCode\": \"return 1\",
                          \"sourceCodeLanguage\": \"PYTHON\"
                        }""")));

        var typedRequest = CreateCodeEvaluatorRequest1.builder()
                .name("evaluator-typed")
                .type(CreateCodeEvaluatorRequest1.TypeEnum.CODE)
                .sourceCode("return 1")
                .sourceCodeLanguage(CodeEvaluatorSourceCodeLanguage.PYTHON)
                .build();

        assertThat(await(asyncLangfuse.evaluators().createIfAbsent(typedRequest)))
                .extracting(evaluator -> ((CodeEvaluator1) evaluator.getActualInstance()).getName())
                .isEqualTo("evaluator-typed");

        verifyEvaluatorsCreated(1);
    }

    @Test
    void createIfAbsentRejectsNullRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.evaluators().createIfAbsent((CreateEvaluatorRequest) null))
                .withMessage("request must not be null");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.evaluators().createIfAbsent((CreateCodeEvaluatorRequest1) null))
                .withMessage("request must not be null");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.evaluators().createIfAbsent((CreateLlmAsJudgeEvaluatorRequest1) null))
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

    private static <T> T await(Uni<T> uni) {
        return uni.await().atMost(TIMEOUT);
    }

    private static <T> List<T> collect(Multi<T> multi) {
        return multi.collect().asList().await().atMost(TIMEOUT);
    }
}
