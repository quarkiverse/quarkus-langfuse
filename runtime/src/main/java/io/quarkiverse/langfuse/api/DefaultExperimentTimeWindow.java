package io.quarkiverse.langfuse.api;

import java.time.OffsetDateTime;

import com.langfuse.api.experiments.ExperimentsApi;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;

// Both bounds are validated here rather than inside the operations, so an unusable window fails at
// since(...) or between(...) instead of at the first traversal. The ordering check is duplicated in
// each gateway rather than shared: the two experiment domains hold no common contract by design.
record DefaultExperimentTimeWindow(ExperimentsApi experimentsApi, LangfuseConfig config)
        implements
            ExperimentTimeWindow {

    @Override
    public ExperimentOperations since(OffsetDateTime fromStartTime) {
        return new DefaultExperimentOperations(this.experimentsApi, this.config,
                ValidationUtils.ensureNotNull(fromStartTime, "From start time"), null);
    }

    @Override
    public ExperimentOperations between(OffsetDateTime fromStartTime, OffsetDateTime toStartTime) {
        ensureOrdered(ValidationUtils.ensureNotNull(fromStartTime, "From start time"),
                ValidationUtils.ensureNotNull(toStartTime, "To start time"));

        return new DefaultExperimentOperations(this.experimentsApi, this.config, fromStartTime, toStartTime);
    }

    private static void ensureOrdered(OffsetDateTime fromStartTime, OffsetDateTime toStartTime) {
        if (toStartTime.isBefore(fromStartTime)) {
            throw new IllegalArgumentException(
                    "To start time must not precede from start time, but %s precedes %s".formatted(toStartTime,
                            fromStartTime));
        }
    }
}
