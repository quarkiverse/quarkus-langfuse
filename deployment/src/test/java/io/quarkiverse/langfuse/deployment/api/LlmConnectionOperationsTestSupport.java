package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.util.stream.IntStream;

import io.quarkiverse.langfuse.deployment.WiremockAware;

/**
 * Serves a fake {@code /api/public/llm-connections} collection.
 */
abstract class LlmConnectionOperationsTestSupport extends WiremockAware {
    static final String LLM_CONNECTIONS_PATH = "/api/public/llm-connections";

    void stubConnections(int itemCount, int pageSize) {
        var totalPages = Math.max(1, (itemCount + pageSize - 1) / pageSize);

        IntStream.rangeClosed(1, totalPages)
                .forEach(page -> stubPage(page, pageSize, itemCount, totalPages));
    }

    private void stubPage(int page, int pageSize, int itemCount, int totalPages) {
        var from = (page - 1) * pageSize;
        var to = Math.min(from + pageSize, itemCount);

        var connections = IntStream.range(from, to)
                .mapToObj(index -> """
                        {
                          "id": "connection-%1$d",
                          "provider": "provider-%1$d",
                          "adapter": "openai",
                          "displaySecretKey": "sk-...redacted"
                        }""".formatted(index + 1))
                .toList();

        wiremock().register(
                get(urlPathEqualTo(LLM_CONNECTIONS_PATH))
                        .withQueryParam("page", equalTo(String.valueOf(page)))
                        .withQueryParam("limit", equalTo(String.valueOf(pageSize)))
                        .willReturn(okJson("""
                                {
                                  "data": [%s],
                                  "meta": {
                                    "page": %d,
                                    "limit": %d,
                                    "totalItems": %d,
                                    "totalPages": %d
                                  }
                                }
                                """.formatted(String.join(",", connections), page, pageSize, itemCount, totalPages))));
    }

    void stubUpsert(String provider, String adapter) {
        wiremock().register(
                put(urlPathEqualTo(LLM_CONNECTIONS_PATH))
                        .willReturn(okJson("""
                                {
                                  "id": "connection-%s",
                                  "provider": "%s",
                                  "adapter": "%s",
                                  "displaySecretKey": "sk-...redacted"
                                }
                                """.formatted(provider, provider, adapter))));
    }

    void verifyListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(LLM_CONNECTIONS_PATH)));
    }

    void verifyPageRequested(int times, int page) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(LLM_CONNECTIONS_PATH))
                .withQueryParam("page", equalTo(String.valueOf(page))));
    }

    void verifyUpsertRequests(int times) {
        wiremock().verifyThat(times, putRequestedFor(urlPathEqualTo(LLM_CONNECTIONS_PATH)));
    }
}
