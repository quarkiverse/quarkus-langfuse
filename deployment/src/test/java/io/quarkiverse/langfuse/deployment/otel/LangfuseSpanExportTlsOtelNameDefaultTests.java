package io.quarkiverse.langfuse.deployment.otel;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;

/**
 * HTTPS export where only {@code quarkus.otel.exporter.otlp.traces.tls-configuration-name} is set, verifying that
 * {@code quarkus.langfuse.otel.tls-configuration-name} defaults to it.
 */
class LangfuseSpanExportTlsOtelNameDefaultTests extends BaseHttpsSpanExportTests {
    private static final HttpsLangfuseServer LANGFUSE = new HttpsLangfuseServer();

    @RegisterExtension
    static final QuarkusExtensionTest extensionTest = httpsExtensionTest(LANGFUSE)
            .overrideRuntimeConfigKey("quarkus.otel.exporter.otlp.traces.tls-configuration-name", TLS_CONFIGURATION_NAME);
}
