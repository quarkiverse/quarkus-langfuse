package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/**
 * Serves a fake {@code /api/public/experiments} cursor-addressed collection.
 *
 * <p>
 * The listing stubs registered by {@link CursorCollectionTestSupport} match on {@code limit} and
 * {@code cursor} alone, and WireMock matches query parameters as a subset - so a filtered walk is
 * served by the very same stubs, and the filter is asserted on the recorded requests instead. That
 * is deliberate: it lets one test prove both that the filtered view walks correctly and that the
 * filter actually reached the wire.
 */
abstract class ExperimentOperationsTestSupport extends CursorCollectionTestSupport {
    static final String EXPERIMENTS_PATH = "/api/public/experiments";

    protected ExperimentOperationsTestSupport() {
        super(EXPERIMENTS_PATH, ordinal -> """
                {
                  "id": "experiment-%1$d",
                  "name": "experiment-%1$d",
                  "description": "the description",
                  "startTime": "2024-01-01T00:00:00Z",
                  "endTime": "2024-01-01T01:00:00Z",
                  "itemCount": 3,
                  "datasetId": "dataset-1"
                }""".formatted(ordinal));
    }

    void stubExperiments(int itemCount, int batchSize) {
        stubCollection(itemCount, batchSize);
    }

    /**
     * Asserts that exactly {@code times} listing requests carried every filter parameter with the given
     * values. The time bounds are not among them - they come from the time window rather than from the
     * filter, and are asserted by {@link #verifyLowerBoundOnEveryListRequest}.
     */
    void verifyFullyFilteredListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(EXPERIMENTS_PATH))
                .withQueryParam("fields", equalTo("core,metadata,scores"))
                .withQueryParam("scoreLimit", equalTo("7"))
                .withQueryParam("id", equalTo("the-id"))
                .withQueryParam("name", equalTo("the-name"))
                .withQueryParam("datasetId", equalTo("the-dataset-id"))
                .withQueryParam("filter", equalTo("the-filter")));
    }

    /**
     * Asserts that exactly {@code times} listing requests carried no filter parameter at all, which is
     * what a view restricted by nothing but its time window must send.
     */
    void verifyUnfilteredListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(EXPERIMENTS_PATH))
                .withQueryParam("fields", absent())
                .withQueryParam("scoreLimit", absent())
                .withQueryParam("id", absent())
                .withQueryParam("name", absent())
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
     * {@code Z} or {@code +00:00}. Passing the local date-time portion still proves the bound reached
     * the wire carrying the right instant, without pinning the test to an offset rendering this layer
     * does not choose.
     */
    void verifyLowerBoundOnEveryListRequest(int times, String localFromStartTime) {
        verifyListRequests(times);

        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(EXPERIMENTS_PATH))
                .withQueryParam("fromStartTime", containing(localFromStartTime)));
    }

    /**
     * Asserts that exactly {@code times} listing requests carried the given upper bound.
     */
    void verifyUpperBoundRequested(int times, String localToStartTime) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(EXPERIMENTS_PATH))
                .withQueryParam("toStartTime", containing(localToStartTime)));
    }

    /**
     * Asserts that exactly {@code times} listing requests carried no upper bound, which is what a
     * window opened with {@code since} must send.
     */
    void verifyNoUpperBoundRequested(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(EXPERIMENTS_PATH))
                .withQueryParam("toStartTime", absent()));
    }

    /**
     * Asserts that exactly {@code times} listing requests carried the given {@code name}.
     */
    void verifyNameRequested(int times, String name) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(EXPERIMENTS_PATH))
                .withQueryParam("name", equalTo(name)));
    }
}
