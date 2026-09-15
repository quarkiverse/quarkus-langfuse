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

import com.langfuse.api.model.CreateEvaluationRuleRequest;
import com.langfuse.api.model.EvaluationRule;

import io.quarkiverse.langfuse.api.AsyncLangfuseOperations;
import io.quarkiverse.langfuse.api.Cursor;
import io.quarkiverse.langfuse.api.CursorResult;
import io.quarkiverse.langfuse.api.CursorSelection;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

class AsyncEvaluationRuleOperationsTests extends EvaluationRuleOperationsTestSupport {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
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
    void findByNameStopsAtTheFirstBatchWhenTheRuleIsThere() {
        stubRules(7, 3);

        assertThat(await(asyncLangfuse.evaluationRules().findByName("rule-2")))
                .isNotNull()
                .extracting(EvaluationRule::getName)
                .isEqualTo("rule-2");

        verifyListRequests(1);
        verifyInitialBatchRequested(1);
    }

    @Test
    void findByNameWalksBatchesViaCursors() {
        stubRules(7, 3);

        assertThat(await(asyncLangfuse.evaluationRules().findByName("rule-5"))).isNotNull();

        verifyListRequests(2);
        verifyInitialBatchRequested(1);
        verifyResumedBatchRequested(1, "3");
    }

    @Test
    void findByNameEmitsNullWhenAbsent() {
        stubRules(7, 3);

        assertThat(await(asyncLangfuse.evaluationRules().findByName("nope"))).isNull();

        verifyListRequests(3);
    }

    @Test
    void existsReflectsPresence() {
        stubRules(3, 3);

        assertThat(await(asyncLangfuse.evaluationRules().exists("rule-1"))).isTrue();
        assertThat(await(asyncLangfuse.evaluationRules().exists("nope"))).isFalse();
    }

    @Test
    void blankNamesAreRejectedBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.evaluationRules().findByName("  "))
                .withMessageContaining("Evaluation rule name");

        verifyListRequests(0);
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryBatch() {
        stubRules(7, 3);

        assertThat(await(asyncLangfuse.evaluationRules().findAll()))
                .hasSize(7)
                .extracting(EvaluationRule::getName)
                .startsWith("rule-1")
                .endsWith("rule-7");

        verifyListRequests(3);
    }

    @Test
    void boundedCursorSelectionsStopAfterTheirLastBatch() {
        stubRules(20, 3);

        assertThat(await(asyncLangfuse.evaluationRules().find(CursorSelection.first(2, 3))))
                .extracting(EvaluationRule::getName)
                .containsExactly("rule-1", "rule-2", "rule-3", "rule-4", "rule-5", "rule-6");

        verifyListRequests(2);
    }

    @Test
    void multisAreLazyUntilSubscribed() {
        stubRules(7, 3);

        var multi = asyncLangfuse.evaluationRules().streamAll();

        verifyListRequests(0);

        assertThat(collect(multi.select().first(4))).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findBatchExposesTheNextCursor() {
        stubRules(7, 3);

        assertThat(await(asyncLangfuse.evaluationRules().findBatch(Cursor.first(3))))
                .satisfies(batch -> assertThat(batch.items()).hasSize(3))
                .satisfies(batch -> assertThat(batch.nextCursor()).contains(Cursor.at("3", 3)))
                .extracting(CursorResult::hasNext)
                .isEqualTo(true);
    }

    @Test
    void streamBatchesExposesEveryBatchIncludingTheLast() {
        stubRules(7, 3);

        assertThat(collect(asyncLangfuse.evaluationRules().streamBatches(CursorSelection.all(3))))
                .hasSize(3)
                .extracting(CursorResult::hasNext)
                .containsExactly(true, true, false);
    }

    // --- error policy ----------------------------------------------------------------------

    @Test
    void findByNameTreatsNotFoundAsAbsence() {
        stubListingFailure(404);

        assertThat(await(asyncLangfuse.evaluationRules().findByName("rule-1"))).isNull();
        assertThat(await(asyncLangfuse.evaluationRules().exists("rule-1"))).isFalse();
    }

    @Test
    void findAllPropagatesNotFound() {
        stubListingFailure(404);

        assertThatThrownBy(() -> await(asyncLangfuse.evaluationRules().findAll()))
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    // --- writes ----------------------------------------------------------------------------

    @Test
    void createIfAbsentReturnsTheExistingRuleWithoutCreating() {
        stubRules(3, 3);

        assertThat(await(asyncLangfuse.evaluationRules().createIfAbsent(createRequest("rule-2"))))
                .extracting(EvaluationRule::getName)
                .isEqualTo("rule-2");

        verifyRulesCreated(0);
    }

    @Test
    void createIfAbsentCreatesWhenMissing() {
        stubRules(3, 3);
        wiremock().register(post(urlPathEqualTo(RULES_PATH))
                .willReturn(okJson("""
                        {
                          \"id\": \"rule-new\",
                          \"name\": \"rule-new\",
                          \"projectId\": \"project-1\"
                        }""")));

        assertThat(await(asyncLangfuse.evaluationRules().createIfAbsent(createRequest("rule-new"))))
                .extracting(EvaluationRule::getName)
                .isEqualTo("rule-new");

        verifyRulesCreated(1);
    }

    private void stubListingFailure(int status) {
        wiremock().register(get(urlPathEqualTo(RULES_PATH))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"not found\"}")));
    }

    private static CreateEvaluationRuleRequest createRequest(String name) {
        return CreateEvaluationRuleRequest.builder()
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
