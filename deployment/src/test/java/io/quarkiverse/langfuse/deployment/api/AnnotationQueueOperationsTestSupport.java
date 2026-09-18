package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.client.WireMock;

/**
 * Serves a fake, paginated {@code /api/public/annotation-queues} collection.
 */
abstract class AnnotationQueueOperationsTestSupport extends PagedCollectionTestSupport {
    static final String QUEUES_PATH = "/api/public/annotation-queues";

    protected AnnotationQueueOperationsTestSupport() {
        super(QUEUES_PATH, ordinal -> """
                {
                  "id": "queue-%1$d",
                  "name": "queue-%1$d",
                  "description": "queue %1$d",
                  "scoreConfigIds": [],
                  "createdAt": "2024-01-01T00:00:00Z",
                  "updatedAt": "2024-01-01T00:00:00Z"
                }""".formatted(ordinal));
    }

    void stubQueues(int itemCount, int pageSize) {
        stubCollection(itemCount, pageSize);
    }

    /**
     * Serves the single-queue lookup endpoint that backs {@code findById}, which resolves an id in one
     * request rather than walking the collection.
     */
    void stubQueueFound(String id) {
        wiremock().register(
                get(urlPathEqualTo(QUEUES_PATH + "/" + id))
                        .willReturn(okJson("""
                                {
                                  "id": "%1$s",
                                  "name": "%1$s",
                                  "description": "a queue",
                                  "scoreConfigIds": [],
                                  "createdAt": "2024-01-01T00:00:00Z",
                                  "updatedAt": "2024-01-01T00:00:00Z"
                                }
                                """.formatted(id))));
    }

    /**
     * Makes the single-queue lookup endpoint fail with the given status, so tests can prove that only
     * a 404 is read as absence on the direct-lookup path.
     */
    void stubQueueFailure(String id, int status) {
        wiremock().register(
                get(urlPathEqualTo(QUEUES_PATH + "/" + id))
                        .willReturn(aResponse()
                                .withStatus(status)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"message\":\"rejected\"}")));
    }

    void verifyQueueGetRequests(int times, String id) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(QUEUES_PATH + "/" + id)));
    }

    void verifyQueuesCreated(int times) {
        verifyItemsCreated(times);
    }

    WireMock resetAndGetWiremock() {
        resetMappings();
        resetRequests();

        return wiremock();
    }
}
