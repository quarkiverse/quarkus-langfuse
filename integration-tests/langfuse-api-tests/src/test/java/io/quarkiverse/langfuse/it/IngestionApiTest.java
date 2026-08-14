package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.langfuse.api.model.IngestionEventOneOf;
import com.langfuse.api.model.IngestionEventOneOf1;
import com.langfuse.api.model.IngestionSuccess;
import com.langfuse.api.model.ScoreBody;
import com.langfuse.api.model.ScoreDataType;
import com.langfuse.api.model.TraceBody;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class IngestionApiTest {

    @Inject
    LangfuseApi client;

    @Inject
    LangfuseConfig config;

    @Test
    void traceCreateReturnsErrorInEventsOnlyMode() {
        var eventId = UUID.randomUUID().toString();

        var traceEvent = IngestionEventOneOf.builder()
                .id(eventId)
                .timestamp(OffsetDateTime.now().toString())
                .type(IngestionEventOneOf.TypeEnum.TRACE_CREATE)
                .body(TraceBody.builder()
                        .id(UUID.randomUUID().toString())
                        .name("test-trace")
                        .environment(config.environment())
                        .build())
                .build();

        assertThat(client.ingestion().ingestionBatch(
                APIIngestionBatchRequest.newBuilder()
                        .ingestionBatchRequest(IngestionBatchRequest.builder()
                                .batch(List.of(new IngestionEvent(traceEvent)))
                                .build())
                        .build()))
                .satisfies(response -> {
                    assertThat(response.getErrors())
                            .isNotEmpty()
                            .first()
                            .satisfies(error -> assertThat(error.getId()).isEqualTo(eventId));
                    assertThat(response.getSuccesses()).isEmpty();
                });
    }

    @Test
    void multipleTraceCreatesReturnErrorsInEventsOnlyMode() {
        var eventId1 = UUID.randomUUID().toString();
        var eventId2 = UUID.randomUUID().toString();

        List<IngestionEvent> events = List.of(
                new IngestionEvent(IngestionEventOneOf.builder()
                        .id(eventId1)
                        .timestamp(OffsetDateTime.now().toString())
                        .type(IngestionEventOneOf.TypeEnum.TRACE_CREATE)
                        .body(TraceBody.builder()
                                .id(UUID.randomUUID().toString())
                                .name("batch-trace-1")
                                .environment(config.environment())
                                .build())
                        .build()),
                new IngestionEvent(IngestionEventOneOf.builder()
                        .id(eventId2)
                        .timestamp(OffsetDateTime.now().toString())
                        .type(IngestionEventOneOf.TypeEnum.TRACE_CREATE)
                        .body(TraceBody.builder()
                                .id(UUID.randomUUID().toString())
                                .name("batch-trace-2")
                                .environment(config.environment())
                                .build())
                        .build()));

        assertThat(client.ingestion().ingestionBatch(
                APIIngestionBatchRequest.newBuilder()
                        .ingestionBatchRequest(IngestionBatchRequest.builder()
                                .batch(events)
                                .build())
                        .build()))
                .satisfies(response -> {
                    assertThat(response.getErrors())
                            .hasSize(2)
                            .extracting("id")
                            .containsExactlyInAnyOrder(eventId1, eventId2);
                    assertThat(response.getSuccesses()).isEmpty();
                });
    }

    @Test
    void scoreCreateSucceedsInEventsOnlyMode() {
        var traceId = UUID.randomUUID().toString();
        OtelTestHelper.ingestTrace(client, traceId, "score-ingestion-test-trace");

        var eventId = UUID.randomUUID().toString();

        var scoreEvent = IngestionEventOneOf1.builder()
                .id(eventId)
                .timestamp(OffsetDateTime.now().toString())
                .type(IngestionEventOneOf1.TypeEnum.SCORE_CREATE)
                .body(ScoreBody.builder()
                        .traceId(traceId)
                        .name("ingestion-test-score")
                        .value(new CreateScoreValue(0.75))
                        .dataType(ScoreDataType.NUMERIC)
                        .build())
                .build();

        assertThat(client.ingestion().ingestionBatch(
                APIIngestionBatchRequest.newBuilder()
                        .ingestionBatchRequest(IngestionBatchRequest.builder()
                                .batch(List.of(new IngestionEvent(scoreEvent)))
                                .build())
                        .build()))
                .satisfies(response -> {
                    assertThat(response.getSuccesses())
                            .hasSize(1)
                            .first()
                            .extracting(IngestionSuccess::getId, IngestionSuccess::getStatus)
                            .containsExactly(eventId, 201);
                    assertThat(response.getErrors()).isEmpty();
                });
    }
}
