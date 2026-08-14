package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.langfuse.api.scores.ScoresApi.APIScoresGetByIdRequest;
import com.langfuse.api.scores.ScoresApi.APIScoresGetManyRequest;
import com.langfuse.api.scoresV3.ScoresV3Api.APIScoresV3GetManyV3Request;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.junit.QuarkusTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class ScoresApiTest {

    private static final String TRACE_ID = UUID.randomUUID().toString().replace("-", "");
    private static final String SCORE_NAME = "test-score";

    @Inject
    LangfuseApi client;

    @Inject
    LangfuseConfig config;

    @Test
    @Order(1)
    void ingestTrace() {
        // Ingest via OTel — legacy ingestion rejects trace-create in v4 events_only mode
        OtelTestHelper.ingestTrace(client, TRACE_ID, "score-test-trace");
    }

    @Test
    @Order(1)
    void createScore() {
        assertThat(client.scores().scoresCreate(
                APIScoresCreateRequest.newBuilder()
                        .createScoreRequest(CreateScoreRequest.builder()
                                .traceId(TRACE_ID)
                                .name(SCORE_NAME)
                                .value(new CreateScoreValue(0.95))
                                .dataType(ScoreDataType.NUMERIC)
                                .source(CreateScoreSource.API)
                                .environment(config.environment())
                                .build())
                        .build()))
                .satisfies(response -> assertThat(response.getId()).isNotBlank());
    }

    @Test
    @Order(2)
    void listScoresForTrace() {
        // Query via v3 endpoint — v2 scores endpoint returns 404 in v4 events_only mode
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptionsMatching(LangfuseApiException.class::isInstance)
                .untilAsserted(() -> assertThat(client.scoresV3().scoresV3GetManyV3(
                        APIScoresV3GetManyV3Request.newBuilder()
                                .name(SCORE_NAME)
                                .traceId(TRACE_ID)
                                .build()))
                        .satisfies(scores -> assertThat(scores.getData()).isNotEmpty()));
    }

    @Test
    @Order(3)
    void scoresGetManyReturns404() {
        assertThatThrownBy(() -> client.scores().scoresGetMany(
                APIScoresGetManyRequest.newBuilder()
                        .build()))
                .isInstanceOf(LangfuseApiException.class)
                .satisfies(e -> assertThat(((LangfuseApiException) e).getStatusCode()).isEqualTo(404));
    }

    @Test
    @Order(3)
    void scoresGetByIdReturns404() {
        assertThatThrownBy(() -> client.scores().scoresGetById(
                APIScoresGetByIdRequest.newBuilder()
                        .scoreId(UUID.randomUUID().toString())
                        .build()))
                .isInstanceOf(LangfuseApiException.class)
                .satisfies(e -> assertThat(((LangfuseApiException) e).getStatusCode()).isEqualTo(404));
    }
}
