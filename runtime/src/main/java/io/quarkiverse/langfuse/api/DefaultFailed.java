package io.quarkiverse.langfuse.api;

import io.quarkiverse.langfuse.util.ValidationUtils;

record DefaultFailed(String identifier, Throwable cause) implements DeletionOutcome.Failed {

    DefaultFailed {
        ValidationUtils.ensureNotBlank(identifier, "Identifier");
        ValidationUtils.ensureNotNull(cause, "Cause");
    }
}
