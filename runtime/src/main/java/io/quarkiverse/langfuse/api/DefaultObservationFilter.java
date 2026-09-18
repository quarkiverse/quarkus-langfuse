package io.quarkiverse.langfuse.api;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import com.langfuse.api.model.ObservationLevel;
import com.langfuse.api.model.ObservationType;

// The components carry the raw nullable values and are renamed, because a record component and an
// Optional-returning accessor cannot share a name. Same shape as DefaultCursor.cursorValue backing
// Optional<String> value().
record DefaultObservationFilter(String rawFields, String rawExpandMetadata, String rawName, String rawUserId,
        String rawSessionId, ObservationType rawType, String rawTraceId, ObservationLevel rawLevel,
        String rawParentObservationId, Boolean rawIsRootObservation, List<String> rawEnvironment,
        OffsetDateTime rawFromStartTime, OffsetDateTime rawToStartTime, String rawVersion,
        String rawFilter) implements ObservationFilter {

    // Package-private, not private: Builder is nested in the interface rather than in this record, so
    // the two are not nestmates and a private constructor would be unreachable from build().
    DefaultObservationFilter(Builder builder) {
        this(builder.rawFields, builder.rawExpandMetadata, builder.rawName, builder.rawUserId, builder.rawSessionId,
                builder.rawType, builder.rawTraceId, builder.rawLevel, builder.rawParentObservationId,
                builder.rawIsRootObservation, builder.rawEnvironment, builder.rawFromStartTime,
                builder.rawToStartTime, builder.rawVersion, builder.rawFilter);
    }

    @Override
    public Optional<String> fields() {
        return Optional.ofNullable(this.rawFields);
    }

    @Override
    public Optional<String> expandMetadata() {
        return Optional.ofNullable(this.rawExpandMetadata);
    }

    @Override
    public Optional<String> name() {
        return Optional.ofNullable(this.rawName);
    }

    @Override
    public Optional<String> userId() {
        return Optional.ofNullable(this.rawUserId);
    }

    @Override
    public Optional<String> sessionId() {
        return Optional.ofNullable(this.rawSessionId);
    }

    @Override
    public Optional<ObservationType> type() {
        return Optional.ofNullable(this.rawType);
    }

    @Override
    public Optional<String> traceId() {
        return Optional.ofNullable(this.rawTraceId);
    }

    @Override
    public Optional<ObservationLevel> level() {
        return Optional.ofNullable(this.rawLevel);
    }

    @Override
    public Optional<String> parentObservationId() {
        return Optional.ofNullable(this.rawParentObservationId);
    }

    @Override
    public Optional<Boolean> isRootObservation() {
        return Optional.ofNullable(this.rawIsRootObservation);
    }

    // The component is already an immutable copy, taken by Builder.environment, so it is handed back
    // as it is. An unset criterion reads as an empty list rather than as null.
    @Override
    public List<String> environment() {
        return (this.rawEnvironment == null) ? List.of() : this.rawEnvironment;
    }

    @Override
    public Optional<OffsetDateTime> fromStartTime() {
        return Optional.ofNullable(this.rawFromStartTime);
    }

    @Override
    public Optional<OffsetDateTime> toStartTime() {
        return Optional.ofNullable(this.rawToStartTime);
    }

    @Override
    public Optional<String> version() {
        return Optional.ofNullable(this.rawVersion);
    }

    @Override
    public Optional<String> filter() {
        return Optional.ofNullable(this.rawFilter);
    }
}
