package io.quarkiverse.langfuse.runtime.langchain4j;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import org.eclipse.microprofile.config.spi.ConfigSource;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.config.PropertiesConfigSource;

/**
 * A factory implementation for providing a {@link ConfigSource} that supplies default configuration
 * properties for Langfuse integration in LangChain4j.
 *
 * This factory derives configuration properties that enable tracing features for prompts, completions,
 * tool arguments, and tool results during LangChain4j operations. These properties are pre-configured
 * with default values and have a predefined priority ensuring they can be overridden by higher
 * priority sources such as environment variables or application properties.
 *
 * <p>
 * Key behavior:
 * <ul>
 * <li>Provides default configuration for LangChain4j tracing-related settings.</li>
 * <li>Sets a low ordinal (priority) for its {@link ConfigSource}, allowing overriding configurations
 * from user-defined sources.</li>
 * <li>Ensures that Langfuse-specific settings are made available after higher-priority sources have
 * been initialized.</li>
 * </ul>
 *
 * @see LangfuseLangchain4jConfigBuilder
 */
public class LangfuseLangchain4jConfigSourceFactory implements ConfigSourceFactory {
    /**
     * Config source ordinal. Set to 50, well below {@code application.properties} (250),
     * environment variables (300), and system properties (400) so that any explicit OTel
     * configuration by the user takes precedence over the auto-derived values.
     */
    public static final int ORDINAL = 50;

    /**
     * The name assigned to the {@link ConfigSource} returned by this factory, used for
     * identification in config diagnostics and logging.
     */
    public static final String CONFIG_SOURCE_NAME = "LangfuseLangchain4jConfigSource";

    /**
     * Configuration property name that determines whether user prompts should be included
     * in tracing data during LangChain4j operations. Setting this property to "true" enables
     * the tracing of prompts, which can be useful for debugging and observability purposes
     * in applications using Langfuse integration with LangChain4j.
     */
    public static final String LC4J_TRACING_INCLUDE_PROMPT = "quarkus.langchain4j.tracing.include-prompt";

    /**
     * Configuration property name that determines whether completions should be included
     * in tracing data during LangChain4j operations. Setting this property to "true" enables
     * the tracing of completions, which can be useful for debugging, monitoring, and observability
     * in applications utilizing Langfuse integration with LangChain4j.
     */
    public static final String LC4J_TRACING_INCLUDE_COMPLETION = "quarkus.langchain4j.tracing.include-completion";

    /**
     * Configuration property name used to control whether tool argument details are included
     * in tracing data during LangChain4j operations. When enabled, this feature provides
     * additional observability by capturing the arguments passed to tools, which can assist
     * in debugging and monitoring workflows.
     */
    public static final String LC4J_TRACING_INCLUDE_TOOL_ARGUMENTS = "quarkus.langchain4j.tracing.include-tool-arguments";

    /**
     * Configuration property name that determines whether tool results should be included
     * in tracing data during LangChain4j operations. Setting this property to "true" enables
     * the tracing of tool results, which can be useful for debugging, monitoring, and improving
     * observability in applications using Langfuse integration with LangChain4j.
     */
    public static final String LC4J_TRACING_INCLUDE_TOOL_RESULT = "quarkus.langchain4j.tracing.include-tool-result";

    private static final Map<String, String> PROPERTIES = Map.of(
            LC4J_TRACING_INCLUDE_PROMPT, "true",
            LC4J_TRACING_INCLUDE_COMPLETION, "true",
            LC4J_TRACING_INCLUDE_TOOL_ARGUMENTS, "true",
            LC4J_TRACING_INCLUDE_TOOL_RESULT, "true");

    @Override
    public Iterable<ConfigSource> getConfigSources(ConfigSourceContext context) {
        return List.of(new PropertiesConfigSource(PROPERTIES, CONFIG_SOURCE_NAME, ORDINAL));
    }

    /**
     * Returns a low priority so this factory runs after higher-priority config sources
     * (such as DevServices and {@code application.properties}) have been established,
     * ensuring the Langfuse properties are available for reading.
     */
    @Override
    public OptionalInt getPriority() {
        return OptionalInt.of(Integer.MIN_VALUE + 100);
    }
}
