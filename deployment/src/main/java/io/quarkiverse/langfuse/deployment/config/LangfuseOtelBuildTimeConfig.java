package io.quarkiverse.langfuse.deployment.config;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

/**
 * Build-time configuration group for automatic OpenTelemetry integration,
 * mapped to properties under {@code quarkus.langfuse.otel}.
 */
@ConfigGroup
public interface LangfuseOtelBuildTimeConfig {
    /**
     * Whether to automatically configure the OpenTelemetry OTLP exporter
     * to send traces to Langfuse when the {@code quarkus-opentelemetry}
     * extension is present.
     * <p>
     * Defaults to the value of {@code quarkus.otel.enabled}, falling back to {@code true}
     * if that property is not set.
     */
    @WithDefault("${quarkus.otel.enabled:true}")
    boolean enabled();
}
