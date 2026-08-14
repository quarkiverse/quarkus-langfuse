package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.LangfuseApiException;
import com.langfuse.api.legacyMetricsV1.LegacyMetricsV1Api.APILegacyMetricsV1MetricsRequest;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class LegacyMetricsV1ApiTest {

    @Inject
    LangfuseApi client;

    @Test
    void legacyMetricsReturns404InEventsOnlyMode() {
        // Legacy metrics endpoint returns 404 or 400 in v4 events_only mode
        var query = """
                {
                  "view": "traces",
                  "metrics": [{"measure": "count", "aggregation": "count"}],
                  "fromTimestamp": "2020-01-01T00:00:00.000Z",
                  "toTimestamp": "2099-01-01T00:00:00.000Z"
                }""";

        assertThatThrownBy(() -> client.legacyMetricsV1().legacyMetricsV1Metrics(
                APILegacyMetricsV1MetricsRequest.newBuilder()
                        .query(query)
                        .build()))
                .isInstanceOf(LangfuseApiException.class)
                .satisfies(e -> assertThat(((LangfuseApiException) e).getStatusCode())
                        .isIn(404, 400));
    }
}
