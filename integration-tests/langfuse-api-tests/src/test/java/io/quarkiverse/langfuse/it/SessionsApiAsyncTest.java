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
import com.langfuse.api.observations.ObservationsApi.APIObservationsGetManyRequest;
import com.langfuse.api.sessions.SessionsApi.APISessionsGetRequest;
import com.langfuse.api.sessions.SessionsApi.APISessionsListRequest;

import io.quarkus.test.junit.QuarkusTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class SessionsApiAsyncTest {

    private static final String TRACE_ID = UUID.randomUUID().toString().replace("-", "");
    private static final String SESSION_ID = "async-test-session-" + UUID.randomUUID();

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void ingestTraceWithSession() {
        // Ingest via OTel with session attribute — legacy ingestion rejects trace-create in v4 events_only mode
        assertThat(OtelTestHelper.ingestTraceWithSessionAsync(client, TRACE_ID, "async-sessions-test-trace", SESSION_ID))
                .succeedsWithin(Duration.ofSeconds(5));
    }

    @Test
    @Order(2)
    void getSessionReturns404InEventsOnlyMode() {
        // Legacy GET /api/public/sessions/{id} returns 404 in v4 events_only mode
        assertThat(client.asyncSessions().sessionsGet(
                APISessionsGetRequest.newBuilder()
                        .sessionId(SESSION_ID)
                        .build()))
                .failsWithin(Duration.ofSeconds(5))
                .withThrowableThat()
                .withCauseInstanceOf(LangfuseApiException.class);
    }

    @Test
    @Order(2)
    void listSessionsReturns404InEventsOnlyMode() {
        // Legacy GET /api/public/sessions returns 404 in v4 events_only mode
        assertThat(client.asyncSessions().sessionsList(
                APISessionsListRequest.newBuilder()
                        .build()))
                .failsWithin(Duration.ofSeconds(5))
                .withThrowableThat()
                .withCauseInstanceOf(LangfuseApiException.class);
    }

    @Test
    @Order(2)
    void querySessionDataViaV2Observations() {
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptionsMatching(LangfuseApiException.class::isInstance)
                .untilAsserted(() -> assertThat(client.asyncObservations().observationsGetMany(
                        APIObservationsGetManyRequest.newBuilder()
                                .traceId(TRACE_ID)
                                .build()))
                        .succeedsWithin(Duration.ofSeconds(5))
                        .satisfies(response -> assertThat(response.getData())
                                .isNotEmpty()
                                .anyMatch(o -> "async-sessions-test-trace".equals(o.getName()))));
    }
}
