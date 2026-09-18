package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.client.WireMock;

/**
 * Serves a fake, paginated {@code /api/public/comments} collection.
 *
 * <p>
 * The listing stubs registered by {@link PagedCollectionTestSupport} match on {@code page} and
 * {@code limit} alone, and WireMock matches query parameters as a subset - so a filtered walk is
 * served by the very same stubs, and the filter is asserted on the recorded requests instead. That
 * is deliberate: it lets one test prove both that the filtered view pages correctly and that the
 * filter actually reached the wire.
 */
abstract class CommentOperationsTestSupport extends PagedCollectionTestSupport {
    static final String COMMENTS_PATH = "/api/public/comments";

    protected CommentOperationsTestSupport() {
        super(COMMENTS_PATH, ordinal -> commentJson("comment-%d".formatted(ordinal)));
    }

    void stubComments(int itemCount, int pageSize) {
        stubCollection(itemCount, pageSize);
    }

    /**
     * Serves the single-comment lookup endpoint that backs {@code findById}, which resolves an id in
     * one request rather than walking the collection.
     */
    void stubCommentFound(String commentId) {
        wiremock().register(
                get(urlPathEqualTo(COMMENTS_PATH + "/" + commentId))
                        .willReturn(okJson(commentJson(commentId))));
    }

    /**
     * Makes the single-comment lookup endpoint fail with the given status, so tests can prove that only
     * a 404 is read as absence on the direct-lookup path.
     */
    void stubCommentFailure(String commentId, int status) {
        wiremock().register(
                get(urlPathEqualTo(COMMENTS_PATH + "/" + commentId))
                        .willReturn(aResponse()
                                .withStatus(status)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"message\":\"rejected\"}")));
    }

    void stubCommentCreated(String commentId) {
        wiremock().register(
                post(urlPathEqualTo(COMMENTS_PATH))
                        .willReturn(okJson("{\"id\":\"%s\"}".formatted(commentId))));
    }

    void verifyCommentGetRequests(int times, String commentId) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(COMMENTS_PATH + "/" + commentId)));
    }

    void verifyCommentsCreated(int times) {
        verifyItemsCreated(times);
    }

    /**
     * Asserts that exactly {@code times} listing requests carried the three filter parameters with the
     * given values.
     */
    void verifyFilteredListRequests(int times, String objectType, String objectId, String authorUserId) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(COMMENTS_PATH))
                .withQueryParam("objectType", equalTo(objectType))
                .withQueryParam("objectId", equalTo(objectId))
                .withQueryParam("authorUserId", equalTo(authorUserId)));
    }

    /**
     * Asserts that exactly {@code times} listing requests carried no filter parameter at all, which is
     * what an unrestricted view must send.
     */
    void verifyUnfilteredListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(COMMENTS_PATH))
                .withQueryParam("objectType", absent())
                .withQueryParam("objectId", absent())
                .withQueryParam("authorUserId", absent()));
    }

    /**
     * Asserts that exactly {@code times} listing requests carried the given {@code objectType}.
     */
    void verifyObjectTypeRequested(int times, String objectType) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(COMMENTS_PATH))
                .withQueryParam("objectType", equalTo(objectType)));
    }

    WireMock resetAndGetWiremock() {
        resetMappings();
        resetRequests();

        return wiremock();
    }

    private static String commentJson(String commentId) {
        return """
                {
                  "id": "%1$s",
                  "projectId": "project-1",
                  "createdAt": "2024-01-01T00:00:00Z",
                  "updatedAt": "2024-01-01T00:00:00Z",
                  "objectType": "TRACE",
                  "objectId": "trace-1",
                  "content": "looks good",
                  "authorUserId": "user-1"
                }""".formatted(commentId);
    }
}
