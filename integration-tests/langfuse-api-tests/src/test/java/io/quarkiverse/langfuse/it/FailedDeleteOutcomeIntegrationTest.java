package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkiverse.langfuse.api.AsyncLangfuseOperations;
import io.quarkiverse.langfuse.api.DeletionOutcome;
import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.client.LangfuseAuthenticationException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

/**
 * Verifies that a genuine server-side failure becomes a {@code Failed} outcome carrying its cause,
 * rather than propagating or being mistaken for absence.
 *
 * <p>
 * The failure is provoked with an invalid secret key, so every request answers {@code 401}. That is
 * the one reliably reproducible non-404 error against a self-hosted instance: deleting a
 * Langfuse-managed model answers {@code 404} and so reads as absence, and evaluator mutations are
 * enterprise-gated rather than dependably rejected.
 *
 * <p>
 * The {@code 401} also doubles as the end-to-end regression test for issue #96: the cause must be a
 * {@link LangfuseAuthenticationException} raised by our own exception mapper, not the generic
 * substitute RESTEasy throws when a {@code ResponseExceptionMapper} declines to map a response.
 *
 * <p>
 * Credentials are wrong for the whole Quarkus instance here, which is why this cannot live in
 * {@link OperationsIntegrationTest}.
 */
@QuarkusTest
@TestProfile(FailedDeleteOutcomeIntegrationTest.InvalidCredentialsProfile.class)
class FailedDeleteOutcomeIntegrationTest {

    public static class InvalidCredentialsProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("quarkus.langfuse.secret-key", "sk-lf-invalid-" + UUID.randomUUID());
        }
    }

    @Inject
    LangfuseOperations langfuse;

    @Inject
    AsyncLangfuseOperations asyncLangfuse;

    @Test
    void aRejectedRequestBecomesFailedRatherThanNotFound() {
        var id = "any-id-" + UUID.randomUUID();

        var result = langfuse.models().deleteById(id);

        assertThat(result.hasFailures()).isTrue();
        assertThat(result.outcome(id))
                .get()
                .isInstanceOfSatisfying(DeletionOutcome.Failed.class,
                        failed -> assertThat(failed.cause())
                                .isInstanceOf(LangfuseAuthenticationException.class)
                                .hasMessageContaining("401"));
        assertThat(result.failed()).containsOnlyKeys(id);
        assertThat(result.notFound()).isEmpty();
    }

    @Test
    void everyIdentifierIsStillAttemptedWhenAllOfThemFail() {
        var ids = List.of("a-" + UUID.randomUUID(), "b-" + UUID.randomUUID(), "c-" + UUID.randomUUID());

        var result = langfuse.models().deleteById(ids);

        assertThat(result.size()).isEqualTo(3);
        assertThat(result.failed()).containsOnlyKeys(ids.toArray(String[]::new));
    }

    @Test
    void theAsyncTreeReportsFailuresAsItemsRatherThanFailingTheUni() {
        var id = "any-id-" + UUID.randomUUID();

        var result = asyncLangfuse.models().deleteById(id).await().indefinitely();

        assertThat(result.outcome(id)).get().isInstanceOf(DeletionOutcome.Failed.class);
    }
}
