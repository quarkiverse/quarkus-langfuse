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
import com.langfuse.api.legacyScoreV1.LegacyScoreV1Api.APILegacyScoreV1DeleteRequest;
import com.langfuse.api.model.CreateScoreRequest;
import com.langfuse.api.model.CreateScoreSource;
import com.langfuse.api.model.CreateScoreValue;
import com.langfuse.api.model.ScoreDataType;
import com.langfuse.api.scores.ScoresApi.APIScoresCreateRequest;

import io.quarkus.test.junit.QuarkusTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class LegacyScoreV1ApiAsyncTest {

    private static final String TRACE_ID = UUID.randomUUID().toString().replace("-", "");
    private static String scoreId;

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void ingestTraceAndCreateScore() {
        OtelTestHelper.ingestTrace(client, TRACE_ID, "async-legacy-score-test-trace");

        await().atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptionsMatching(LangfuseApiException.class::isInstance)
                .untilAsserted(() -> assertThat(client.asyncScores().scoresCreate(
                        APIScoresCreateRequest.newBuilder()
                                .createScoreRequest(CreateScoreRequest.builder()
                                        .traceId(TRACE_ID)
                                        .name("async-legacy-delete-test")
                                        .value(new CreateScoreValue(1.0))
                                        .dataType(ScoreDataType.NUMERIC)
                                        .source(CreateScoreSource.API)
                                        .build())
                                .build()))
                        .succeedsWithin(Duration.ofSeconds(5))
                        .satisfies(response -> {
                            assertThat(response.getId()).isNotBlank();
                            scoreId = response.getId();
                        }));
    }

    @Test
    @Order(2)
    void deleteScoreViaLegacyEndpoint() {
        assertThat(client.asyncLegacyScoreV1().legacyScoreV1Delete(
                APILegacyScoreV1DeleteRequest.newBuilder()
                        .scoreId(scoreId)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5));
    }
}
