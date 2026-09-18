package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/**
 * Serves a fake {@code /api/public/experiment-items} cursor-addressed collection.
 *
 * <p>
 * Served from its own top-level path rather than from under an experiment, which is the whole point
 * of the domain being modelled as a top-level collection: the parent experiment is a query criterion
 * asserted on the recorded requests, not a path segment.
 *
 * <p>
 * The listing stubs registered by {@link CursorCollectionTestSupport} match on {@code limit} and
 * {@code cursor} alone, and WireMock matches query parameters as a subset - so a filtered walk is
 * served by the very same stubs, and the filter is asserted on the recorded requests instead.
 */
abstract class ExperimentItemOperationsTestSupport extends CursorCollectionTestSupport {
    static final String EXPERIMENT_ITEMS_PATH = "/api/public/experiment-items";

    protected ExperimentItemOperationsTestSupport() {
        super(EXPERIMENT_ITEMS_PATH, ordinal -> """
                {
                  "id": "experiment-item-%1$d",
                  "traceId": "trace-%1$d",
                  "startTime": "2024-01-01T00:00:00Z",
                  "level": "DEFAULT",
                  "environment": "production",
                  "experimentId": "experiment-1",
                  "experimentName": "experiment-1",
                  "experimentItemId": "dataset-item-%1$d"
                }""".formatted(ordinal));
    }

    void stubExperimentItems(int itemCount, int batchSize) {
        stubCollection(itemCount, batchSize);
    }

    /**
     * Asserts that exactly {@code times} listing requests carried every filter parameter with the given
     * values. The time bounds are not among them - they come from the time window rather than from the
     * filter, and are asserted by {@link #verifyLowerBoundOnEveryListRequest}.
     */
    void verifyFullyFilteredListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(EXPERIMENT_ITEMS_PATH))
                .withQueryParam("fields", equalTo("core,dataset,experimentMetadata,io,itemMetadata,metadata,scores"))
                .withQueryParam("scoreLimit", equalTo("7"))
                .withQueryParam("experimentId", equalTo("the-experiment-id"))
                .withQueryParam("experimentName", equalTo("the-experiment-name"))
                .withQueryParam("experimentItemId", equalTo("the-experiment-item-id"))
                .withQueryParam("datasetId", equalTo("the-dataset-id"))
                .withQueryParam("filter", equalTo("the-filter")));
    }

    /**
     * Asserts that exactly {@code times} listing requests carried no filter parameter at all, which is
     * what a view restricted by nothing but its time window must send.
     */
    void verifyUnfilteredListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(EXPERIMENT_ITEMS_PATH))
                .withQueryParam("fields", absent())
                .withQueryParam("scoreLimit", absent())
                .withQueryParam("experimentId", absent())
                .withQueryParam("experimentName", absent())
                .withQueryParam("experimentItemId", absent())
                .withQueryParam("datasetId", absent())
                .withQueryParam("filter", absent()));
    }

    /**
     * Asserts that the mandatory lower bound reached the wire on <strong>every</strong> one of the
     * {@code times} listing requests, and that no other listing request was made. WireMock serves a
     * stub whether or not a required parameter is present, so this explicit count-matched assertion is
     * the only thing standing between a dropped bound and a green test.
     *
     * <p>
     * The timestamp is matched as a substring rather than exactly, because the REST client renders an
     * {@code OffsetDateTime} through its own converter and a UTC offset may come out as either
     * {@code Z} or {@code +00:00}.
     */
    void verifyLowerBoundOnEveryListRequest(int times, String localFromStartTime) {
        verifyListRequests(times);

        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(EXPERIMENT_ITEMS_PATH))
                .withQueryParam("fromStartTime", containing(localFromStartTime)));
    }

    /**
     * Asserts that exactly {@code times} listing requests carried the given upper bound.
     */
    void verifyUpperBoundRequested(int times, String localToStartTime) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(EXPERIMENT_ITEMS_PATH))
                .withQueryParam("toStartTime", containing(localToStartTime)));
    }

    /**
     * Asserts that exactly {@code times} listing requests carried no upper bound, which is what a
     * window opened with {@code since} must send.
     */
    void verifyNoUpperBoundRequested(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(EXPERIMENT_ITEMS_PATH))
                .withQueryParam("toStartTime", absent()));
    }

    /**
     * Asserts that exactly {@code times} listing requests carried the given {@code experimentName}.
     */
    void verifyExperimentNameRequested(int times, String experimentName) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(EXPERIMENT_ITEMS_PATH))
                .withQueryParam("experimentName", equalTo(experimentName)));
    }
}
