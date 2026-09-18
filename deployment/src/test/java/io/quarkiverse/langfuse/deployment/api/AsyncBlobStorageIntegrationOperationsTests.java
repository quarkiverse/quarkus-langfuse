package io.quarkiverse.langfuse.deployment.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.langfuse.api.model.BlobStorageExportFrequency;
import com.langfuse.api.model.BlobStorageIntegrationFileType;
import com.langfuse.api.model.BlobStorageIntegrationResponse;
import com.langfuse.api.model.BlobStorageIntegrationStatusResponse;
import com.langfuse.api.model.BlobStorageIntegrationType;
import com.langfuse.api.model.BlobStorageSyncStatus;
import com.langfuse.api.model.CreateBlobStorageIntegrationRequest;

import io.quarkiverse.langfuse.api.AsyncLangfuseOperations;
import io.quarkiverse.langfuse.api.deletion.DeletionOutcome;
import io.quarkiverse.langfuse.client.LangfuseAuthenticationException;
import io.quarkiverse.langfuse.client.LangfuseAuthorizationException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;
import io.smallrye.mutiny.Uni;

class AsyncBlobStorageIntegrationOperationsTests extends BlobStorageIntegrationOperationsTestSupport {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideRuntimeConfigKey("quarkus.langfuse.public-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.secret-key", "quarkus")
            .overrideRuntimeConfigKey(LangfuseConfig.BASE_URL_KEY, wiremockUrlForConfig());

    @Inject
    AsyncLangfuseOperations asyncLangfuse;

    @BeforeEach
    void beforeEach() {
        resetAndGetWiremock();
    }

    // --- traversal -------------------------------------------------------------------------

    /**
     * The defining property of this domain: the collection is unpaginated, so the whole thing arrives
     * in exactly one request no matter how many integrations there are.
     */
    @Test
    void findAllIssuesExactlyOneRequestForTheWholeCollection() {
        stubIntegrations(5);

        assertThat(await(asyncLangfuse.blobStorageIntegrations().findAll()))
                .hasSize(5)
                .extracting(BlobStorageIntegrationResponse::getId)
                .containsExactly("integration-1", "integration-2", "integration-3", "integration-4", "integration-5");

        verifyListRequests(1);
    }

    @Test
    void findAllIsEmptyWhenNothingIsConfigured() {
        stubIntegrations(0);

        assertThat(await(asyncLangfuse.blobStorageIntegrations().findAll())).isEmpty();

        verifyListRequests(1);
    }

    /**
     * There is no pagination metadata to read, so a limit or cursor query parameter would be
     * meaningless - and none is sent.
     */
    @Test
    void findAllSendsNoPaginationParameters() {
        stubIntegrations(3);

        assertThat(await(asyncLangfuse.blobStorageIntegrations().findAll())).hasSize(3);

        verifyListRequestWithoutPaginationParameters();
    }

    /**
     * The listing is deferred: nothing is requested until the {@link Uni} is subscribed to.
     */
    @Test
    void findAllIssuesNothingUntilSubscribed() {
        stubIntegrations(3);

        var uni = asyncLangfuse.blobStorageIntegrations().findAll();

        verifyListRequests(0);

        assertThat(await(uni)).hasSize(3);

        verifyListRequests(1);
    }

    @Test
    void findAllPropagatesFailures() {
        stubListingFailure(401);

        assertThatThrownBy(() -> await(asyncLangfuse.blobStorageIntegrations().findAll()))
                .isInstanceOf(LangfuseAuthenticationException.class);
    }

    // --- status lookup ---------------------------------------------------------------------

    /**
     * Named {@code findStatusById} rather than {@code findById} because the endpoint returns the
     * export sync status, not the integration: the name has to say what comes back.
     */
    @Test
    void findStatusByIdResolvesInASingleRequest() {
        stubIntegrations(3);
        stubStatusFound("integration-2");

        assertThat(await(asyncLangfuse.blobStorageIntegrations().findStatusById("integration-2")))
                .extracting(BlobStorageIntegrationStatusResponse::getId,
                        BlobStorageIntegrationStatusResponse::getSyncStatus)
                .containsExactly("integration-2", BlobStorageSyncStatus.UP_TO_DATE);

        verifyStatusRequests(1, "integration-2");
        verifyListRequests(0);
    }

    @Test
    void findStatusByIdEmitsNullWhenAbsent() {
        stubStatusFailure("nope", 404);

        assertThat(await(asyncLangfuse.blobStorageIntegrations().findStatusById("nope"))).isNull();
    }

    /**
     * Only a 404 is recovered: a rejected credential must fail the {@link Uni} rather than read as
     * absence.
     */
    @Test
    void findStatusByIdNeverMistakesARejectedCredentialForAbsence() {
        stubStatusFailure("integration-1", 401);

        assertThatThrownBy(() -> await(asyncLangfuse.blobStorageIntegrations().findStatusById("integration-1")))
                .isInstanceOf(LangfuseAuthenticationException.class);
    }

    @Test
    void findStatusByIdNeverMistakesAForbiddenResponseForAbsence() {
        stubStatusFailure("integration-1", 403);

        assertThatThrownBy(() -> await(asyncLangfuse.blobStorageIntegrations().findStatusById("integration-1")))
                .isInstanceOf(LangfuseAuthorizationException.class);
    }

    /**
     * Validated eagerly, outside the {@code completionStage} supplier, so blank input throws from the
     * call rather than emitting a failure at subscription.
     */
    @Test
    void blankStatusIdsAreRejectedEagerlyRatherThanEmittedAsAFailure() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.blobStorageIntegrations().findStatusById("  "))
                .withMessageContaining("Blob storage integration id");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.blobStorageIntegrations().findStatusById(null))
                .withMessageContaining("Blob storage integration id");

        verifyListRequests(0);
    }

    // --- writes ----------------------------------------------------------------------------

    /**
     * A genuine server-side upsert: one PUT, no preceding lookup, so there is no window in which two
     * callers can both observe the integration as absent.
     */
    @Test
    void upsertIssuesASinglePutWithoutLookingTheIntegrationUpFirst() {
        stubUpsert(9);

        assertThat(await(asyncLangfuse.blobStorageIntegrations().upsert(upsertRequest())))
                .extracting(BlobStorageIntegrationResponse::getId, BlobStorageIntegrationResponse::getBucketName)
                .containsExactly("integration-9", "bucket-9");

        verifyUpsertRequests(1);
        verifyListRequests(0);
    }

    // --- deletion --------------------------------------------------------------------------

    @Test
    void deleteByIdReportsEachIdSeparately() {
        stubIntegrationDeleted("integration-1");
        stubIntegrationDeleted("integration-2");

        assertThat(await(asyncLangfuse.blobStorageIntegrations().deleteById("integration-1", "integration-2")))
                .satisfies(result -> assertThat(result.deleted())
                        .containsExactlyInAnyOrder("integration-1", "integration-2"))
                .satisfies(result -> assertThat(result.hasFailures()).isFalse());

        verifyDeleteRequests(1, "integration-1");
        verifyDeleteRequests(1, "integration-2");
    }

    @Test
    void deleteByIdReportsAbsenceRatherThanFailing() {
        stubIntegrationDeleteFailure("nope", 404);

        assertThat(await(asyncLangfuse.blobStorageIntegrations().deleteById("nope")))
                .satisfies(result -> assertThat(result.deleted()).isEmpty())
                .satisfies(result -> assertThat(result.notFound()).containsExactly("nope"));
    }

    /**
     * Never fails fast: a mixed batch reports the success, the absence and the failure side by side
     * rather than abandoning the rest at the first problem.
     */
    @Test
    void deleteByIdNeverFailsFastOnAMixedBatch() {
        stubIntegrationDeleted("integration-1");
        stubIntegrationDeleteFailure("integration-2", 404);
        stubIntegrationDeleteFailure("integration-3", 500);

        assertThat(await(asyncLangfuse.blobStorageIntegrations()
                .deleteById(List.of("integration-1", "integration-2", "integration-3"))))
                .satisfies(result -> assertThat(result.deleted()).containsExactly("integration-1"))
                .satisfies(result -> assertThat(result.notFound()).containsExactly("integration-2"))
                .satisfies(result -> assertThat(result.failed()).containsOnlyKeys("integration-3"))
                .satisfies(result -> assertThat(result.outcome("integration-3"))
                        .get()
                        .isInstanceOf(DeletionOutcome.Failed.class));

        verifyDeleteRequests(1, "integration-1");
        verifyDeleteRequests(1, "integration-2");
        verifyDeleteRequests(1, "integration-3");
    }

    @Test
    void deletingAnEmptyCollectionIssuesNoRequest() {
        assertThat(await(asyncLangfuse.blobStorageIntegrations().deleteById(List.of())).outcomes()).isEmpty();

        assertThat(wiremock().getServeEvents()).isEmpty();
    }

    @Test
    void blankDeleteIdsAreRejectedEagerlyRatherThanEmittedAsAFailure() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.blobStorageIntegrations().deleteById("  "))
                .withMessageContaining("Blob storage integration id");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.blobStorageIntegrations().deleteById((String) null))
                .withMessageContaining("Blob storage integration id");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> asyncLangfuse.blobStorageIntegrations().deleteById((String[]) null))
                .withMessageContaining("Blob storage integration id");

        assertThat(wiremock().getServeEvents()).isEmpty();
    }

    /**
     * A delete failure that is not a 404 is reported as a failure, never recovered - the same
     * absence-versus-failure rule the lookups follow.
     */
    @Test
    void deleteByIdNeverMistakesARejectedCredentialForAbsence() {
        stubIntegrationDeleteFailure("integration-1", 401);

        assertThat(await(asyncLangfuse.blobStorageIntegrations().deleteById("integration-1")))
                .satisfies(result -> assertThat(result.notFound()).isEmpty())
                .satisfies(result -> assertThat(result.failed()).containsOnlyKeys("integration-1"));
    }

    private static CreateBlobStorageIntegrationRequest upsertRequest() {
        return CreateBlobStorageIntegrationRequest.builder()
                .projectId("project-9")
                .type(BlobStorageIntegrationType.S3)
                .bucketName("bucket-9")
                .exportFrequency(BlobStorageExportFrequency.DAILY)
                .enabled(true)
                .forcePathStyle(false)
                .fileType(BlobStorageIntegrationFileType.JSONL)
                .build();
    }

    private static <T> T await(Uni<T> uni) {
        return uni.await().atMost(Duration.ofSeconds(10));
    }
}
