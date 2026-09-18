package io.quarkiverse.langfuse.api;

import java.util.Optional;
import java.util.Set;

// The components carry the raw nullable values and are renamed, because a record component and an
// Optional-returning accessor cannot share a name. Same shape as DefaultCursor.cursorValue backing
// Optional<String> value().
record DefaultExperimentFilter(Set<ExperimentFieldGroup> rawFields, Integer rawScoreLimit, String rawId,
        String rawName, String rawDatasetId, String rawFilter) implements ExperimentFilter {

    // fields() is the one accessor that does not return an Optional, so its component is normalized
    // here instead: an absent group set reads as "core fields only", and the copy keeps the record
    // immutable even when the caller mutates the set it handed the builder.
    DefaultExperimentFilter {
        rawFields = (rawFields == null) ? Set.of() : Set.copyOf(rawFields);
    }

    // Package-private, not private: Builder is nested in the interface rather than in this record, so
    // the two are not nestmates and a private constructor would be unreachable from build().
    DefaultExperimentFilter(Builder builder) {
        this(builder.rawFields, builder.rawScoreLimit, builder.rawId, builder.rawName, builder.rawDatasetId,
                builder.rawFilter);
    }

    @Override
    public Set<ExperimentFieldGroup> fields() {
        return this.rawFields;
    }

    @Override
    public Optional<Integer> scoreLimit() {
        return Optional.ofNullable(this.rawScoreLimit);
    }

    @Override
    public Optional<String> id() {
        return Optional.ofNullable(this.rawId);
    }

    @Override
    public Optional<String> name() {
        return Optional.ofNullable(this.rawName);
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
