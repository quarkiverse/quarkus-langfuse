package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.client.WireMock;

/**
 * Serves a fake, paginated {@code /api/public/v2/prompts} collection.
 *
 * <p>
 * The listing returns {@code PromptMeta} items - a name with its versions, labels and tags - while
 * the by-name lookup returns the polymorphic text-or-chat prompt, which is the shape the real API
 * uses and the reason this domain needs two different item JSONs.
 */
abstract class PromptOperationsTestSupport extends PagedCollectionTestSupport {
    static final String PROMPTS_PATH = "/api/public/v2/prompts";

    protected PromptOperationsTestSupport() {
        super(PROMPTS_PATH, ordinal -> """
                {
                  "name": "prompt-%1$d",
                  "type": "text",
                  "versions": [1],
                  "labels": ["production"],
                  "tags": [],
                  "lastUpdatedAt": "2024-01-01T00:00:00Z"
                }""".formatted(ordinal));
    }

    void stubPrompts(int itemCount, int pageSize) {
        stubCollection(itemCount, pageSize);
    }

    /**
     * Serves the single-prompt lookup endpoint that backs {@code findByName}, which resolves a name in
     * one request rather than walking the collection.
     */
    void stubPromptFound(String promptName) {
        wiremock().register(
                get(urlPathEqualTo(PROMPTS_PATH + "/" + promptName))
                        .willReturn(okJson(promptJson(promptName))));
    }

    /**
     * Makes the single-prompt lookup endpoint fail with the given status, so tests can prove that only
     * a 404 is read as absence on the direct-lookup path.
     */
    void stubPromptFailure(String promptName, int status) {
        wiremock().register(
                get(urlPathEqualTo(PROMPTS_PATH + "/" + promptName))
                        .willReturn(aResponse()
                                .withStatus(status)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"message\":\"rejected\"}")));
    }

    void stubPromptCreated(String promptName) {
        wiremock().register(
                post(urlPathEqualTo(PROMPTS_PATH))
                        .willReturn(okJson(promptJson(promptName))));
    }

    void stubPromptDeleted(String promptName) {
        wiremock().register(
                delete(urlPathEqualTo(PROMPTS_PATH + "/" + promptName))
                        .willReturn(aResponse().withStatus(200)));
    }

    void stubPromptDeleteFailure(String promptName, int status) {
        wiremock().register(
                delete(urlPathEqualTo(PROMPTS_PATH + "/" + promptName))
                        .willReturn(aResponse()
                                .withStatus(status)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"message\":\"rejected\"}")));
    }

    void verifyPromptGetRequests(int times, String promptName) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(PROMPTS_PATH + "/" + promptName)));
    }

    void verifyPromptDeleteRequests(int times, String promptName) {
        wiremock().verifyThat(times, deleteRequestedFor(urlPathEqualTo(PROMPTS_PATH + "/" + promptName)));
    }

    void verifyPromptsCreated(int times) {
        verifyItemsCreated(times);
    }

    WireMock resetAndGetWiremock() {
        resetMappings();
        resetRequests();

        return wiremock();
    }

    private static String promptJson(String promptName) {
        return """
                {
                  "name": "%1$s",
                  "type": "text",
                  "version": 1,
                  "prompt": "hello",
                  "labels": ["production"],
                  "tags": []
                }
                """.formatted(promptName);
    }
}
