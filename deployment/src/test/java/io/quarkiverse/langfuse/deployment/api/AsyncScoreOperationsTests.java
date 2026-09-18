package io.quarkiverse.langfuse.deployment.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.langfuse.api.model.CreateScoreRequest;
import com.langfuse.api.model.CreateScoreResponse;
import com.langfuse.api.model.CreateScoreValue;
import com.langfuse.api.model.NumericScoreV31;
import com.langfuse.api.model.ScoreDataType;
import com.langfuse.api.model.ScoreSource;
import com.langfuse.api.model.ScoreV3;

import io.quarkiverse.langfuse.api.AsyncLangfuseOperations;
import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.ScoreFieldGroup;
import io.quarkiverse.langfuse.api.ScoreFilter;
import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorSelection;
import io.quarkiverse.langfuse.api.deletion.DeletionOutcome;
import io.quarkiverse.langfuse.client.LangfuseAuthenticationException;
import io.quarkiverse.langfuse.client.LangfuseAuthorizationException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.mutiny.Uni;

class AsyncScoreOperationsTests extends ScoreOperationsTestSupport {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final OffsetDateTime FROM_TIMESTAMP = OffsetDateTime.parse("2024-01-02T03:04:05Z");
    private static final OffsetDateTime TO_TIMESTAMP = OffsetDateTime.parse("2024-02-03T04:05:06Z");

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

    @Inject
    AsyncLangfuseOperations asyncLangfuse;

    @BeforeEach
    void beforeEach() {
        resetAndGetWiremock();
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryBatchViaCursors() {
        stubScores(7, 3);

        assertThat(await(asyncLangfuse.scores().findAll()))
                .hasSize(7)
                .extracting(AsyncScoreOperationsTests::scoreId)
                .startsWith("score-1")
                .endsWith("score-7");

        verifyListRequests(3);
        verifyInitialBatchRequested(1);
        verifyResumedBatchRequested(1, "3");
        verifyResumedBatchRequested(1, "6");
        verifyUnfilteredListRequests(3);
    }

    @Test
    void streamAllIssuesNothingUntilItIsSubscribed() {
        stubScores(7, 3);

        var stream = asyncLangfuse.scores().streamAll();

        verifyListRequests(0);

        assertThat(await(stream.collect().asList())).hasSize(7);

        verifyListRequests(3);
    }

    @Test
    void findBatchFetchesTheRequestedBatchAlone() {
        stubScores(7, 3);

        assertThat(await(asyncLangfuse.scores().findBatch(Cursor.at("3", 3))))
                .satisfies(batch -> assertThat(batch.items()).hasSize(3))
                .satisfies(batch -> assertThat(batch.nextCursor()).isPresent());

        verifyListRequests(1);
        verifyResumedBatchRequested(1, "3");
    }

    @Test
    void theLastBatchReportsNoNextCursor() {
        stubScores(7, 3);

        assertThat(await(asyncLangfuse.scores().findBatch(Cursor.at("6", 3))).nextCursor()).isEmpty();
    }

    /**
     * The absence-versus-failure rule on the traversal path: a rejected credential must fail the
     * {@link Uni} rather than read as an empty collection.
     */
    @Test
    void traversalNeverMistakesARejectedCredentialForAnEmptyCollection() {
        stubListingFailure(401);

        assertThatThrownBy(() -> await(asyncLangfuse.scores().findAll()))
                .isInstanceOf(LangfuseAuthenticationException.class);
    }

    @Test
    void traversalNeverMistakesARefusedActionForAnEmptyCollection() {
        stubListingFailure(403);

        assertThatThrownBy(() -> await(asyncLangfuse.scores().findAll()))
                .isInstanceOf(LangfuseAuthorizationException.class);
    }

    // --- filtering -------------------------------------------------------------------------

    @Test
    void filteredViewsSendEveryCriterionOnEveryBatchOfTheWalk() {
        stubScores(7, 3);

        assertThat(await(asyncLangfuse.scores().matching(fullFilter()).findAll())).hasSize(7);

        verifyListRequests(3);
        verifyFullyFilteredListRequests(3, "2024-01-02T03:04:05", "2024-02-03T04:05:06");
    }

    @Test
    void matchingReplacesTheFilterRatherThanCombiningIt() {
        stubScores(2, 3);

        var filter = ScoreFilter.builder()
                .traceId("another-trace-id")
                .build();

        assertThat(await(asyncLangfuse.scores().matching(fullFilter()).matching(filter).findAll())).hasSize(2);

        verifyListRequests(1);
        verifyTraceIdRequested(1, "another-trace-id");
        verifyTraceIdRequested(0, "the-trace-id");
    }

    @Test
    void matchingLeavesTheReceivingViewUnfiltered() {
        stubScores(2, 3);

        var scores = asyncLangfuse.scores();
        scores.matching(fullFilter());

        assertThat(await(scores.findAll())).hasSize(2);

        verifyUnfilteredListRequests(1);
    }

    @Test
    void matchingRejectsANullFilterBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.scores().matching(null))
                .withMessageContaining("Filter");

        verifyListRequests(0);
    }

    @Test
    void anEmptyFilterSendsNoCriteria() {
        stubScores(2, 3);

        assertThat(await(asyncLangfuse.scores().matching(ScoreFilter.none()).findAll())).hasSize(2);

        verifyUnfilteredListRequests(1);
    }

    // --- writes ----------------------------------------------------------------------------

    @Test
    void createReturnsTheAssignedId() {
        stubScoreCreated("score-new");

        assertThat(await(asyncLangfuse.scores().create(createRequest())))
                .isNotNull()
                .extracting(CreateScoreResponse::getId)
                .isEqualTo("score-new");

        verifyScoresCreated(1);
    }

    /**
     * Validation runs eagerly rather than inside the {@code completionStage} supplier, so a null
     * request is thrown from the call rather than emitted as a failure at subscription.
     */
    @Test
    void createRejectsANullRequestFromTheCallRatherThanAtSubscription() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.scores().create(null));

        verifyScoresCreated(0);
    }

    // --- deletion --------------------------------------------------------------------------

    @Test
    void deleteByIdIssuesOneRequestPerIdAndNoListing() {
        stubScoreDeleted("score-1");

        assertThat(await(asyncLangfuse.scores().deleteById("score-1")))
                .satisfies(result -> assertThat(result.deleted()).containsExactly("score-1"))
                .satisfies(result -> assertThat(result.hasFailures()).isFalse());

        verifyScoreDeleteRequests(1, "score-1");
        verifyListRequests(0);
    }

    @Test
    void deleteByIdReportsAbsenceRatherThanFailing() {
        stubScoreDeleteFailure("gone", 404);

        assertThat(await(asyncLangfuse.scores().deleteById("gone")))
                .satisfies(result -> assertThat(result.notFound()).containsExactly("gone"))
                .satisfies(result -> assertThat(result.hasFailures()).isFalse());
    }

    @Test
    void deleteByIdAttemptsEveryIdWhateverHappensToTheOthers() {
        stubScoreDeleted("score-1");
        stubScoreDeleteFailure("score-2", 500);
        stubScoreDeleteFailure("score-3", 404);

        assertThat(await(asyncLangfuse.scores().deleteById("score-1", "score-2", "score-3")))
                .satisfies(result -> assertThat(result.deleted()).containsExactly("score-1"))
                .satisfies(result -> assertThat(result.failed()).containsOnlyKeys("score-2"))
                .satisfies(result -> assertThat(result.notFound()).containsExactly("score-3"))
                .satisfies(result -> assertThat(result.outcome("score-2"))
                        .get()
                        .isInstanceOf(DeletionOutcome.Failed.class))
                .extracting(result -> result.size())
                .isEqualTo(3);

        verifyScoreDeleteRequests(1, "score-1");
        verifyScoreDeleteRequests(1, "score-2");
        verifyScoreDeleteRequests(1, "score-3");
    }

    @Test
    void deletingNothingIssuesNoRequest() {
        assertThat(await(asyncLangfuse.scores().deleteById(List.of())).size()).isZero();

        verifyListRequests(0);
    }

    @Test
    void blankIdsAreRejectedFromTheCallRatherThanAtSubscription() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.scores().deleteById("  "))
                .withMessageContaining("Score id");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.scores().deleteById((String[]) null));
    }

    // --- parity ----------------------------------------------------------------------------

    @Test
    void bothTreesWalkTheSameCollectionTheSameWay() {
        stubScores(7, 3);

        assertThat(await(asyncLangfuse.scores().findAll()))
                .extracting(AsyncScoreOperationsTests::scoreId)
                .isEqualTo(langfuse.scores().findAll().stream()
                        .map(AsyncScoreOperationsTests::scoreId)
                        .toList());
    }

    @Test
    void boundedSelectionsRequestOnlyTheirBatches() {
        stubScores(20, 3);

        assertThat(await(asyncLangfuse.scores().find(CursorSelection.only(Cursor.at("3", 3))))).hasSize(3);

        verifyListRequests(1);
        verifyResumedBatchRequested(1, "3");
    }

    // Every stubbed score is NUMERIC, which is the ScoreV3 variant carrying a Double value; the cast
    // doubles as an assertion that the polymorphic deserializer picked the right one.
    private static String scoreId(ScoreV3 score) {
        return ((NumericScoreV31) score.getActualInstance()).getId();
    }

    private static <T> T await(Uni<T> uni) {
        return uni.await().atMost(TIMEOUT);
    }

    private static ScoreFilter fullFilter() {
        return ScoreFilter.builder()
                .fields(ScoreFieldGroup.DETAILS, ScoreFieldGroup.SUBJECT)
                .id("the-id")
                .name("the-name")
                .source(ScoreSource.EVAL)
                .dataType(ScoreDataType.NUMERIC)
                .environment("the-environment")
                .configId("the-config-id")
                .queueId("the-queue-id")
                .authorUserId("the-author-user-id")
                .value("the-value")
                .valueMin(1.5d)
                .valueMax(2.5d)
                .traceId("the-trace-id")
                .sessionId("the-session-id")
                .observationId("the-observation-id")
                .experimentId("the-experiment-id")
                .fromTimestamp(FROM_TIMESTAMP)
                .toTimestamp(TO_TIMESTAMP)
                .build();
    }

    private static CreateScoreRequest createRequest() {
        return CreateScoreRequest.builder()
                .traceId("trace-1")
                .name("accuracy")
                .value(new CreateScoreValue(0.75d))
                .build();
    }
}
