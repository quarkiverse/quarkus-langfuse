package io.quarkiverse.langfuse.api;

import io.quarkiverse.langfuse.util.ValidationUtils;

record DefaultDeleted(String identifier) implements DeletionOutcome.Deleted {

    DefaultDeleted {
        ValidationUtils.ensureNotBlank(identifier, "Identifier");
    }
}
