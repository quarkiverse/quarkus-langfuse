package io.quarkiverse.langfuse.deployment.otel;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.langfuse.runtime.otel.LangfuseSpanProcessor;
import io.quarkus.test.QuarkusExtensionTest;

/**
 * Verifies that when {@code quarkus.otel.sdk.disabled} is set to {@code true},
 * the {@link LangfuseSpanProcessor} bean is registered but is a no-op, even though the
 * Langfuse OTel integration itself is enabled.
 */
public class OtelSdkDisabledTests {
    @RegisterExtension
    static final QuarkusExtensionTest extensionTest = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideRuntimeConfigKey("quarkus.otel.sdk.disabled", "true")
            .overrideRuntimeConfigKey("quarkus.langfuse.base-url", "http://localhost:3000")
            .overrideRuntimeConfigKey("quarkus.langfuse.public-key", "pk-lf-test")
            .overrideRuntimeConfigKey("quarkus.langfuse.secret-key", "sk-lf-test");

    @Inject
    Instance<LangfuseSpanProcessor> spanProcessorInstance;

    @Test
    void langfuseSpanProcessorIsNoop() {
        assertThat(this.spanProcessorInstance.isResolvable())
                .as("LangfuseSpanProcessor bean should be registered")
                .isTrue();

        assertThat(this.spanProcessorInstance.get().isNoop())
                .as("LangfuseSpanProcessor should be a no-op when the OTel SDK is disabled")
                .isTrue();
    }
}
