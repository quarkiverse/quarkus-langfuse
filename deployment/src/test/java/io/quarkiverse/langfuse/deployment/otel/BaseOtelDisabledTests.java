package io.quarkiverse.langfuse.deployment.otel;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import io.quarkiverse.langfuse.runtime.otel.LangfuseOtelConfigSourceFactory;
import io.quarkiverse.langfuse.runtime.otel.LangfuseSpanProcessor;

abstract class BaseOtelDisabledTests {
    @Inject
    Instance<LangfuseSpanProcessor> spanProcessorInstance;

    @Test
    void otelConfigNotAutoSet() {
        assertThat(ConfigProvider.getConfig()
                .getOptionalValue(LangfuseOtelConfigSourceFactory.OTEL_ENDPOINT_KEY, String.class)
                .filter(v -> v.contains("/api/public/otel")))
                .as("OTel endpoint should not be auto-configured to Langfuse")
                .isEmpty();
    }

    @Test
    void noLangfuseSpanProcessor() {
        assertThat(this.spanProcessorInstance.isResolvable())
                .as("LangfuseSpanProcessor should not be present when OTel integration is disabled")
                .isFalse();
    }
}
