package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.ingestion.IngestionApi.APIIngestionBatchRequest;
import com.langfuse.api.model.CreateScoreValue;
import com.langfuse.api.model.IngestionBatchRequest;
import com.langfuse.api.model.IngestionEvent;
import com.langfuse.api.model.ScoreBody;
import com.langfuse.api.model.ScoreDataType;
import com.langfuse.api.model.ScoreEvent1;
import com.langfuse.api.model.TraceBody;
import com.langfuse.api.model.TraceEvent1;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class IngestionApiAsyncTest {

    @Inject
    LangfuseApi client;

    @Inject
    LangfuseConfig config;

    @Test
    void traceCreateReturnsErrorInEventsOnlyMode() {
        var traceEvent = TraceEvent1.builder()
                .id(UUID.randomUUID().toString())
                .timestamp(OffsetDateTime.now().toString())
                .type(TraceEvent1.TypeEnum.TRACE_CREATE)
                .body(TraceBody.builder()
                        .id(UUID.randomUUID().toString())
                        .name("async-test-trace")
                        .environment(config.environment())
                        .build())
                .build();

        assertThat(client.asyncIngestion().ingestionBatch(
                APIIngestionBatchRequest.newBuilder()
                        .ingestionBatchRequest(IngestionBatchRequest.builder()
                                .batch(List.of(new IngestionEvent(traceEvent)))
                                .build())
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(response -> {
                    assertThat(response.getErrors())
                            .isNotEmpty();

                    assertThat(response.getSuccesses())
                            .isEmpty();
                });
    }

    @Test
    void multipleTraceCreatesReturnErrorsInEventsOnlyMode() {
        var events = List.of(
                new IngestionEvent(TraceEvent1.builder()
                        .id(UUID.randomUUID().toString())
                        .timestamp(OffsetDateTime.now().toString())
                        .type(TraceEvent1.TypeEnum.TRACE_CREATE)
                        .body(TraceBody.builder()
                                .id(UUID.randomUUID().toString())
                                .name("async-batch-trace-1")
                                .environment(config.environment())
                                .build())
                        .build()),
                new IngestionEvent(TraceEvent1.builder()
                        .id(UUID.randomUUID().toString())
                        .timestamp(OffsetDateTime.now().toString())
                        .type(TraceEvent1.TypeEnum.TRACE_CREATE)
                        .body(TraceBody.builder()
                                .id(UUID.randomUUID().toString())
                                .name("async-batch-trace-2")
                                .environment(config.environment())
                                .build())
                        .build()));

        assertThat(client.asyncIngestion().ingestionBatch(
                APIIngestionBatchRequest.newBuilder()
                        .ingestionBatchRequest(IngestionBatchRequest.builder()
                                .batch(events)
                                .build())
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(response -> {
                    assertThat(response.getErrors())
                            .hasSize(2);

                    assertThat(response.getSuccesses())
                            .isEmpty();
                });
    }

    @Test
    void scoreCreateSucceedsInEventsOnlyMode() {
        var traceId = UUID.randomUUID().toString();
        OtelTestHelper.ingestTrace(client, traceId, "async-score-ingestion-test-trace");

        var scoreEvent = ScoreEvent1.builder()
                .id(UUID.randomUUID().toString())
                .timestamp(OffsetDateTime.now().toString())
                .type(ScoreEvent1.TypeEnum.SCORE_CREATE)
                .body(ScoreBody.builder()
                        .traceId(traceId)
                        .name("async-ingestion-test-score")
                        .value(new CreateScoreValue(0.80))
                        .dataType(ScoreDataType.NUMERIC)
                        .build())
                .build();

        assertThat(client.asyncIngestion().ingestionBatch(
                APIIngestionBatchRequest.newBuilder()
                        .ingestionBatchRequest(IngestionBatchRequest.builder()
                                .batch(List.of(new IngestionEvent(scoreEvent)))
                                .build())
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(response -> {
                    assertThat(response.getSuccesses())
                            .hasSize(1);

                    assertThat(response.getErrors())
                            .isEmpty();
                });
    }
}
