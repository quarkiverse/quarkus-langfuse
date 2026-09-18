package io.quarkiverse.langfuse.api;

import java.util.Optional;
import java.util.Set;

// The components carry the raw nullable values and are renamed, because a record component and an
// Optional-returning accessor cannot share a name. Same shape as DefaultCursor.cursorValue backing
// Optional<String> value().
record DefaultExperimentItemFilter(Set<ExperimentItemFieldGroup> rawFields, Integer rawScoreLimit,
        String rawExperimentId, String rawExperimentName, String rawExperimentItemId, String rawDatasetId,
        String rawFilter) implements ExperimentItemFilter {

    // fields() is the one accessor that does not return an Optional, so its component is normalized
    // here instead: an absent group set reads as "the Langfuse default groups", and the copy keeps the
    // record immutable even when the caller mutates the set it handed the builder.
    DefaultExperimentItemFilter {
        rawFields = (rawFields == null) ? Set.of() : Set.copyOf(rawFields);
    }

    // Package-private, not private: Builder is nested in the interface rather than in this record, so
    // the two are not nestmates and a private constructor would be unreachable from build().
    DefaultExperimentItemFilter(Builder builder) {
        this(builder.rawFields, builder.rawScoreLimit, builder.rawExperimentId, builder.rawExperimentName,
                builder.rawExperimentItemId, builder.rawDatasetId, builder.rawFilter);
    }

    @Override
    public Set<ExperimentItemFieldGroup> fields() {
        return this.rawFields;
    }

    @Override
    public Optional<Integer> scoreLimit() {
        return Optional.ofNullable(this.rawScoreLimit);
    }

    @Override
    public Optional<String> experimentId() {
        return Optional.ofNullable(this.rawExperimentId);
    }

    @Override
    public Optional<String> experimentName() {
        return Optional.ofNullable(this.rawExperimentName);
    }

    @Override
    public Optional<String> experimentItemId() {
        return Optional.ofNullable(this.rawExperimentItemId);
    }

    @Override
    public Optional<String> datasetId() {
        return Optional.ofNullable(this.rawDatasetId);
    }

    @Override
    public Optional<String> filter() {
        return Optional.ofNullable(this.rawFilter);
    }
}
