package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
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
import com.langfuse.api.model.CreateScoresResponse;
import com.langfuse.api.model.ScoreDataType;
import com.langfuse.api.scores.ScoresApi.APIScoresCreateRequest;

import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.ScoreFilter;
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
    LangfuseOperations langfuse;

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
                        .createScoreRequest(List.of(CreateScoreRequest.builder()
                                .traceId(TRACE_ID)
                                .name(SCORE_NAME)
                                .value(new CreateScoreValue(0.95))
                                .dataType(ScoreDataType.NUMERIC)
                                .source(CreateScoreSource.API)
                                .environment(config.environment())
                                .build()))
                        .build()))
                .isInstanceOfSatisfying(CreateScoresResponse.class,
                        response -> assertThat(response.getActualInstance()).isNotNull());
    }

    @Test
    @Order(2)
    void listScoresForTrace() {
        // Query via the scores collection API — the v3 list is now exposed through it.
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptionsMatching(LangfuseApiException.class::isInstance)
                .untilAsserted(() -> assertThat(langfuse.scores().matching(ScoreFilter.builder()
                        .name(SCORE_NAME)
                        .traceId(TRACE_ID)
                        .build()).findAll())
                        .isNotEmpty());
    }
}
