package io.quarkiverse.langfuse.deployment.otel;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;

/**
 * Verifies that when {@code quarkus.langfuse.otel.enabled} is set to {@code false},
 * the OTel OTLP exporter is not auto-configured to point at Langfuse, even though
 * the {@code quarkus-opentelemetry} extension is present and Langfuse properties are set.
 */
class LangfuseOtelDisabledTests extends BaseOtelDisabledTests {
    @RegisterExtension
    static final QuarkusExtensionTest extensionTest = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideConfigKey("quarkus.langfuse.otel.enabled", "false")
            .overrideRuntimeConfigKey("quarkus.langfuse.base-url", "http://localhost:3000")
            .overrideRuntimeConfigKey("quarkus.langfuse.public-key", "pk-lf-test")
            .overrideRuntimeConfigKey("quarkus.langfuse.secret-key", "sk-lf-test");
}
