package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

/**
 * Serves a fake {@code /api/public/v2/observations} cursor-addressed collection.
 *
 * <p>
 * The listing stubs registered by {@link CursorCollectionTestSupport} match on {@code limit} and
 * {@code cursor} alone, and WireMock matches query parameters as a subset - so a filtered walk is
 * served by the very same stubs, and the filter is asserted on the recorded requests instead. That
 * is deliberate: it lets one test prove both that the filtered view walks correctly and that the
 * filter actually reached the wire.
 */
abstract class ObservationOperationsTestSupport extends CursorCollectionTestSupport {
    static final String OBSERVATIONS_PATH = "/api/public/v2/observations";

    protected ObservationOperationsTestSupport() {
        super(OBSERVATIONS_PATH, ordinal -> """
                {
                  "id": "observation-%1$d",
                  "traceId": "trace-1",
                  "projectId": "project-1",
                  "type": "GENERATION",
                  "startTime": "2024-01-01T00:00:00Z",
                  "name": "observation-%1$d",
                  "level": "DEFAULT",
                  "environment": "production"
                }""".formatted(ordinal));
    }

    void stubObservations(int itemCount, int batchSize) {
        stubCollection(itemCount, batchSize);
    }

    /**
     * Asserts that exactly {@code times} listing requests carried every filter parameter with the given
     * values.
     *
     * <p>
     * The two timestamps are matched as substrings rather than exactly, because the REST client renders
     * an {@code OffsetDateTime} through its own converter and a UTC offset may come out as either
     * {@code Z} or {@code +00:00}. Passing the local date-time portion still proves the criterion
     * reached the wire carrying the right instant, without pinning the test to an offset rendering this
     * layer does not choose.
     */
    void verifyFullyFilteredListRequests(int times, String localFromStartTime, String localToStartTime) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(OBSERVATIONS_PATH))
                .withQueryParam("fields", equalTo("core,basic"))
                .withQueryParam("expandMetadata", equalTo("key1,key2"))
                .withQueryParam("name", equalTo("the-name"))
                .withQueryParam("userId", equalTo("user-1"))
                .withQueryParam("sessionId", equalTo("session-1"))
                .withQueryParam("type", equalTo("GENERATION"))
                .withQueryParam("traceId", equalTo("trace-1"))
                .withQueryParam("level", equalTo("ERROR"))
                .withQueryParam("parentObservationId", equalTo("observation-parent"))
                .withQueryParam("isRootObservation", equalTo("false"))
                .withQueryParam("environment", equalTo("production"))
                .withQueryParam("fromStartTime", containing(localFromStartTime))
                .withQueryParam("toStartTime", containing(localToStartTime))
                .withQueryParam("version", equalTo("the-version"))
                .withQueryParam("filter", equalTo("the-filter")));
    }

    /**
     * Asserts that exactly {@code times} listing requests carried no filter parameter at all, which is
     * what an unrestricted view must send.
     */
    void verifyUnfilteredListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(OBSERVATIONS_PATH))
                .withQueryParam("fields", absent())
                .withQueryParam("expandMetadata", absent())
                .withQueryParam("name", absent())
                .withQueryParam("userId", absent())
                .withQueryParam("sessionId", absent())
                .withQueryParam("type", absent())
                .withQueryParam("traceId", absent())
                .withQueryParam("level", absent())
                .withQueryParam("parentObservationId", absent())
                .withQueryParam("isRootObservation", absent())
                .withQueryParam("environment", absent())
                .withQueryParam("fromStartTime", absent())
                .withQueryParam("toStartTime", absent())
                .withQueryParam("version", absent())
                .withQueryParam("filter", absent()));
    }

    /**
     * Asserts that exactly {@code times} listing requests carried the given {@code traceId}.
     */
    void verifyTraceIdRequested(int times, String traceId) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(OBSERVATIONS_PATH))
                .withQueryParam("traceId", equalTo(traceId)));
    }
}
