package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import io.quarkiverse.langfuse.util.ValidationUtils;

record DefaultDeletionResult(Map<String, DeletionOutcome> outcomes) implements DeletionResult {

    DefaultDeletionResult {
        ValidationUtils.ensureNotNull(outcomes, "Outcomes");

        outcomes = Collections.unmodifiableMap(new LinkedHashMap<>(outcomes));
    }

    DefaultDeletionResult(Collection<DeletionOutcome> outcomes) {
        this(index(outcomes));
    }

    @Override
    public Optional<DeletionOutcome> outcome(String identifier) {
        return Optional.ofNullable(identifier)
                .map(outcomes::get);
    }

    // Indexes the outcomes by identifier so lookup does not scan. putIfAbsent keeps the first outcome
    // seen for an identifier, so a caller-supplied collection holding duplicates collapses the same way
    // the delete operations deduplicate their input.
    private static Map<String, DeletionOutcome> index(Collection<DeletionOutcome> outcomes) {
        ValidationUtils.ensureNotNull(outcomes, "Outcomes");

        var indexed = new LinkedHashMap<String, DeletionOutcome>();

        for (var outcome : outcomes) {
            ValidationUtils.ensureNotNull(outcome, "Outcome");
            indexed.putIfAbsent(outcome.identifier(), outcome);
        }

        return indexed;
    }
}
