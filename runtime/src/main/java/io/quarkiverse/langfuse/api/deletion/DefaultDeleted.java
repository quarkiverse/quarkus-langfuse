package io.quarkiverse.langfuse.api.deletion;

import io.quarkiverse.langfuse.util.ValidationUtils;

record DefaultDeleted(String identifier) implements DeletionOutcome.Deleted {

    DefaultDeleted {
        ValidationUtils.ensureNotBlank(identifier, "Identifier");
    }
}
