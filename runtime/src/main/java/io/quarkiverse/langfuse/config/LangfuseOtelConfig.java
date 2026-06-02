package io.quarkiverse.langfuse.config;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

/**
 * Configuration group interface for setting up OpenTelemetry integration
 * with Langfuse. This interface is nested within the Langfuse configuration
 * hierarchy and is used to define properties and settings specific to
 * OpenTelemetry-related aspects of Langfuse integration.
 *
 * Example configuration key prefix in the application.properties or equivalent:
 * {@code quarkus.langfuse.otel}
 */
@ConfigGroup
public interface LangfuseOtelConfig {
    /**
     * The URL used for ingesting traces in the OpenTelemetry integration
     * for Langfuse. The URL points to the endpoint where trace data is exported.
     */
    @WithDefault("${quarkus.langfuse.base-url}/api/public/otel/v1/traces")
    String traceIngestionUrl();

    /**
     * Retrieves the configured span filter type used for OpenTelemetry integration
     * within Langfuse. The span filter determines which spans are included based
     * on their context or relevance to specific operations, such as AI-related
     * activities.
     *
     * @return The configured {@link SpanFilterType}, which defines the span filtering
     *         logic. By default, this is set to {@code AI_ONLY}.
     */
    @WithDefault("AI_ONLY")
    SpanFilterType spanFilter();

    /**
     * Enum representing the types of span filters available for OpenTelemetry integration
     * within Langfuse configuration.
     *
     * <ul>
     * <li>ALL - All spans are included without any filtering.</li>
     * <li>AI_ONLY - Only spans related to AI-specific operations are included.</li>
     * </ul>
     */
    enum SpanFilterType {
        /**
         * Represents a span filter type where all spans are included without any filtering.
         * Used in OpenTelemetry integration within the Langfuse configuration to encompass
         * every span regardless of its context or purpose.
         */
        ALL,

        /**
         * Represents a span filter type where only spans related to AI-specific operations
         * are included. This is used in OpenTelemetry integration within the Langfuse
         * configuration to filter out spans unrelated to AI activities or processes,
         * focusing exclusively on AI-driven operations.
         * <p>
         * This is the default. Non-AI spans create lots of clutter/noise in Langfuse.
         * </p>
         */
        AI_ONLY;
    }
}
