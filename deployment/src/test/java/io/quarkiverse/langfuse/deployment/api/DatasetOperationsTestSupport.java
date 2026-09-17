package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/**
 * Serves a fake {@code /api/public/v2/datasets} collection, including the single-dataset lookup
 * endpoint that lets {@code DatasetOperations} resolve a name in one request instead of scanning.
 */
abstract class DatasetOperationsTestSupport extends PagedCollectionTestSupport {
    static final String DATASETS_PATH = "/api/public/v2/datasets";

    protected DatasetOperationsTestSupport() {
        super(DATASETS_PATH, ordinal -> """
                {
                  "id": "dataset-%1$d",
                  "name": "dataset-%1$d",
                  "projectId": "project-1"
                }""".formatted(ordinal));
    }

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

    /**
     * Makes the single-dataset lookup endpoint fail with the given status, so tests can prove that
     * only a 404 is read as absence on the direct-lookup path.
     */
    void stubDatasetFailure(String name, int status) {
        wiremock().register(
                get(urlPathEqualTo(DATASETS_PATH + "/" + name))
                        .willReturn(aResponse()
                                .withStatus(status)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"message\":\"rejected\"}")));
    }

    void stubDatasets(int itemCount, int pageSize) {
        stubCollection(itemCount, pageSize);
    }

    void verifyDatasetGetRequests(int times, String name) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(DATASETS_PATH + "/" + name)));
    }

    void verifyDatasetsCreated(int times) {
        verifyItemsCreated(times);
    }
}
