package io.quarkiverse.langfuse.deployment.otel;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;

/**
 * HTTPS export using the TLS configuration named by {@code quarkus.langfuse.otel.tls-configuration-name}.
 */
class LangfuseSpanExportTlsTests extends BaseHttpsSpanExportTests {
    private static final HttpsLangfuseServer LANGFUSE = new HttpsLangfuseServer();

    @RegisterExtension
    static final QuarkusExtensionTest extensionTest = httpsExtensionTest(LANGFUSE)
            .overrideRuntimeConfigKey("quarkus.langfuse.otel.tls-configuration-name", TLS_CONFIGURATION_NAME);
}
