package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.langfuse.api.model.TraceDeleteMultipleRequest;
import com.langfuse.api.observations.ObservationsApi.APIObservationsGetManyRequest;
import com.langfuse.api.trace.TraceApi.APITraceDeleteMultipleRequest;
import com.langfuse.api.trace.TraceApi.APITraceDeleteRequest;
import com.langfuse.api.trace.TraceApi.APITraceGetRequest;
import com.langfuse.api.trace.TraceApi.APITraceListRequest;

import io.quarkus.test.junit.QuarkusTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class TraceApiTest {

    private static final String TRACE_ID = UUID.randomUUID().toString().replace("-", "");
    private static final String TRACE_NAME = "trace-api-test-" + UUID.randomUUID();

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void ingestTrace() {
        // Ingest via OTel — legacy ingestion rejects trace-create in v4 events_only mode
        OtelTestHelper.ingestTrace(client, TRACE_ID, TRACE_NAME);
    }

    @Test
    @Order(2)
    void getTraceByIdReturns404InEventsOnlyMode() {
        // Legacy GET /api/public/traces/{id} returns 404 in v4 events_only mode
        assertThatThrownBy(() -> client.trace().traceGet(
                APITraceGetRequest.newBuilder()
                        .traceId(TRACE_ID)
                        .build()))
                .isInstanceOf(LangfuseApiException.class)
                .satisfies(e -> assertThat(((LangfuseApiException) e).getStatusCode()).isEqualTo(404));
    }

    @Test
    @Order(2)
    void listTracesReturns404InEventsOnlyMode() {
        // Legacy GET /api/public/traces returns 404 in v4 events_only mode
        assertThatThrownBy(() -> client.trace().traceList(
                APITraceListRequest.newBuilder()
                        .name(TRACE_NAME)
                        .build()))
                .isInstanceOf(LangfuseApiException.class)
                .satisfies(e -> assertThat(((LangfuseApiException) e).getStatusCode()).isEqualTo(404));
    }

    @Test
    @Order(2)
    void queryTraceDataViaV2Observations() {
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptionsMatching(LangfuseApiException.class::isInstance)
                .untilAsserted(() -> assertThat(client.observations().observationsGetMany(
                        APIObservationsGetManyRequest.newBuilder()
                                .traceId(TRACE_ID)
                                .build()))
                        .satisfies(response -> assertThat(response.getData())
                                .isNotEmpty()
                                .anyMatch(o -> TRACE_NAME.equals(o.getName()))));
    }

    @Test
    @Order(3)
    void traceDelete() {
        assertThat(client.trace().traceDelete(
                APITraceDeleteRequest.newBuilder()
                        .traceId(TRACE_ID)
                        .build()))
                .satisfies(response -> assertThat(response.getMessage()).isNotBlank());
    }

    @Test
    @Order(3)
    void traceDeleteMultiple() {
        assertThat(client.trace().traceDeleteMultiple(
                APITraceDeleteMultipleRequest.newBuilder()
                        .traceDeleteMultipleRequest(TraceDeleteMultipleRequest.builder()
                                .traceIds(List.of(UUID.randomUUID().toString()))
                                .build())
                        .build()))
                .satisfies(response -> assertThat(response.getMessage()).isNotBlank());
    }
}
