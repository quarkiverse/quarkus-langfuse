package io.quarkiverse.langfuse.runtime.otel;

import io.quarkus.runtime.configuration.ConfigBuilder;
import io.smallrye.config.SmallRyeConfigBuilder;

/**
 * Quarkus {@link ConfigBuilder} that registers a {@link LangfuseOtelConfigSourceFactory} to
 * derive OpenTelemetry OTLP exporter configuration from existing Langfuse connection properties.
 * <p>
 * This builder is conditionally activated at build time only when the {@code quarkus-opentelemetry}
 * extension is present and {@code quarkus.langfuse.otel.enabled} is {@code true}.
 *
 * @see LangfuseOtelConfigSourceFactory
 */
public class LangfuseOtelConfigBuilder implements ConfigBuilder {
    /**
     * Registers the {@link LangfuseOtelConfigSourceFactory} so that OTel exporter
     * properties are derived from Langfuse configuration at config resolution time.
     */
    @Override
    public SmallRyeConfigBuilder configBuilder(SmallRyeConfigBuilder builder) {
        return builder.withSources(new LangfuseOtelConfigSourceFactory());
    }

    /**
     * Returns a low priority so that the OpenTelemetry extension's own config builder
     * runs first and establishes its defaults and fallbacks before this builder layers
     * the Langfuse-derived values on top.
     */
    @Override
    public int priority() {
        return Integer.MIN_VALUE + 50;
    }
}
