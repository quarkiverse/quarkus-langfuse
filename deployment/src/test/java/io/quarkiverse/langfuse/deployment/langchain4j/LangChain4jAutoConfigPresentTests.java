package io.quarkiverse.langfuse.deployment.langchain4j;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.langfuse.runtime.langchain4j.LangfuseLangchain4jConfigSourceFactory;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Unit tests for the LangChain4j auto-configuration.
 *
 * Verifies that the LangChain4j configuration properties, particularly those
 * related to Langfuse integration, are properly set to their expected default values
 * during application runtime. The tests ensure that tracing-related properties for prompts,
 * completions, tool arguments, and tool results are enabled as expected when the application
 * configuration is initialized.
 */
public class LangChain4jAutoConfigPresentTests {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideRuntimeConfigKey("quarkus.langfuse.base-url", "http://localhost:3000")
            .overrideRuntimeConfigKey("quarkus.langfuse.username", "pk-lf-test")
            .overrideRuntimeConfigKey("quarkus.langfuse.password", "sk-lf-test");

    @Test
    void langchain4jConfigAutoSet() {
        var config = ConfigProvider.getConfig();

        assertThat(config.getOptionalValue(LangfuseLangchain4jConfigSourceFactory.LC4J_TRACING_INCLUDE_PROMPT, Boolean.class))
                .as("LC4j tracing include prompt should be set to true")
                .get(InstanceOfAssertFactories.BOOLEAN)
                .isTrue();

        assertThat(
                config.getOptionalValue(LangfuseLangchain4jConfigSourceFactory.LC4J_TRACING_INCLUDE_COMPLETION, Boolean.class))
                .as("LC4j tracing include completion should be set to true")
                .get(InstanceOfAssertFactories.BOOLEAN)
                .isTrue();

        assertThat(config.getOptionalValue(LangfuseLangchain4jConfigSourceFactory.LC4J_TRACING_INCLUDE_TOOL_ARGUMENTS,
                Boolean.class))
                .as("LC4j tracing include tool arguments should be set to true")
                .get(InstanceOfAssertFactories.BOOLEAN)
                .isTrue();

        assertThat(
                config.getOptionalValue(LangfuseLangchain4jConfigSourceFactory.LC4J_TRACING_INCLUDE_TOOL_RESULT, Boolean.class))
                .as("LC4j tracing include tool result should be set to true")
                .get(InstanceOfAssertFactories.BOOLEAN)
                .isTrue();
    }
}
