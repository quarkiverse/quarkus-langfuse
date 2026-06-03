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

    /**
     * Specifies the target destination for exporting telemetry data in the OpenTelemetry integration configuration.
     * The default value is "ALL", which directs telemetry data to both the regular OTLP-configured target
     * and Langfuse.
     *
     * @return the export target, which can be {@code ExportTarget.ALL} for multiple destinations or
     *         {@code ExportTarget.LANGFUSE_ONLY} to export only to Langfuse.
     */
    @WithDefault("ALL")
    ExportTarget exportTarget();

    /**
     * Represents the target destination for exporting telemetry data in the
     * OpenTelemetry integration configuration.
     */
    enum ExportTarget {
        /**
         * Exports to the regular OTLP-configured target, which may be Langfuse or another OTLP ingestion endpoint, as well as
         * to Langfuse.
         */
        ALL,

        /**
         * Exports only to Langfuse as the single OTLP exporter.
         */
        LANGFUSE_ONLY;
    }
}
