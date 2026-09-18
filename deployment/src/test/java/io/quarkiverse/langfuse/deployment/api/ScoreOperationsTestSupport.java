package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.client.WireMock;

/**
 * Serves a fake, cursor-addressed {@code /api/public/v3/scores} collection.
 *
 * <p>
 * <strong>Three paths, not one.</strong> Scores are the only domain whose operations are split across
 * three Langfuse endpoints: the listing is {@code /api/public/v3/scores}, the create is
 * {@code /api/public/scores}, and the delete is {@code /api/public/scores/{scoreId}}. Only the first
 * is the collection {@link CursorCollectionTestSupport} stubs, so the other two are registered here
 * against their own paths - {@code verifyItemsCreated} on the base would look at the wrong one.
 *
 * <p>
 * The listing stubs registered by the base match on {@code cursor} and {@code limit} alone, and
 * WireMock matches query parameters as a subset - so a filtered walk is served by the very same stubs,
 * and the filter is asserted on the recorded requests instead. That is deliberate: it lets one test
 * prove both that the filtered view walks the cursors correctly and that the filter reached the wire.
 */
abstract class ScoreOperationsTestSupport extends CursorCollectionTestSupport {
    static final String SCORES_LIST_PATH = "/api/public/v3/scores";
    static final String SCORES_WRITE_PATH = "/api/public/scores";

    protected ScoreOperationsTestSupport() {
        super(SCORES_LIST_PATH, ordinal -> scoreJson("score-%d".formatted(ordinal)));
    }

    void stubScores(int scoreCount, int batchSize) {
        stubCollection(scoreCount, batchSize);
    }

    /**
     * Langfuse answers a score create with the assigned id alone rather than the stored score.
     */
    void stubScoreCreated(String scoreId) {
        wiremock().register(
                post(urlPathEqualTo(SCORES_WRITE_PATH))
                        .willReturn(okJson("{\"id\":\"%s\"}".formatted(scoreId))));
    }

    /**
     * Langfuse answers a score delete with a body, which the generated client discards because the
     * operation is typed {@code void} - stubbing one here proves that discarding is what happens.
     */
    void stubScoreDeleted(String scoreId) {
        wiremock().register(
                delete(urlPathEqualTo(SCORES_WRITE_PATH + "/" + scoreId))
                        .willReturn(okJson("{\"message\":\"deleted\"}")));
    }

    void stubScoreDeleteFailure(String scoreId, int status) {
        wiremock().register(
                delete(urlPathEqualTo(SCORES_WRITE_PATH + "/" + scoreId))
                        .willReturn(aResponse()
                                .withStatus(status)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"message\":\"rejected\"}")));
    }

    void verifyScoreDeleteRequests(int times, String scoreId) {
        wiremock().verifyThat(times, deleteRequestedFor(urlPathEqualTo(SCORES_WRITE_PATH + "/" + scoreId)));
    }

    void verifyScoresCreated(int times) {
        wiremock().verifyThat(times, postRequestedFor(urlPathEqualTo(SCORES_WRITE_PATH)));
    }

    /**
     * Asserts that exactly {@code times} listing requests carried every one of the eighteen filter
     * parameters with the values {@code ScoreOperationsTests#scoreFilter()} sets.
     *
     * <p>
     * The timestamps are matched as substrings rather than exactly, because the REST client renders an
     * {@code OffsetDateTime} through its own converter and a UTC offset may come out as either
     * {@code Z} or {@code +00:00}. Passing the local date-time portion still proves the filter reached
     * the wire carrying the right instant, without pinning the test to an offset rendering this layer
     * does not choose.
     */
    void verifyFullyFilteredListRequests(int times, String localFromTimestamp, String localToTimestamp) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(SCORES_LIST_PATH))
                .withQueryParam("fields", equalTo("details,subject"))
                .withQueryParam("id", equalTo("the-id"))
                .withQueryParam("name", equalTo("the-name"))
                .withQueryParam("source", equalTo("EVAL"))
                .withQueryParam("dataType", equalTo("NUMERIC"))
                .withQueryParam("environment", equalTo("the-environment"))
                .withQueryParam("configId", equalTo("the-config-id"))
                .withQueryParam("queueId", equalTo("the-queue-id"))
                .withQueryParam("authorUserId", equalTo("the-author-user-id"))
                .withQueryParam("value", equalTo("the-value"))
                .withQueryParam("valueMin", equalTo("1.5"))
                .withQueryParam("valueMax", equalTo("2.5"))
                .withQueryParam("traceId", equalTo("the-trace-id"))
                .withQueryParam("sessionId", equalTo("the-session-id"))
                .withQueryParam("observationId", equalTo("the-observation-id"))
                .withQueryParam("experimentId", equalTo("the-experiment-id"))
                .withQueryParam("fromTimestamp", containing(localFromTimestamp))
                .withQueryParam("toTimestamp", containing(localToTimestamp)));
    }

    /**
     * Asserts that exactly {@code times} listing requests carried no filter parameter at all, which is
     * what an unrestricted view must send.
     */
    void verifyUnfilteredListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(SCORES_LIST_PATH))
                .withQueryParam("fields", absent())
                .withQueryParam("id", absent())
                .withQueryParam("name", absent())
                .withQueryParam("source", absent())
                .withQueryParam("dataType", absent())
                .withQueryParam("traceId", absent())
                .withQueryParam("sessionId", absent())
                .withQueryParam("fromTimestamp", absent())
                .withQueryParam("toTimestamp", absent()));
    }

    /**
     * Asserts that exactly {@code times} listing requests carried the given {@code traceId}, which is
     * how the replace-rather-than-compose semantics of {@code matching} are checked.
     */
    void verifyTraceIdRequested(int times, String traceId) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(SCORES_LIST_PATH))
                .withQueryParam("traceId", equalTo(traceId)));
    }

    WireMock resetAndGetWiremock() {
        resetMappings();
        resetRequests();

        return wiremock();
    }

    /**
     * A {@code NUMERIC} score, which is the {@code ScoreV3} variant carrying a {@code Double} value.
     */
    private static String scoreJson(String scoreId) {
        return """
                {
                  "id": "%1$s",
                  "projectId": "project-1",
                  "name": "accuracy",
                  "source": "API",
                  "timestamp": "2024-01-01T00:00:00Z",
                  "environment": "default",
                  "createdAt": "2024-01-01T00:00:00Z",
                  "updatedAt": "2024-01-01T00:00:00Z",
                  "value": 0.75,
                  "dataType": "NUMERIC"
                }""".formatted(scoreId);
    }
}
