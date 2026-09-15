package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.util.stream.IntStream;

import io.quarkiverse.langfuse.deployment.WiremockAware;

/**
 * Serves a fake {@code /api/public/v2/evaluation-rules} cursor-addressed collection.
 */
abstract class EvaluationRuleOperationsTestSupport extends WiremockAware {
    static final String RULES_PATH = "/api/public/v2/evaluation-rules";

    void stubRules(int itemCount, int batchSize) {
        var totalBatches = Math.max(1, (itemCount + batchSize - 1) / batchSize);

        IntStream.rangeClosed(1, totalBatches)
                .forEach(batch -> stubBatch(batch, batchSize, itemCount, totalBatches));
    }

    private void stubBatch(int batch, int batchSize, int itemCount, int totalBatches) {
        var from = (batch - 1) * batchSize;
        var to = Math.min(from + batchSize, itemCount);

        var rules = IntStream.range(from, to)
                .mapToObj(index -> """
                        {
                          \"id\": \"rule-%1$d\",
                          \"name\": \"rule-%1$d\",
                          \"projectId\": \"project-1\"
                        }""".formatted(index + 1))
                .toList();

        // The cursor requested to GET this batch: empty on batch 1, the previous batch's "to" otherwise
        var requestedCursor = (batch == 1) ? null : String.valueOf(from);

        // The next cursor emitted in this batch's response: the new "to" if more remain, absent on the last
        var nextCursorJson = (batch < totalBatches)
                ? "\"cursor\": \"%d\"".formatted(to)
                : "";

        var mapping = get(urlPathEqualTo(RULES_PATH))
                .withQueryParam("limit", equalTo(String.valueOf(batchSize)));

        if (requestedCursor == null) {
            mapping = mapping.withQueryParam("cursor", absent());
        } else {
            mapping = mapping.withQueryParam("cursor", equalTo(requestedCursor));
        }

        wiremock().register(mapping.willReturn(okJson("""
                {
                  \"data\": [%s],
                  \"meta\": {
                    %s
                  }
                }
                """.formatted(String.join(",", rules), nextCursorJson))));
    }

    void verifyListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(RULES_PATH)));
    }

    void verifyInitialBatchRequested(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(RULES_PATH))
                .withoutQueryParam("cursor"));
    }

    void verifyResumedBatchRequested(int times, String cursor) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(RULES_PATH))
                .withQueryParam("cursor", equalTo(cursor)));
    }

    void verifyRulesCreated(int times) {
        wiremock().verifyThat(times, postRequestedFor(urlPathEqualTo(RULES_PATH)));
    }
}
