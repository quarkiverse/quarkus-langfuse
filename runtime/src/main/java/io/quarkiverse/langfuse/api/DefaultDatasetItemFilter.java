package io.quarkiverse.langfuse.api;

import java.time.OffsetDateTime;
import java.util.Optional;

// The components carry the raw nullable values and are renamed, because a record component and an
// Optional-returning accessor cannot share a name. Same shape as DefaultCursor.cursorValue backing
// Optional<String> value().
record DefaultDatasetItemFilter(String rawDatasetName, String rawSourceTraceId, String rawSourceObservationId,
        OffsetDateTime rawVersion) implements DatasetItemFilter {

    // Package-private, not private: Builder is nested in the interface rather than in this record, so
    // the two are not nestmates and a private constructor would be unreachable from build().
    DefaultDatasetItemFilter(Builder builder) {
        this(builder.rawDatasetName, builder.rawSourceTraceId, builder.rawSourceObservationId, builder.rawVersion);
    }

    @Override
    public Optional<String> datasetName() {
        return Optional.ofNullable(this.rawDatasetName);
    }

    @Override
    public Optional<String> sourceTraceId() {
        return Optional.ofNullable(this.rawSourceTraceId);
    }

    @Override
    public Optional<String> sourceObservationId() {
        return Optional.ofNullable(this.rawSourceObservationId);
    }

    @Override
    public Optional<OffsetDateTime> version() {
        return Optional.ofNullable(this.rawVersion);
    }
}
