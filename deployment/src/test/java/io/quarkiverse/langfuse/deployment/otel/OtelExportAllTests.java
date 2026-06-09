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
public class OtelExportAllTests {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideRuntimeConfigKey("quarkus.langfuse.base-url", "http://localhost:3000")
            .overrideRuntimeConfigKey("quarkus.langfuse.username", "pk-lf-test")
            .overrideRuntimeConfigKey("quarkus.langfuse.password", "sk-lf-test");

    @Inject
    Instance<LangfuseSpanProcessor> spanProcessorInstance;

    @Test
    void otelConfigAutoSet() {
        var config = ConfigProvider.getConfig();

        assertThat(config.getValue(LangfuseOtelConfigSourceFactory.OTEL_ENDPOINT_KEY, String.class))
                .as("OTel endpoint should be auto-configured to Langfuse")
                .isEqualTo("http://localhost:4317/");
    }

    @Test
    void langfuseSpanProcessor() {
        assertThat(this.spanProcessorInstance.isResolvable())
                .as("LangfuseSpanProcessor should be present")
                .isTrue();
    }
}
