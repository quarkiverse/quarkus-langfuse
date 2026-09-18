package io.quarkiverse.langfuse.api;

import java.time.OffsetDateTime;

import com.langfuse.api.experiments.async.ExperimentsApi;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;

// Both bounds are validated here rather than inside the operations, and outside any deferred
// assembly, so an unusable window is thrown from since(...) or between(...) rather than emitted as a
// failure at subscription.
record DefaultAsyncExperimentTimeWindow(ExperimentsApi experimentsApi, LangfuseConfig config)
        implements
            AsyncExperimentTimeWindow {

    @Override
    public AsyncExperimentOperations since(OffsetDateTime fromStartTime) {
        return new DefaultAsyncExperimentOperations(this.experimentsApi, this.config,
                ValidationUtils.ensureNotNull(fromStartTime, "From start time"), null);
    }

    @Override
    public AsyncExperimentOperations between(OffsetDateTime fromStartTime, OffsetDateTime toStartTime) {
        ensureOrdered(ValidationUtils.ensureNotNull(fromStartTime, "From start time"),
                ValidationUtils.ensureNotNull(toStartTime, "To start time"));

        return new DefaultAsyncExperimentOperations(this.experimentsApi, this.config, fromStartTime, toStartTime);
    }

    private static void ensureOrdered(OffsetDateTime fromStartTime, OffsetDateTime toStartTime) {
        if (toStartTime.isBefore(fromStartTime)) {
            throw new IllegalArgumentException(
                    "To start time must not precede from start time, but %s precedes %s".formatted(toStartTime,
                            fromStartTime));
        }
    }
}
