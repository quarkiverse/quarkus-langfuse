package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.metrics.MetricsApi.APIMetricsMetricsRequest;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class MetricsApiAsyncTest {

    @Inject
    LangfuseApi client;

    @Test
    void queryObservationCount() {
        var query = """
                {
                  "view": "observations",
                  "metrics": [{"measure": "count", "aggregation": "count"}],
                  "fromTimestamp": "2020-01-01T00:00:00.000Z",
                  "toTimestamp": "2099-01-01T00:00:00.000Z"
                }""";

        assertThat(client.asyncMetrics().metricsMetrics(
                APIMetricsMetricsRequest.newBuilder()
                        .query(query)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(response -> assertThat(response.getData()).isNotNull());
    }
}
