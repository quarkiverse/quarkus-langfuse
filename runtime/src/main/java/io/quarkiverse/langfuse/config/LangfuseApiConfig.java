package io.quarkiverse.langfuse.config;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

/**
 * Configuration group for the higher-level Langfuse API operations exposed by
 * {@code io.quarkiverse.langfuse.api}.
 *
 * <p>
 * These settings control how many items each request asks Langfuse for while traversing a
 * collection. They are only defaults: any operation that accepts a
 * {@code PageSelection} or a {@code CursorSelection} specifying its own size uses that instead.
 *
 * <p>
 * Example configuration key prefix in {@code application.properties} or equivalent:
 * {@code quarkus.langfuse.api}
 */
@ConfigGroup
public interface LangfuseApiConfig {
    /**
     * Default number of items requested per page when traversing a page-addressed collection,
     * such as models, datasets, LLM connections or score configs.
     *
     * <p>
     * Langfuse caps this per endpoint (most allow at most {@code 100}); a value above an
     * endpoint's cap is rejected by the server rather than by this extension.
     */
    @WithDefault("50")
    int defaultPageSize();

    /**
     * Default number of items requested per batch when traversing a cursor-addressed collection,
     * such as evaluation rules.
     *
     * <p>
     * Langfuse caps this per endpoint (most allow at most {@code 100}); a value above an
     * endpoint's cap is rejected by the server rather than by this extension.
     */
    @WithDefault("50")
    int defaultBatchSize();
}
