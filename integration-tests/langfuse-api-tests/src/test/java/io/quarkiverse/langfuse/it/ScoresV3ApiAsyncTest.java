package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.LangfuseApiException;
import com.langfuse.api.model.CreateScoreRequest;
import com.langfuse.api.model.CreateScoreSource;
import com.langfuse.api.model.CreateScoreValue;
import com.langfuse.api.model.ScoreDataType;
import com.langfuse.api.scores.ScoresApi.APIScoresCreateRequest;
import com.langfuse.api.scoresV3.ScoresV3Api.APIScoresV3GetManyV3Request;

import io.quarkus.test.junit.QuarkusTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class ScoresV3ApiAsyncTest {

    private static final String TRACE_ID = UUID.randomUUID().toString().replace("-", "");
    private static final String SCORE_NAME = "v3-async-test-score-" + UUID.randomUUID().toString().substring(0, 8);

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void setupTraceAndScore() {
        // Ingest trace via OTel, then create a score to query through the v3 endpoint
        OtelTestHelper.ingestTrace(client, TRACE_ID, "async-scores-v3-test-trace");

        assertThat(client.scores().scoresCreate(
                APIScoresCreateRequest.newBuilder()
                        .createScoreRequest(CreateScoreRequest.builder()
                                .traceId(TRACE_ID)
                                .name(SCORE_NAME)
                                .value(new CreateScoreValue(0.65))
                                .dataType(ScoreDataType.NUMERIC)
                                .source(CreateScoreSource.API)
                                .build())
                        .build()))
                .satisfies(response -> assertThat(response.getId()).isNotBlank());
    }

    @Test
    @Order(2)
    void listScoresV3AsyncContainsCreatedScore() {
        // Poll until score is queryable — score ingestion is eventually consistent
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptionsMatching(e -> (e instanceof LangfuseApiException))
                .untilAsserted(() -> assertThat(client.asyncScoresV3().scoresV3GetManyV3(
                        APIScoresV3GetManyV3Request.newBuilder()
                                .name(SCORE_NAME)
                                .traceId(TRACE_ID)
                                .build()))
                        .succeedsWithin(Duration.ofSeconds(5))
                        .satisfies(response -> {
                            assertThat(response.getData()).isNotEmpty();
                            assertThat(response.getMeta()).isNotNull();
                        }));
    }
}
