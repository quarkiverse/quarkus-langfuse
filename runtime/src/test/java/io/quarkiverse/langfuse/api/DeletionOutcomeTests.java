package io.quarkiverse.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

import io.quarkiverse.langfuse.api.DeletionOutcome.Deleted;
import io.quarkiverse.langfuse.api.DeletionOutcome.Failed;
import io.quarkiverse.langfuse.api.DeletionOutcome.NotFound;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;

class DeletionOutcomeTests {

    @Test
    void deletedCarriesOnlyTheIdentifier() {
        assertThat(DeletionOutcome.deleted("model-1"))
                .isInstanceOf(Deleted.class)
                .isNotInstanceOf(Failed.class)
                .extracting(DeletionOutcome::identifier)
                .isEqualTo("model-1");
    }

    @Test
    void notFoundCarriesOnlyTheIdentifier() {
        assertThat(DeletionOutcome.notFound("absent-model"))
                .isInstanceOf(NotFound.class)
                .isNotInstanceOf(Failed.class)
                .extracting(DeletionOutcome::identifier)
                .isEqualTo("absent-model");
    }

    @Test
    void failedCarriesTheIdentifierAndTheCause() {
        var cause = new LangfuseNotFoundException("boom");

        assertThat(DeletionOutcome.failed("model-1", cause))
                .isInstanceOf(Failed.class)
                .extracting(DeletionOutcome::identifier, outcome -> ((Failed) outcome).cause())
                .containsExactly("model-1", cause);
    }

    @Test
    void outcomesOfTheSameBranchAndIdentifierAreEqual() {
        assertThat(DeletionOutcome.deleted("model-1"))
                .isEqualTo(DeletionOutcome.deleted("model-1"))
                .hasSameHashCodeAs(DeletionOutcome.deleted("model-1"))
                .isNotEqualTo(DeletionOutcome.notFound("model-1"))
                .isNotEqualTo(DeletionOutcome.deleted("model-2"));
    }

    @Test
    void everyOutcomeMatchesExactlyOneBranch() {
        // The three branches partition the hierarchy, which is what lets a caller on Java 21+ switch over
        // it with no default. This module compiles at release 17, where switch patterns do not exist, so
        // the equivalent instanceof chain stands in for that here.
        assertThat(describe(DeletionOutcome.deleted("a")))
                .isEqualTo("deleted a");
        assertThat(describe(DeletionOutcome.notFound("b")))
                .isEqualTo("absent b");
        assertThat(describe(DeletionOutcome.failed("c", new IllegalStateException("nope"))))
                .isEqualTo("failed c: nope");
    }

    @Test
    void theHierarchyIsSealedToExactlyThreeBranches() {
        // Guards the exhaustiveness the sealed hierarchy exists to provide: a fourth branch would have to
        // be added here deliberately, and would break every caller's switch at compile time on Java 21+.
        assertThat(DeletionOutcome.class.isSealed()).isTrue();
        assertThat(DeletionOutcome.class.getPermittedSubclasses())
                .containsExactlyInAnyOrder(Deleted.class, NotFound.class, Failed.class);
    }

    @Test
    void branchesAreUsableQualifiedAsWellAsImported() {
        // Both spellings must work: the branches are nested so callers may qualify them, but a nested
        // type is importable, so nesting costs the short form nothing.
        DeletionOutcome outcome = DeletionOutcome.deleted("model-1");

        assertThat(outcome)
                .isInstanceOf(Deleted.class)
                .isInstanceOf(DeletionOutcome.Deleted.class);
    }

    @Test
    void blankIdentifiersAreRejected() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> DeletionOutcome.deleted(null))
                .withMessage("Identifier must not be null or blank");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> DeletionOutcome.notFound("  "))
                .withMessage("Identifier must not be null or blank");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> DeletionOutcome.failed("", new IllegalStateException()))
                .withMessage("Identifier must not be null or blank");
    }

    @Test
    void aFailedOutcomeRequiresACause() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> DeletionOutcome.failed("model-1", null))
                .withMessage("Cause must not be null");
    }

    private static String describe(DeletionOutcome outcome) {
        var description = "";

        if (outcome instanceof Deleted deleted) {
            description = "deleted %s".formatted(deleted.identifier());
        } else if (outcome instanceof NotFound notFound) {
            description = "absent %s".formatted(notFound.identifier());
        } else if (outcome instanceof Failed failed) {
            description = "failed %s: %s".formatted(failed.identifier(), failed.cause().getMessage());
        }

        return description;
    }
}
