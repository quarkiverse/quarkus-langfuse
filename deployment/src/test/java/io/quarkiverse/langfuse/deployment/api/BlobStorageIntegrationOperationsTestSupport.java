package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.util.stream.IntStream;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;

import io.quarkiverse.langfuse.deployment.WiremockAware;

/**
 * Serves a fake {@code /api/public/integrations/blob-storage} collection.
 *
 * <p>
 * This support class deliberately extends neither {@link PagedCollectionTestSupport} nor
 * {@link CursorCollectionTestSupport}: the endpoint is unpaginated, so there are no batches to stub
 * against a page index or a cursor and the whole collection is served from one stub. That is the
 * property the tests here exist to pin down.
 */
abstract class BlobStorageIntegrationOperationsTestSupport extends WiremockAware {
    static final String INTEGRATIONS_PATH = "/api/public/integrations/blob-storage";

    /**
     * Renders one integration. The credentials-adjacent fields are present precisely so that a test
     * reading them back proves they round-trip without this layer touching them.
     */
    static String integrationJson(int ordinal) {
        return """
                {
                  "id": "integration-%1$d",
                  "projectId": "project-%1$d",
                  "type": "S3",
                  "bucketName": "bucket-%1$d",
                  "endpoint": "https://s3.example.test",
                  "region": "eu-west-1",
                  "accessKeyId": "AKIA%1$d",
                  "prefix": "langfuse/",
                  "exportFrequency": "daily",
                  "enabled": true,
                  "forcePathStyle": false,
                  "fileType": "JSONL",
                  "exportMode": "FULL_HISTORY",
                  "compressed": false,
                  "exportSource": "LEGACY_TRACES_OBSERVATIONS",
                  "createdAt": "2024-01-01T00:00:00Z",
                  "updatedAt": "2024-01-02T00:00:00Z"
                }""".formatted(ordinal);
    }

    /**
     * Stubs the single listing request with {@code itemCount} integrations - the whole collection, in
     * one response, with no pagination metadata of any kind.
     */
    void stubIntegrations(int itemCount) {
        var items = IntStream.rangeClosed(1, itemCount)
                .mapToObj(BlobStorageIntegrationOperationsTestSupport::integrationJson)
                .toList();

        wiremock().register(get(urlPathEqualTo(INTEGRATIONS_PATH))
                .willReturn(okJson("""
                        {
                          "data": [%s]
                        }
                        """.formatted(String.join(",", items)))));
    }

    void stubListingFailure(int status) {
        wiremock().register(get(urlPathEqualTo(INTEGRATIONS_PATH))
                .willReturn(rejection(status)));
    }

    void stubStatusFound(String id) {
        wiremock().register(get(urlPathEqualTo(INTEGRATIONS_PATH + "/" + id))
                .willReturn(okJson("""
                        {
                          "id": "%s",
                          "projectId": "project-1",
                          "syncStatus": "up_to_date",
                          "enabled": true
                        }
                        """.formatted(id))));
    }

    void stubStatusFailure(String id, int status) {
        wiremock().register(get(urlPathEqualTo(INTEGRATIONS_PATH + "/" + id))
                .willReturn(rejection(status)));
    }

    void stubUpsert(int ordinal) {
        wiremock().register(put(urlPathEqualTo(INTEGRATIONS_PATH))
                .willReturn(okJson(integrationJson(ordinal))));
    }

    void stubIntegrationDeleted(String id) {
        wiremock().register(delete(urlPathEqualTo(INTEGRATIONS_PATH + "/" + id))
                .willReturn(okJson("""
                        {
                          "message": "deleted"
                        }
                        """)));
    }

    void stubIntegrationDeleteFailure(String id, int status) {
        wiremock().register(delete(urlPathEqualTo(INTEGRATIONS_PATH + "/" + id))
                .willReturn(rejection(status)));
    }

    void verifyListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(INTEGRATIONS_PATH)));
    }

    /**
     * Asserts that the listing request carried no {@code page}, {@code limit} or {@code cursor}
     * parameter - there is no pagination to drive, so sending one would be meaningless.
     */
    void verifyListRequestWithoutPaginationParameters() {
        wiremock().verifyThat(1, getRequestedFor(urlPathEqualTo(INTEGRATIONS_PATH))
                .withoutQueryParam("page")
                .withoutQueryParam("limit")
                .withoutQueryParam("cursor"));
    }

    void verifyStatusRequests(int times, String id) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(INTEGRATIONS_PATH + "/" + id)));
    }

    void verifyUpsertRequests(int times) {
        wiremock().verifyThat(times, putRequestedFor(urlPathEqualTo(INTEGRATIONS_PATH)));
    }

    void verifyDeleteRequests(int times, String id) {
        wiremock().verifyThat(times, deleteRequestedFor(urlPathEqualTo(INTEGRATIONS_PATH + "/" + id)));
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
