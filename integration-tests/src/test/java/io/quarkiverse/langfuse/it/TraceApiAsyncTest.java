package io.quarkiverse.langfuse.it;

import static com.langfuse.api.ingestion.IngestionApi.APIIngestionBatchRequest;
import static com.langfuse.api.trace.TraceApi.APITraceGetRequest;
import static com.langfuse.api.trace.TraceApi.APITraceListRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.LangfuseApiException;
import com.langfuse.api.model.IngestionBatchRequest;
import com.langfuse.api.model.IngestionEvent;
import com.langfuse.api.model.IngestionEventOneOf;
import com.langfuse.api.model.TraceBody;
import com.langfuse.api.model.TraceWithFullDetails;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.junit.QuarkusTest;

/**
 * Async integration tests for the Trace API.
 *
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class TraceApiAsyncTest {

    @Inject
    LangfuseApi client;

    @Inject
    LangfuseConfig config;

    private static final String TRACE_ID = UUID.randomUUID().toString();
    private static final String TRACE_NAME = "async-trace-api-test-" + UUID.randomUUID();

    @Test
    @Order(1)
    void ingestTrace() {
        var traceEvent = IngestionEventOneOf.builder()
                .id(UUID.randomUUID().toString())
                .timestamp(OffsetDateTime.now().toString())
                .type(IngestionEventOneOf.TypeEnum.TRACE_CREATE)
                .body(TraceBody.builder()
                        .id(TRACE_ID)
                        .name(TRACE_NAME)
                        .userId("async-test-user")
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
                .satisfies(response -> assertThat(response.getSuccesses()).isNotEmpty());
    }

    @Test
    @Order(2)
    void getTraceById() {
        await().atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptionsMatching(e -> e instanceof LangfuseApiException)
                .untilAsserted(() -> assertThat(client.asyncTrace().traceGet(
                        APITraceGetRequest.newBuilder()
                                .traceId(TRACE_ID)
                                .build()))
                        .succeedsWithin(Duration.ofSeconds(5))
                        .extracting(TraceWithFullDetails::getId, TraceWithFullDetails::getName)
                        .containsExactly(TRACE_ID, TRACE_NAME));
    }

    @Test
    @Order(2)
    void listTracesContainsIngestedTrace() {
        await().atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptionsMatching(e -> e instanceof LangfuseApiException)
                .untilAsserted(() -> assertThat(client.asyncTrace().traceList(
                        APITraceListRequest.newBuilder()
                                .name(TRACE_NAME)
                                .build()))
                        .succeedsWithin(Duration.ofSeconds(5))
                        .satisfies(traces -> assertThat(traces.getData())
                                .isNotEmpty()
                                .anyMatch(t -> TRACE_ID.equals(t.getId()))));
    }
}
