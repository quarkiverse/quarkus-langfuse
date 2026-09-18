package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import com.github.tomakehurst.wiremock.client.WireMock;

/**
 * Serves a fake, cursor-addressed
 * {@code /api/public/v2/evaluators/{evaluatorId}/versions} collection.
 *
 * <p>
 * The parent evaluator id is part of every stubbed path, which is what lets the tests prove that a
 * view opened on one evaluator never addresses another: a request carrying the wrong evaluator id
 * simply matches no stub, and the sibling-path verifications assert it was never issued.
 */
abstract class EvaluatorVersionOperationsTestSupport extends CursorCollectionTestSupport {
    static final String EVALUATOR_ID = "evaluator-1";
    static final String OTHER_EVALUATOR_ID = "evaluator-2";
    static final String VERSIONS_PATH = "/api/public/v2/evaluators/" + EVALUATOR_ID + "/versions";
    static final String OTHER_VERSIONS_PATH = "/api/public/v2/evaluators/" + OTHER_EVALUATOR_ID + "/versions";

    protected EvaluatorVersionOperationsTestSupport() {
        super(VERSIONS_PATH, EvaluatorVersionOperationsTestSupport::versionJson);
    }

    /**
     * Renders one version. The ordinal doubles as the version number, so a test can assert the
     * newest-first ordering the endpoint documents by reading the versions back in order.
     */
    static String versionJson(int ordinal) {
        return """
                {
                  "id": "version-%1$d",
                  "version": %1$d,
                  "createdAt": "2024-01-01T00:00:00Z",
                  "createdBy": {
                    "id": "user-1",
                    "name": "Ada"
                  },
                  "type": "code",
                  "sourceCode": "return %1$d",
                  "sourceCodeLanguage": "PYTHON"
                }""".formatted(ordinal);
    }

    void stubVersions(int itemCount, int batchSize) {
        stubCollection(itemCount, batchSize);
    }

    /**
     * Asserts how many listing requests were addressed to an evaluator other than the one the view
     * under test was opened on - the direct check that the parent id really does scope every request.
     */
    void verifyOtherEvaluatorListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathEqualTo(OTHER_VERSIONS_PATH)));
    }

    /**
     * Asserts how many requests were addressed to any versions path at all, so a test can prove no
     * request escaped to an evaluator the assertions above do not name.
     */
    void verifyAnyVersionsListRequests(int times) {
        wiremock().verifyThat(times, getRequestedFor(urlPathMatching("/api/public/v2/evaluators/[^/]+/versions")));
    }

    WireMock resetAndGetWiremock() {
        resetMappings();
        resetRequests();

        return wiremock();
    }
}
