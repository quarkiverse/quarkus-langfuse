package io.quarkiverse.langfuse.deployment.otel;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.langfuse.runtime.otel.LangfuseOtelConfigSourceFactory;
import io.quarkiverse.langfuse.runtime.otel.LangfuseSpanProcessor;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Verifies that when {@code quarkus.langfuse.otel.enabled} is set to {@code false},
 * the OTel OTLP exporter is not auto-configured to point at Langfuse, even though
 * the {@code quarkus-opentelemetry} extension is present and Langfuse properties are set.
 */
public class OtelDisabledTests {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideConfigKey("quarkus.langfuse.otel.enabled", "false")
            .overrideRuntimeConfigKey("quarkus.langfuse.base-url", "http://localhost:3000")
            .overrideRuntimeConfigKey("quarkus.langfuse.username", "pk-lf-test")
            .overrideRuntimeConfigKey("quarkus.langfuse.password", "sk-lf-test");

    @Inject
    Instance<LangfuseSpanProcessor> spanProcessorInstance;

    @Test
    void otelConfigNotAutoSet() {
        var config = ConfigProvider.getConfig();

        assertThat(config.getOptionalValue(LangfuseOtelConfigSourceFactory.OTEL_ENDPOINT_KEY, String.class)
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
