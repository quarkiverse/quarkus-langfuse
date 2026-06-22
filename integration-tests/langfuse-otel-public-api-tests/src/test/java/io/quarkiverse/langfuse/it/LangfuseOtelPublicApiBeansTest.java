package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApi;

import io.quarkiverse.langfuse.client.QuarkusLangfuseAsyncClient;
import io.quarkiverse.langfuse.client.QuarkusLangfuseClient;
import io.quarkiverse.langfuse.runtime.langchain4j.LangfuseLangchain4jConfigSourceFactory;
import io.quarkiverse.langfuse.runtime.otel.LangfuseSpanProcessor;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class LangfuseOtelPublicApiBeansTest {

    @Inject
    Instance<LangfuseSpanProcessor> langfuseSpanProcessorInstance;

    @Inject
    Instance<QuarkusLangfuseClient> quarkusLangfuseClientInstance;

    @Inject
    Instance<QuarkusLangfuseAsyncClient> quarkusLangfuseAsyncClientInstance;

    @Inject
    Instance<LangfuseApi> langfuseApiInstance;

    @Test
    void langfuseSpanProcessorBeanPresent() {
        assertThat(langfuseSpanProcessorInstance.isResolvable())
                .as("LangfuseSpanProcessor should be present when OTel is available and enabled with export-target=ALL")
                .isTrue();
    }

    @Test
    void quarkusLangfuseClientBeanPresent() {
        assertThat(quarkusLangfuseClientInstance.isResolvable())
                .as("QuarkusLangfuseClient should be present as a synthetic bean")
                .isTrue();
    }

    @Test
    void quarkusLangfuseAsyncClientBeanPresent() {
        assertThat(quarkusLangfuseAsyncClientInstance.isResolvable())
                .as("QuarkusLangfuseAsyncClient should be present as a synthetic bean")
                .isTrue();
    }

    @Test
    void langfuseApiBeanPresent() {
        assertThat(langfuseApiInstance.isResolvable())
                .as("LangfuseApi should be present as a synthetic bean")
                .isTrue();
    }

    @Test
    void langchain4jTracingPropertiesNotSet() {
        var config = ConfigProvider.getConfig();

        assertThat(config.getOptionalValue(LangfuseLangchain4jConfigSourceFactory.LC4J_TRACING_INCLUDE_PROMPT, String.class))
                .as("LC4j tracing include-prompt should not be set when langchain4j is not on the classpath")
                .isEmpty();

        assertThat(config.getOptionalValue(LangfuseLangchain4jConfigSourceFactory.LC4J_TRACING_INCLUDE_COMPLETION,
                String.class))
                .as("LC4j tracing include-completion should not be set when langchain4j is not on the classpath")
                .isEmpty();

        assertThat(config.getOptionalValue(LangfuseLangchain4jConfigSourceFactory.LC4J_TRACING_INCLUDE_TOOL_ARGUMENTS,
                String.class))
                .as("LC4j tracing include-tool-arguments should not be set when langchain4j is not on the classpath")
                .isEmpty();

        assertThat(config.getOptionalValue(LangfuseLangchain4jConfigSourceFactory.LC4J_TRACING_INCLUDE_TOOL_RESULT,
                String.class))
                .as("LC4j tracing include-tool-result should not be set when langchain4j is not on the classpath")
                .isEmpty();
    }
}
