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
import com.langfuse.api.legacyObservationsV1.LegacyObservationsV1Api.APILegacyObservationsV1GetManyRequest;
import com.langfuse.api.legacyObservationsV1.LegacyObservationsV1Api.APILegacyObservationsV1GetRequest;
import com.langfuse.api.observations.ObservationsApi.APIObservationsGetManyRequest;

import io.quarkus.test.junit.QuarkusTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class ObservationsApiAsyncTest {

    private static final String TRACE_ID = UUID.randomUUID().toString().replace("-", "");
    private static final String SPAN_NAME = "async-observations-test-span-" + UUID.randomUUID();

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void ingestTraceWithSpan() {
        // Ingest via OTel — legacy ingestion rejects trace-create in v4 events_only mode
        assertThat(OtelTestHelper.ingestTraceWithSpanAsync(client, TRACE_ID, "async-observations-test-trace", SPAN_NAME))
                .succeedsWithin(Duration.ofSeconds(5));
    }

    @Test
    @Order(2)
    void listObservationsViaV2Api() {
        // Poll until the OTel-ingested span appears — ingestion is eventually consistent
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptionsMatching(LangfuseApiException.class::isInstance)
                .untilAsserted(() -> assertThat(client.asyncObservations().observationsGetMany(
                        APIObservationsGetManyRequest.newBuilder()
                                .traceId(TRACE_ID)
                                .build()))
                        .succeedsWithin(Duration.ofSeconds(5))
                        .satisfies(observations -> assertThat(observations.getData())
                                .isNotEmpty()
                                .anyMatch(o -> SPAN_NAME.equals(o.getName()))));
    }

    @Test
    @Order(2)
    void legacyListObservationsReturns404() {
        assertThat(client.asyncLegacyObservationsV1().legacyObservationsV1GetMany(
                APILegacyObservationsV1GetManyRequest.newBuilder()
                        .traceId(TRACE_ID)
                        .build()))
                .failsWithin(Duration.ofSeconds(5))
                .withThrowableThat()
                .withCauseInstanceOf(LangfuseApiException.class);
    }

    @Test
    @Order(2)
    void legacyGetObservationByIdReturns404() {
        assertThat(client.asyncLegacyObservationsV1().legacyObservationsV1Get(
                APILegacyObservationsV1GetRequest.newBuilder()
                        .observationId(UUID.randomUUID().toString())
                        .build()))
                .failsWithin(Duration.ofSeconds(5))
                .withThrowableThat()
                .withCauseInstanceOf(LangfuseApiException.class);
    }
}
