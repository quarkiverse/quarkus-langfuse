package io.quarkiverse.langfuse.deployment.api;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.langfuse.api.model.CodeEvaluatorVersion1;
import com.langfuse.api.model.EvaluatorVersion;

import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorResult;
import io.quarkiverse.langfuse.api.cursor.CursorSelection;
import io.quarkiverse.langfuse.client.LangfuseAuthenticationException;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;

class EvaluatorVersionOperationsTests extends EvaluatorVersionOperationsTestSupport {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideRuntimeConfigKey("quarkus.langfuse.public-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.secret-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.api.default-batch-size", "3")
            .overrideRuntimeConfigKey(LangfuseConfig.BASE_URL_KEY, wiremockUrlForConfig());

    @Inject
    LangfuseOperations langfuse;

    @BeforeEach
    void beforeEach() {
        resetAndGetWiremock();
    }

    // --- parent scoping --------------------------------------------------------------------

    /**
     * The point of the parent-scoped view: every batch of the walk is addressed to the evaluator the
     * view was opened on, and nothing leaks to any other evaluator.
     */
    @Test
    void everyBatchOfTheWalkIsAddressedToTheParentEvaluator() {
        stubVersions(7, 3);

        assertThat(langfuse.evaluators().versions(EVALUATOR_ID).findAll())
                .hasSize(7)
                .extracting(EvaluatorVersionOperationsTests::versionNumber)
                .containsExactly(1, 2, 3, 4, 5, 6, 7);

        verifyListRequests(3);
        verifyAnyVersionsListRequests(3);
        verifyOtherEvaluatorListRequests(0);
    }

    /**
     * Two views over different parents are independent: opening one on another evaluator does not
     * retarget the first, because each captures its own parent in its own fetcher.
     */
    @Test
    void viewsOverDifferentEvaluatorsDoNotShareTheirParent() {
        stubVersions(3, 3);

        var versions = langfuse.evaluators().versions(EVALUATOR_ID);
        langfuse.evaluators().versions(OTHER_EVALUATOR_ID);

        assertThat(versions.findAll()).hasSize(3);

        verifyListRequests(1);
        verifyOtherEvaluatorListRequests(0);
    }

    /**
     * The parent id is validated in the accessor, so an invalid evaluator fails at
     * {@code versions(...)} rather than later at the first traversal.
     */
    @Test
    void blankParentEvaluatorIdsAreRejectedBeforeAnyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.evaluators().versions("  "))
                .withMessageContaining("Evaluator id");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.evaluators().versions(""))
                .withMessageContaining("Evaluator id");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> langfuse.evaluators().versions(null))
                .withMessageContaining("Evaluator id");

        verifyAnyVersionsListRequests(0);
    }

    // --- traversal -------------------------------------------------------------------------

    @Test
    void findAllWalksEveryBatchViaCursors() {
        stubVersions(7, 3);

        assertThat(langfuse.evaluators().versions(EVALUATOR_ID).findAll()).hasSize(7);

        verifyListRequests(3);
        verifyInitialBatchRequested(1);
        verifyResumedBatchRequested(1, "3");
        verifyResumedBatchRequested(1, "6");
    }

    @Test
    void findAllUsesTheConfiguredDefaultBatchSize() {
        stubVersions(7, 3);

        assertThat(langfuse.evaluators().versions(EVALUATOR_ID).findAll()).hasSize(7);

        wiremock().verifyThat(3, getRequestedFor(urlPathEqualTo(VERSIONS_PATH))
                .withQueryParam("limit", equalTo("3")));
    }

    @Test
    void aSelectionOverridesTheConfiguredBatchSize() {
        stubVersions(7, 5);

        assertThat(langfuse.evaluators().versions(EVALUATOR_ID).find(CursorSelection.all(5))).hasSize(7);

        wiremock().verifyThat(2, getRequestedFor(urlPathEqualTo(VERSIONS_PATH))
                .withQueryParam("limit", equalTo("5")));
    }

    /**
     * A bounded selection over a newest-first history returns the newest versions, which is what makes
     * the documented ordering worth stating: a partial walk is not an arbitrary slice.
     */
    @Test
    void boundedCursorSelectionsStopAfterTheirLastBatchAndYieldTheNewestVersions() {
        stubVersions(20, 3);

        assertThat(langfuse.evaluators().versions(EVALUATOR_ID).find(CursorSelection.first(2, 3)))
                .extracting(EvaluatorVersionOperationsTests::versionNumber)
                .containsExactly(1, 2, 3, 4, 5, 6);

        verifyListRequests(2);
    }

    @Test
    void anEmptyCursorSelectionIssuesNoRequestAtAll() {
        stubVersions(20, 3);

        assertThat(langfuse.evaluators().versions(EVALUATOR_ID).find(CursorSelection.first(0, 3))).isEmpty();

        verifyAnyVersionsListRequests(0);
    }

    @Test
    void streamsAreLazyUntilConsumed() {
        stubVersions(7, 3);

        var stream = langfuse.evaluators().versions(EVALUATOR_ID).streamAll();

        verifyListRequests(0);

        assertThat(stream.limit(4)).hasSize(4);

        verifyListRequests(2);
    }

    @Test
    void findBatchExposesTheNextCursor() {
        stubVersions(7, 3);

        assertThat(langfuse.evaluators().versions(EVALUATOR_ID).findBatch(Cursor.first(3)))
                .satisfies(batch -> assertThat(batch.items()).hasSize(3))
                .satisfies(batch -> assertThat(batch.nextCursor()).contains(Cursor.at("3", 3)))
                .extracting(CursorResult::hasNext)
                .isEqualTo(true);
    }

    @Test
    void streamBatchesExposesEveryBatchIncludingTheLast() {
        stubVersions(7, 3);

        assertThat(langfuse.evaluators().versions(EVALUATOR_ID).streamBatches(CursorSelection.all(3)))
                .hasSize(3)
                .extracting(CursorResult::hasNext)
                .containsExactly(true, true, false);
    }

    @Test
    void anEmptyHistoryIsAnEmptyCollection() {
        stubVersions(0, 3);

        assertThat(langfuse.evaluators().versions(EVALUATOR_ID).findAll()).isEmpty();

        verifyListRequests(1);
    }

    // --- error policy ----------------------------------------------------------------------

    /**
     * Nothing here recovers an exception: this is a read-only traversal, not a lookup, so even a 404
     * must propagate rather than read as an empty history.
     */
    @Test
    void findAllPropagatesNotFound() {
        stubListingFailure(404);

        assertThatThrownBy(() -> langfuse.evaluators().versions(EVALUATOR_ID).findAll())
                .isInstanceOf(LangfuseNotFoundException.class);
    }

    @Test
    void rejectedCredentialsPropagate() {
        stubListingFailure(401);

        assertThatThrownBy(() -> langfuse.evaluators().versions(EVALUATOR_ID).findAll())
                .isInstanceOf(LangfuseAuthenticationException.class);
    }

    private static Integer versionNumber(EvaluatorVersion version) {
        return ((CodeEvaluatorVersion1) version.getActualInstance()).getVersion();
    }
}
