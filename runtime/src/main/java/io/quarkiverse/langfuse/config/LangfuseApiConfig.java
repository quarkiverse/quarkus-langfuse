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

    /**
     * Maximum number of deletes a single batch delete operation runs at once.
     *
     * <p>
     * Langfuse has no bulk delete endpoint, so {@code deleteById}, {@code deleteByName} and
     * {@code deleteByProvider} delete one resource per request. This caps how many of those requests
     * are in flight for one call; it is neither a thread pool size nor a limit on how many identifiers
     * may be passed. Langfuse rate-limits, and an unbounded fan-out simply manufactures the {@code 429}
     * responses that the returned result would then report as failures.
     *
     * <p>
     * A value of {@code 1} runs the deletes strictly one at a time on the calling thread, with no
     * fan-out at all, which is useful for deterministic debugging or a rate-limit-sensitive instance.
     * Values below {@code 1} are clamped to {@code 1} rather than rejected.
     *
     * <p>
     * This setting changes only timing and the order requests interleave. The outcome reported for
     * every identifier is identical at any value, and validation, deduplication and the
     * deleted/not-found/failed contract are unaffected by it.
     */
    @WithDefault("4")
    int deleteConcurrency();
}
