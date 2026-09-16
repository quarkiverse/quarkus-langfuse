package io.quarkiverse.langfuse.api;

import io.quarkiverse.langfuse.util.ValidationUtils;

record DefaultNotFound(String identifier) implements DeletionOutcome.NotFound {

    DefaultNotFound {
        ValidationUtils.ensureNotBlank(identifier, "Identifier");
    }
}
