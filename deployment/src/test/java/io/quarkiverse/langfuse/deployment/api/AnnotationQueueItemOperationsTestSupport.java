package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;

/**
 * Serves a fake, paginated {@code /api/public/annotation-queues/{queueId}/items} collection.
 *
 * <p>
 * The parent queue id is part of every stubbed path, which is what lets the tests prove that a view
 * opened on one queue never addresses another: a request carrying the wrong queue id simply matches
 * no stub, and the sibling-path verifications assert it was never issued.
 */
abstract class AnnotationQueueItemOperationsTestSupport extends PagedCollectionTestSupport {
    static final String QUEUE_ID = "queue-1";
    static final String OTHER_QUEUE_ID = "queue-2";
    static final String ITEMS_PATH = "/api/public/annotation-queues/" + QUEUE_ID + "/items";
    static final String OTHER_ITEMS_PATH = "/api/public/annotation-queues/" + OTHER_QUEUE_ID + "/items";

    protected AnnotationQueueItemOperationsTestSupport() {
        super(ITEMS_PATH, ordinal -> itemJson("item-%d".formatted(ordinal), "PENDING"));
    }

    static String itemJson(String id, String status) {
        return """
                {
                  "id": "%1$s",
                  "queueId": "%2$s",
                  "objectId": "trace-%1$s",
                  "objectType": "TRACE",
                  "status": "%3$s",
                  "createdAt": "2024-01-01T00:00:00Z",
                  "updatedAt": "2024-01-01T00:00:00Z"
                }""".formatted(id, QUEUE_ID, status);
    }

    void stubItems(int itemCount, int pageSize) {
        stubCollection(itemCount, pageSize);
    }

    /**
     * Serves the single-item lookup endpoint that backs {@code findById}, which resolves an id in one
     * request rather than walking the queue.
     */
    void stubItemFound(String itemId) {
        wiremock().register(
                get(urlPathEqualTo(ITEMS_PATH + "/" + itemId))
                        .willReturn(okJson(itemJson(itemId, "PENDING"))));
    }

    /**
     * Makes the single-item lookup endpoint fail with the given status, so tests can prove that only a
     * 404 is read as absence on the direct-lookup path.
     */
    void stubItemFailure(String itemId, int status) {
        wiremock().register(
                get(urlPathEqualTo(ITEMS_PATH + "/" + itemId))
                        .willReturn(rejection(status)));
    }

    void stubItemCreated(String itemId) {
        wiremock().register(
                post(urlPathEqualTo(ITEMS_PATH))
                        .willReturn(okJson(itemJson(itemId, "PENDING"))));
    }

    void stubItemUpdated(String itemId, String status) {
        wiremock().register(
                patch(urlPathEqualTo(ITEMS_PATH + "/" + itemId))
                        .willReturn(okJson(itemJson(itemId, status))));
    }

    void stubItemDeleted(String itemId) {
        wiremock().register(
                delete(urlPathEqualTo(ITEMS_PATH + "/" + itemId))
                        .willReturn(okJson("""
                                {
                                  "success": true,
                                  "message": "deleted"
                                }
                                """)));
    }

    void stubItemDeleteFailure(String itemId, int status) {
        wiremock().register(
                delete(urlPathEqualTo(ITEMS_PATH + "/" + itemId))
                        .willReturn(rejection(status)));
    }

    void verifyItemGetRequests(int times, String itemId) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(ITEMS_PATH + "/" + itemId)));
    }

    void verifyItemUpdateRequests(int times, String itemId) {
        wiremock().verifyThat(times, patchRequestedFor(urlPathEqualTo(ITEMS_PATH + "/" + itemId)));
    }

    void verifyItemDeleteRequests(int times, String itemId) {
        wiremock().verifyThat(times, deleteRequestedFor(urlPathEqualTo(ITEMS_PATH + "/" + itemId)));
    }

    /**
     * Asserts how many listing requests were addressed to a queue other than the one the view under
     * test was opened on - the direct check that the parent id really does scope every request.
     */
    void verifyOtherQueueListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(OTHER_ITEMS_PATH)));
    }

    /**
     * Asserts how many requests were addressed to any queue-items path at all, so a test can prove no
     * request escaped to a queue the assertions above do not name.
     */
    void verifyAnyItemsListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathMatching("/api/public/annotation-queues/[^/]+/items")));
    }

    private static ResponseDefinitionBuilder rejection(int status) {
        return aResponse()
                .withStatus(status)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\":\"rejected\"}");
    }

    WireMock resetAndGetWiremock() {
        resetMappings();
        resetRequests();

        return wiremock();
    }
}
