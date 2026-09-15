package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.util.stream.IntStream;

import io.quarkiverse.langfuse.deployment.WiremockAware;

/**
 * Serves a fake {@code /api/public/v2/datasets} collection, including the single-dataset lookup
 * endpoint that lets {@code DatasetOperations} resolve a name in one request instead of scanning.
 */
abstract class DatasetOperationsTestSupport extends WiremockAware {
    static final String DATASETS_PATH = "/api/public/v2/datasets";

    void stubDatasetFound(String name) {
        wiremock().register(
                get(urlPathEqualTo(DATASETS_PATH + "/" + name))
                        .willReturn(okJson("""
                                {
                                  "id": "%1$s",
                                  "name": "%1$s",
                                  "projectId": "project-1"
                                }
                                """.formatted(name))));
    }

    void stubDatasetNotFound(String name) {
        wiremock().register(
                get(urlPathEqualTo(DATASETS_PATH + "/" + name))
                        .willReturn(aResponse()
                                .withStatus(404)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"message\":\"Dataset %s not found\"}".formatted(name))));
    }

    void stubDatasets(int itemCount, int pageSize) {
        var totalPages = Math.max(1, (itemCount + pageSize - 1) / pageSize);

        IntStream.rangeClosed(1, totalPages)
                .forEach(page -> stubPage(page, pageSize, itemCount, totalPages));
    }

    private void stubPage(int page, int pageSize, int itemCount, int totalPages) {
        var from = (page - 1) * pageSize;
        var to = Math.min(from + pageSize, itemCount);

        var datasets = IntStream.range(from, to)
                .mapToObj(index -> """
                        {
                          "id": "dataset-%1$d",
                          "name": "dataset-%1$d",
                          "projectId": "project-1"
                        }""".formatted(index + 1))
                .toList();

        wiremock().register(
                get(urlPathEqualTo(DATASETS_PATH))
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
                                """.formatted(String.join(",", datasets), page, pageSize, itemCount, totalPages))));
    }

    void verifyDatasetGetRequests(int times, String name) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(DATASETS_PATH + "/" + name)));
    }

    void verifyDatasetsCreated(int times) {
        wiremock().verifyThat(times, postRequestedFor(urlPathEqualTo(DATASETS_PATH)));
    }

    void verifyListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(DATASETS_PATH)));
    }

    void verifyPageRequested(int times, int page) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(DATASETS_PATH))
                .withQueryParam("page", equalTo(String.valueOf(page))));
    }
}
