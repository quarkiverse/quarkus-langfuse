package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import jakarta.inject.Inject;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.LangfuseApiException;
import com.langfuse.api.model.CreateScoreRequest;
import com.langfuse.api.model.CreateScoreSource;
import com.langfuse.api.model.CreateScoreValue;
import com.langfuse.api.model.ObservationV2;
import com.langfuse.api.model.ScoreDataType;
import com.langfuse.api.model.ScoreV3;

import io.quarkiverse.langfuse.api.AsyncLangfuseOperations;
import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.ObservationFilter;
import io.quarkiverse.langfuse.api.ScoreFilter;
import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorResult;
import io.quarkiverse.langfuse.api.cursor.CursorSelection;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;

/**
 * Multi-batch cursor traversals against a real Langfuse instance.
 *
 * <p>
 * This is the coverage no stub can provide. The {@code FakeCollection} behind the deployment tests
 * models a cursor as an integer offset encoded as a string, so those tests would pass just as
 * happily against an engine that did arithmetic on the token. Langfuse issues an opaque token
 * instead, and the only way to prove this layer treats it as opaque is to make a real server hand
 * one back and feed it straight to the next request.
 *
 * <p>
 * Every walk here uses a batch size of {@code 1}, so a collection of <em>n</em> fixtures forces
 * <em>n</em> server-issued cursors rather than one. {@link #aServerIssuedCursorResumesTheWalk()} then
 * takes a token out of the middle of a walk and restarts from it, which is the assertion that fails
 * if anything in the chain ever parses, compares or rewrites a cursor value.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class CursorTraversalTests {
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final Duration INGESTION_TIMEOUT = Duration.ofSeconds(60);
    private static final String RUN_ID = UUID.randomUUID().toString().substring(0, 8);

    // Scores are seeded onto a trace of this run's own, so the traceId criterion isolates the walk from
    // everything else accumulated in the instance by earlier runs.
    private static final String SCORE_TRACE_ID = UUID.randomUUID().toString().replace("-", "");
    private static final String SCORE_NAME_PREFIX = "it-cursor-score-" + RUN_ID + "-";
    private static final int SCORE_COUNT = 5;

    private static final String OBSERVATION_TRACE_ID = UUID.randomUUID().toString().replace("-", "");
    private static final String OBSERVATION_SPAN_NAME = "it-cursor-span-" + RUN_ID;

    @Inject
    LangfuseApi client;

    @Inject
    LangfuseOperations langfuse;

    @Inject
    AsyncLangfuseOperations asyncLangfuse;

    // --- Scenario 1: seed a cursor-addressed collection ------------------------------------

    @Test
    @Order(1)
    void seedScoresOnADedicatedTrace() {
        OtelTestHelper.ingestTrace(this.client, SCORE_TRACE_ID, "cursor-traversal-score-trace");

        var ids = IntStream.rangeClosed(1, SCORE_COUNT)
                .mapToObj(i -> CreateScoreRequest.builder()
                        .traceId(SCORE_TRACE_ID)
                        .name(SCORE_NAME_PREFIX + i)
                        .value(new CreateScoreValue((double) i / 10))
                        .dataType(ScoreDataType.NUMERIC)
                        .source(CreateScoreSource.API)
                        .build())
                .map(request -> this.langfuse.scores().create(request).getId())
                .toList();

        assertThat(ids)
                .hasSize(SCORE_COUNT)
                .doesNotHaveDuplicates()
                .allSatisfy(id -> assertThat(id).isNotBlank());
    }

    @Test
    @Order(2)
    void seedObservationsByIngestingATraceWithAChildSpan() {
        // OpenTelemetry ingestion plus the v2 observations listing is the one real-time path in
        // Langfuse, which is what makes this domain seedable at all despite being read-only.
        OtelTestHelper.ingestTraceWithSpan(this.client, OBSERVATION_TRACE_ID, "cursor-traversal-observation-trace",
                OBSERVATION_SPAN_NAME);
    }

    // --- Scenario 2: the walk itself, one item per batch ------------------------------------

    @Test
    @Order(3)
    void aOneItemBatchSizeForcesAMultiBatchWalkOverRealCursors() {
        // Score listing is eventually consistent, so the walk waits for the full fixture set rather
        // than asserting read-after-write.
        var batches = awaitScoreBatches();

        assertThat(batches)
                .hasSizeGreaterThanOrEqualTo(SCORE_COUNT)
                .allSatisfy(batch -> assertThat(batch.items()).hasSizeLessThanOrEqualTo(1));

        var cursorValues = batches.stream()
                .map(CursorResult::nextCursor)
                .flatMap(java.util.Optional::stream)
                .map(Cursor::value)
                .flatMap(java.util.Optional::stream)
                .toList();

        // Every batch but the last hands back a token, and each one differs from the last: a traversal
        // that reused or recomputed the token would show up here as duplicates.
        assertThat(cursorValues)
                .hasSizeGreaterThanOrEqualTo(SCORE_COUNT - 1)
                .doesNotHaveDuplicates()
                .allSatisfy(value -> assertThat(value).isNotBlank());

        var names = batches.stream()
                .flatMap(batch -> batch.items().stream())
                .map(CursorTraversalTests::scoreName)
                .toList();

        assertThat(names)
                .hasSizeGreaterThanOrEqualTo(SCORE_COUNT)
                .doesNotHaveDuplicates()
                .contains(SCORE_NAME_PREFIX + 1, SCORE_NAME_PREFIX + SCORE_COUNT);
    }

    @Test
    @Order(4)
    void aServerIssuedCursorResumesTheWalk() {
        var first = this.langfuse.scores().matching(scoreFilter()).findBatch(Cursor.first(1));

        assertThat(first.items()).hasSize(1);
        assertThat(first.hasNext()).isTrue();

        var token = first.nextCursor().orElseThrow().value().orElseThrow();

        // Cursor.at takes the server's token verbatim. Resuming from it reaches the second item, which
        // is only true if nothing between here and the wire interpreted the token.
        var resumed = this.langfuse.scores().matching(scoreFilter()).findBatch(Cursor.at(token, 1));

        assertThat(resumed.items()).hasSize(1);
        assertThat(scoreName(resumed.items().get(0)))
                .isNotEqualTo(scoreName(first.items().get(0)));

        // The same token drives a bounded selection to the same place.
        var fromToken = this.langfuse.scores().matching(scoreFilter())
                .find(CursorSelection.from(Cursor.at(token, 1), 2));

        assertThat(fromToken)
                .hasSize(2)
                .extracting(CursorTraversalTests::scoreName)
                .doesNotHaveDuplicates()
                .contains(scoreName(resumed.items().get(0)));
    }

    @Test
    @Order(5)
    void theAsyncTreeWalksTheSameRealCursors() {
        var syncNames = this.langfuse.scores().matching(scoreFilter())
                .stream(CursorSelection.all(1))
                .map(CursorTraversalTests::scoreName)
                .toList();

        var asyncNames = await(this.asyncLangfuse.scores().matching(scoreFilter())
                .stream(CursorSelection.all(1))
                .map(CursorTraversalTests::scoreName)
                .collect().asList());

        assertThat(asyncNames)
                .hasSizeGreaterThanOrEqualTo(SCORE_COUNT)
                .containsExactlyElementsOf(syncNames);

        var asyncBatches = await(this.asyncLangfuse.scores().matching(scoreFilter())
                .streamBatches(CursorSelection.all(1))
                .collect().asList());

        assertThat(asyncBatches)
                .hasSizeGreaterThanOrEqualTo(SCORE_COUNT)
                .allSatisfy(batch -> assertThat(batch.items()).hasSizeLessThanOrEqualTo(1));
    }

    // --- Scenario 3: a second cursor-addressed domain, on the real-time read path ------------

    @Test
    @Order(6)
    void observationsWalkMoreThanOneRealBatchToo() {
        var filter = ObservationFilter.builder()
                .traceId(OBSERVATION_TRACE_ID)
                .build();

        // The ingested trace carries a root span and a child span, so a batch size of 1 needs two
        // requests and one server-issued cursor between them.
        var batches = awaitObservationBatches(filter);

        assertThat(batches)
                .hasSizeGreaterThanOrEqualTo(2)
                .allSatisfy(batch -> assertThat(batch.items()).hasSizeLessThanOrEqualTo(1));

        assertThat(batches.get(0).nextCursor())
                .isPresent()
                .get()
                .extracting(Cursor::value)
                .satisfies(value -> assertThat(value).isPresent());

        var names = batches.stream()
                .flatMap(batch -> batch.items().stream())
                .map(ObservationV2::getName)
                .toList();

        assertThat(names)
                .hasSizeGreaterThanOrEqualTo(2)
                .contains(OBSERVATION_SPAN_NAME);

        var asyncNames = await(this.asyncLangfuse.observations().matching(filter)
                .stream(CursorSelection.all(1))
                .map(ObservationV2::getName)
                .collect().asList());

        assertThat(asyncNames).containsExactlyElementsOf(names);
    }

    // --- Scenario 4: bounded selections stop where they are told -----------------------------

    @Test
    @Order(7)
    void aBoundedSelectionStopsAfterTheRequestedNumberOfRealBatches() {
        var twoBatches = this.langfuse.scores().matching(scoreFilter())
                .find(CursorSelection.first(2, 1));

        assertThat(twoBatches)
                .hasSize(2)
                .extracting(CursorTraversalTests::scoreName)
                .doesNotHaveDuplicates();

        var single = this.langfuse.scores().matching(scoreFilter())
                .find(CursorSelection.first(1, 1));

        assertThat(single)
                .hasSize(1)
                .extracting(CursorTraversalTests::scoreName)
                .isEqualTo(List.of(scoreName(twoBatches.get(0))));

        assertThat(this.langfuse.scores().matching(scoreFilter()).find(CursorSelection.first(0, 1)))
                .isEmpty();
    }

    private List<CursorResult<ScoreV3>> awaitScoreBatches() {
        var batches = new java.util.concurrent.atomic.AtomicReference<List<CursorResult<ScoreV3>>>(List.of());

        // Awaitility is qualified rather than statically imported: this class also carries the module's
        // await(Uni) helper, and the two names collide.
        Awaitility.await().atMost(INGESTION_TIMEOUT)
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptionsMatching(LangfuseApiException.class::isInstance)
                .untilAsserted(() -> {
                    batches.set(this.langfuse.scores().matching(scoreFilter())
                            .streamBatches(CursorSelection.all(1))
                            .toList());

                    assertThat(batches.get()).hasSizeGreaterThanOrEqualTo(SCORE_COUNT);
                });

        return batches.get();
    }

    private List<CursorResult<ObservationV2>> awaitObservationBatches(ObservationFilter filter) {
        var batches = new java.util.concurrent.atomic.AtomicReference<List<CursorResult<ObservationV2>>>(List.of());

        Awaitility.await().atMost(INGESTION_TIMEOUT)
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptionsMatching(LangfuseApiException.class::isInstance)
                .untilAsserted(() -> {
                    batches.set(this.langfuse.observations().matching(filter)
                            .streamBatches(CursorSelection.all(1))
                            .toList());

                    assertThat(batches.get()).hasSizeGreaterThanOrEqualTo(2);
                });

        return batches.get();
    }

    private static ScoreFilter scoreFilter() {
        return ScoreFilter.builder()
                .traceId(SCORE_TRACE_ID)
                .build();
    }

    // Every fixture here is created with ScoreDataType.NUMERIC, so the oneOf wrapper always carries the
    // numeric variant.
    private static String scoreName(ScoreV3 score) {
        return score.getNumericScoreV31().getName();
    }

    private static <T> T await(Uni<T> uni) {
        return uni.await().atMost(TIMEOUT);
    }
}
