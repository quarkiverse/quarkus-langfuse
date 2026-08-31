package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.LangfuseApiException;
import com.langfuse.api.legacyObservationsV1.LegacyObservationsV1Api.APILegacyObservationsV1GetManyRequest;
import com.langfuse.api.legacyObservationsV1.LegacyObservationsV1Api.APILegacyObservationsV1GetRequest;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Async integration tests for the Legacy Observations V1 API.
 *
 * <p>
 * In Langfuse v4 {@code events_only} mode, legacy observation endpoints return 404.
 */
@QuarkusTest
class LegacyObservationsV1ApiAsyncTest {

    @Inject
    LangfuseApi client;

    @Test
    void legacyGetObservationReturns404InEventsOnlyMode() {
        assertThat(client.asyncLegacyObservationsV1().legacyObservationsV1Get(
                APILegacyObservationsV1GetRequest.newBuilder()
                        .observationId(UUID.randomUUID().toString())
                        .build()))
                .failsWithin(Duration.ofSeconds(5))
                .withThrowableThat()
                .withCauseInstanceOf(LangfuseApiException.class);
    }

    @Test
    void legacyListObservationsReturns404InEventsOnlyMode() {
        assertThat(client.asyncLegacyObservationsV1().legacyObservationsV1GetMany(
                APILegacyObservationsV1GetManyRequest.newBuilder()
                        .build()))
                .failsWithin(Duration.ofSeconds(5))
                .withThrowableThat()
                .withCauseInstanceOf(LangfuseApiException.class);
    }
}
