package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.client.WireMock;

/**
 * Serves a fake, paginated {@code /api/public/models} collection.
 */
abstract class ModelOperationsTestSupport extends PagedCollectionTestSupport {
    static final String MODELS_PATH = "/api/public/models";

    protected ModelOperationsTestSupport() {
        super(MODELS_PATH, ordinal -> """
                {
                  "id": "model-%1$d",
                  "modelName": "model-%1$d",
                  "isLangfuseManaged": false
                }""".formatted(ordinal));
    }

    void stubModels(int itemCount, int pageSize) {
        stubCollection(itemCount, pageSize);
    }

    /**
     * Serves the single-model lookup endpoint that backs {@code findById}, which resolves an id in one
     * request rather than walking the collection.
     */
    void stubModelFound(String id) {
        wiremock().register(
                get(urlPathEqualTo(MODELS_PATH + "/" + id))
                        .willReturn(okJson("""
                                {
                                  "id": "%1$s",
                                  "modelName": "%1$s",
                                  "isLangfuseManaged": false
                                }
                                """.formatted(id))));
    }

    /**
     * Makes the single-model lookup endpoint fail with the given status, so tests can prove that only
     * a 404 is read as absence on the direct-lookup path.
     */
    void stubModelFailure(String id, int status) {
        wiremock().register(
                get(urlPathEqualTo(MODELS_PATH + "/" + id))
                        .willReturn(aResponse()
                                .withStatus(status)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"message\":\"rejected\"}")));
    }

    void verifyModelGetRequests(int times, String id) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(MODELS_PATH + "/" + id)));
    }

    void verifyModelsCreated(int times) {
        verifyItemsCreated(times);
    }

    WireMock resetAndGetWiremock() {
        resetMappings();
        resetRequests();

        return wiremock();
    }
}
