package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.util.stream.IntStream;

import com.github.tomakehurst.wiremock.client.WireMock;

import io.quarkiverse.langfuse.deployment.WiremockAware;

/**
 * Serves a fake, paginated {@code /api/public/models} collection so the higher-level model operations
 * can be exercised over the real REST client, exception mapper and Jackson stack.
 *
 * <p>
 * Stubs are registered per page, which is what lets tests assert not only the items returned but also
 * exactly which pages were requested - the only honest way to verify laziness and short-circuiting.
 */
abstract class ModelOperationsTestSupport extends WiremockAware {
    static final String MODELS_PATH = "/api/public/models";

    /**
     * Registers {@code itemCount} models spread over pages of {@code pageSize}, each page stubbed
     * against its own {@code page}/{@code limit} query parameters.
     */
    void stubModels(int itemCount, int pageSize) {
        var totalPages = Math.max(1, (itemCount + pageSize - 1) / pageSize);

        IntStream.rangeClosed(1, totalPages)
                .forEach(page -> stubPage(page, pageSize, itemCount, totalPages));
    }

    private void stubPage(int page, int pageSize, int itemCount, int totalPages) {
        var from = (page - 1) * pageSize;
        var to = Math.min(from + pageSize, itemCount);

        var models = IntStream.range(from, to)
                .mapToObj(index -> """
                        {
                          "id": "model-%1$d",
                          "modelName": "model-%1$d",
                          "isLangfuseManaged": false
                        }""".formatted(index + 1))
                .toList();

        wiremock().register(
                get(urlPathEqualTo(MODELS_PATH))
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
                                """.formatted(String.join(",", models), page, pageSize, itemCount, totalPages))));
    }

    void verifyPageRequested(int times, int page) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(MODELS_PATH))
                .withQueryParam("page", equalTo(String.valueOf(page))));
    }

    void verifyListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(MODELS_PATH)));
    }

    void verifyModelsCreated(int times) {
        wiremock().verifyThat(times, postRequestedFor(urlPathEqualTo(MODELS_PATH)));
    }

    WireMock resetAndGetWiremock() {
        resetMappings();
        resetRequests();

        return wiremock();
    }
}
