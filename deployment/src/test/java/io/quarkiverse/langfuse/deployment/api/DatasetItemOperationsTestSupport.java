package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.client.WireMock;

/**
 * Serves a fake, paginated {@code /api/public/dataset-items} collection.
 *
 * <p>
 * The listing stubs registered by {@link PagedCollectionTestSupport} match on {@code page} and
 * {@code limit} alone, and WireMock matches query parameters as a subset - so a filtered walk is
 * served by the very same stubs, and the filter is asserted on the recorded requests instead. That
 * is deliberate: it lets one test prove both that the filtered view pages correctly and that the
 * filter actually reached the wire.
 */
abstract class DatasetItemOperationsTestSupport extends PagedCollectionTestSupport {
    static final String DATASET_ITEMS_PATH = "/api/public/dataset-items";

    protected DatasetItemOperationsTestSupport() {
        super(DATASET_ITEMS_PATH, ordinal -> datasetItemJson("item-%d".formatted(ordinal)));
    }

    void stubDatasetItems(int itemCount, int pageSize) {
        stubCollection(itemCount, pageSize);
    }

    /**
     * Serves the single-item lookup endpoint that backs {@code findById}, which resolves an id in one
     * request rather than walking the collection.
     */
    void stubDatasetItemFound(String itemId) {
        wiremock().register(
                get(urlPathEqualTo(DATASET_ITEMS_PATH + "/" + itemId))
                        .willReturn(okJson(datasetItemJson(itemId))));
    }

    /**
     * Makes the single-item lookup endpoint fail with the given status, so tests can prove that only a
     * 404 is read as absence on the direct-lookup path.
     */
    void stubDatasetItemFailure(String itemId, int status) {
        wiremock().register(
                get(urlPathEqualTo(DATASET_ITEMS_PATH + "/" + itemId))
                        .willReturn(aResponse()
                                .withStatus(status)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"message\":\"rejected\"}")));
    }

    void stubDatasetItemCreated(String itemId) {
        wiremock().register(
                post(urlPathEqualTo(DATASET_ITEMS_PATH))
                        .willReturn(okJson(datasetItemJson(itemId))));
    }

    /**
     * Langfuse answers a dataset-item delete with a body rather than a 204, which is why this stub
     * returns one.
     */
    void stubDatasetItemDeleted(String itemId) {
        wiremock().register(
                delete(urlPathEqualTo(DATASET_ITEMS_PATH + "/" + itemId))
                        .willReturn(okJson("{\"message\":\"deleted\"}")));
    }

    void stubDatasetItemDeleteFailure(String itemId, int status) {
        wiremock().register(
                delete(urlPathEqualTo(DATASET_ITEMS_PATH + "/" + itemId))
                        .willReturn(aResponse()
                                .withStatus(status)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"message\":\"rejected\"}")));
    }

    void verifyDatasetItemGetRequests(int times, String itemId) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(DATASET_ITEMS_PATH + "/" + itemId)));
    }

    void verifyDatasetItemDeleteRequests(int times, String itemId) {
        wiremock().verifyThat(times, deleteRequestedFor(urlPathEqualTo(DATASET_ITEMS_PATH + "/" + itemId)));
    }

    void verifyDatasetItemsCreated(int times) {
        verifyItemsCreated(times);
    }

    /**
     * Asserts that exactly {@code times} listing requests carried the four filter parameters with the
     * given values.
     *
     * <p>
     * {@code version} is matched as a substring rather than exactly, because the REST client renders an
     * {@code OffsetDateTime} through its own converter and a UTC offset may come out as either
     * {@code Z} or {@code +00:00}. Passing the local date-time portion still proves the filter reached
     * the wire carrying the right instant, without pinning the test to an offset rendering this layer
     * does not choose.
     */
    void verifyFilteredListRequests(int times, String datasetName, String sourceTraceId, String sourceObservationId,
            String localVersion) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(DATASET_ITEMS_PATH))
                .withQueryParam("datasetName", equalTo(datasetName))
                .withQueryParam("sourceTraceId", equalTo(sourceTraceId))
                .withQueryParam("sourceObservationId", equalTo(sourceObservationId))
                .withQueryParam("version", containing(localVersion)));
    }

    /**
     * Asserts that exactly {@code times} listing requests carried no filter parameter at all, which is
     * what an unrestricted view must send.
     */
    void verifyUnfilteredListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(DATASET_ITEMS_PATH))
                .withQueryParam("datasetName", absent())
                .withQueryParam("sourceTraceId", absent())
                .withQueryParam("sourceObservationId", absent())
                .withQueryParam("version", absent()));
    }

    /**
     * Asserts that exactly {@code times} listing requests carried the given {@code datasetName}.
     */
    void verifyDatasetNameRequested(int times, String datasetName) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(DATASET_ITEMS_PATH))
                .withQueryParam("datasetName", equalTo(datasetName)));
    }

    WireMock resetAndGetWiremock() {
        resetMappings();
        resetRequests();

        return wiremock();
    }

    private static String datasetItemJson(String itemId) {
        return """
                {
                  "id": "%1$s",
                  "status": "ACTIVE",
                  "input": {"question": "why"},
                  "expectedOutput": {"answer": "because"},
                  "sourceTraceId": "trace-1",
                  "sourceObservationId": "observation-1",
                  "datasetId": "dataset-1",
                  "datasetName": "my-dataset",
                  "createdAt": "2024-01-01T00:00:00Z",
                  "updatedAt": "2024-01-01T00:00:00Z"
                }""".formatted(itemId);
    }
}
