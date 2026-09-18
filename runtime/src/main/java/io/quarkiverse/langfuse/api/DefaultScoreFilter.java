package io.quarkiverse.langfuse.api;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

import com.langfuse.api.model.ScoreDataType;
import com.langfuse.api.model.ScoreSource;

// The components carry the raw nullable values and are renamed, because a record component and an
// Optional-returning accessor cannot share a name. Same shape as DefaultCursor.cursorValue backing
// Optional<String> value().
record DefaultScoreFilter(Set<ScoreFieldGroup> rawFields, String rawId, String rawName, ScoreSource rawSource,
        ScoreDataType rawDataType, String rawEnvironment, String rawConfigId, String rawQueueId, String rawAuthorUserId,
        String rawValue, Double rawValueMin, Double rawValueMax, String rawTraceId, String rawSessionId,
        String rawObservationId, String rawExperimentId, OffsetDateTime rawFromTimestamp,
        OffsetDateTime rawToTimestamp) implements ScoreFilter {

    // fields() is the one accessor that does not return an Optional, so its component is normalized
    // here instead: an absent group set reads as "core fields only", and the copy keeps the record
    // immutable even when the caller mutates the set it handed the builder.
    DefaultScoreFilter {
        rawFields = (rawFields == null) ? Set.of() : Set.copyOf(rawFields);
    }

    // Package-private, not private: Builder is nested in the interface rather than in this record, so
    // the two are not nestmates and a private constructor would be unreachable from build().
    DefaultScoreFilter(Builder builder) {
        this(builder.rawFields, builder.rawId, builder.rawName, builder.rawSource, builder.rawDataType,
                builder.rawEnvironment, builder.rawConfigId, builder.rawQueueId, builder.rawAuthorUserId,
                builder.rawValue, builder.rawValueMin, builder.rawValueMax, builder.rawTraceId, builder.rawSessionId,
                builder.rawObservationId, builder.rawExperimentId, builder.rawFromTimestamp, builder.rawToTimestamp);
    }

    @Override
    public Set<ScoreFieldGroup> fields() {
        return this.rawFields;
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
    public Optional<ScoreSource> source() {
        return Optional.ofNullable(this.rawSource);
    }

    @Override
    public Optional<ScoreDataType> dataType() {
        return Optional.ofNullable(this.rawDataType);
    }

    @Override
    public Optional<String> environment() {
        return Optional.ofNullable(this.rawEnvironment);
    }

    @Override
    public Optional<String> configId() {
        return Optional.ofNullable(this.rawConfigId);
    }

    @Override
    public Optional<String> queueId() {
        return Optional.ofNullable(this.rawQueueId);
    }

    @Override
    public Optional<String> authorUserId() {
        return Optional.ofNullable(this.rawAuthorUserId);
    }

    @Override
    public Optional<String> value() {
        return Optional.ofNullable(this.rawValue);
    }

    @Override
    public Optional<Double> valueMin() {
        return Optional.ofNullable(this.rawValueMin);
    }

    @Override
    public Optional<Double> valueMax() {
        return Optional.ofNullable(this.rawValueMax);
    }

    @Override
    public Optional<String> traceId() {
        return Optional.ofNullable(this.rawTraceId);
    }

    @Override
    public Optional<String> sessionId() {
        return Optional.ofNullable(this.rawSessionId);
    }

    @Override
    public Optional<String> observationId() {
        return Optional.ofNullable(this.rawObservationId);
    }

    @Override
    public Optional<String> experimentId() {
        return Optional.ofNullable(this.rawExperimentId);
    }

    @Override
    public Optional<OffsetDateTime> fromTimestamp() {
        return Optional.ofNullable(this.rawFromTimestamp);
    }

    @Override
    public Optional<OffsetDateTime> toTimestamp() {
        return Optional.ofNullable(this.rawToTimestamp);
    }
}
