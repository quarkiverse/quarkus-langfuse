package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.util.stream.IntStream;

import io.quarkiverse.langfuse.deployment.WiremockAware;

/**
 * Serves a fake {@code /api/public/score-configs} collection.
 */
abstract class ScoreConfigOperationsTestSupport extends WiremockAware {
    static final String SCORE_CONFIGS_PATH = "/api/public/score-configs";

    void stubConfigs(int itemCount, int pageSize) {
        var totalPages = Math.max(1, (itemCount + pageSize - 1) / pageSize);

        IntStream.rangeClosed(1, totalPages)
                .forEach(page -> stubPage(page, pageSize, itemCount, totalPages));
    }

    private void stubPage(int page, int pageSize, int itemCount, int totalPages) {
        var from = (page - 1) * pageSize;
        var to = Math.min(from + pageSize, itemCount);

        var configs = IntStream.range(from, to)
                .mapToObj(index -> """
                        {
                          \"id\": \"config-%1$d\",
                          \"name\": \"config-%1$d\",\
                          \"dataType\": \"NUMERIC\",
                          \"projectId\": \"project-1\"
                        }""".formatted(index + 1))
                .toList();

        wiremock().register(
                get(urlPathEqualTo(SCORE_CONFIGS_PATH))
                        .withQueryParam("page", equalTo(String.valueOf(page)))
                        .withQueryParam("limit", equalTo(String.valueOf(pageSize)))
                        .willReturn(okJson("""
                                {
                                  \"data\": [%s],
                                  \"meta\": {
                                    \"page\": %d,
                                    \"limit\": %d,
                                    \"totalItems\": %d,
                                    \"totalPages\": %d
                                  }
                                }
                                """.formatted(String.join(",", configs), page, pageSize, itemCount, totalPages))));
    }

    void verifyListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(SCORE_CONFIGS_PATH)));
    }

    void verifyPageRequested(int times, int page) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(SCORE_CONFIGS_PATH))
                .withQueryParam("page", equalTo(String.valueOf(page))));
    }

    void verifyConfigsCreated(int times) {
        wiremock().verifyThat(times, postRequestedFor(urlPathEqualTo(SCORE_CONFIGS_PATH)));
    }
}
