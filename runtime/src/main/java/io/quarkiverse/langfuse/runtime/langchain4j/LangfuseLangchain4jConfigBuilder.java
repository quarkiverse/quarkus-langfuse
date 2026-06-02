package io.quarkiverse.langfuse.runtime.langchain4j;

import io.quarkus.runtime.configuration.ConfigBuilder;
import io.smallrye.config.SmallRyeConfigBuilder;

/**
 * Quarkus {@link ConfigBuilder} that registers a {@link LangfuseLangchain4jConfigSourceFactory} to
 * derive provide default configuration according to
 * https://docs.quarkiverse.io/quarkus-langchain4j/dev/observability.html#_langfuse
 * <p>
 * This builder is conditionally activated at build time only when {@code quarkus-langchain4j}.
 *
 * @see LangfuseLangchain4jConfigSourceFactory
 */
public class LangfuseLangchain4jConfigBuilder implements ConfigBuilder {
    /**
     * Registers the {@link LangfuseLangchain4jConfigSourceFactory} so that OTel exporter
     * properties are derived from Langfuse configuration at config resolution time.
     */
    @Override
    public SmallRyeConfigBuilder configBuilder(SmallRyeConfigBuilder builder) {
        return builder.withSources(new LangfuseLangchain4jConfigSourceFactory());
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
