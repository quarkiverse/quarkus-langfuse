package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.LangfuseApiException;
import com.langfuse.api.legacyObservationsV1.LegacyObservationsV1Api.APILegacyObservationsV1GetManyRequest;
import com.langfuse.api.legacyObservationsV1.LegacyObservationsV1Api.APILegacyObservationsV1GetRequest;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Integration tests for the Legacy Observations V1 API.
 *
 * <p>
 * In Langfuse v4 {@code events_only} mode, legacy observation endpoints return 404.
 */
@QuarkusTest
class LegacyObservationsV1ApiTest {

    @Inject
    LangfuseApi client;

    @Test
    void legacyGetObservationReturns404InEventsOnlyMode() {
        assertThatThrownBy(() -> client.legacyObservationsV1().legacyObservationsV1Get(
                APILegacyObservationsV1GetRequest.newBuilder()
                        .observationId(UUID.randomUUID().toString())
                        .build()))
                .isInstanceOf(LangfuseApiException.class)
                .satisfies(e -> assertThat(((LangfuseApiException) e).getStatusCode())
                        .isIn(404, 400));
    }

    @Test
    void legacyListObservationsReturns404InEventsOnlyMode() {
        assertThatThrownBy(() -> client.legacyObservationsV1().legacyObservationsV1GetMany(
                APILegacyObservationsV1GetManyRequest.newBuilder()
                        .build()))
                .isInstanceOf(LangfuseApiException.class)
                .satisfies(e -> assertThat(((LangfuseApiException) e).getStatusCode())
                        .isIn(404, 400));
    }
}
