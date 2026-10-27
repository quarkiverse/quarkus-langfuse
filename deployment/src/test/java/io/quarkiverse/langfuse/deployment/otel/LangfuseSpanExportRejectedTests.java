package io.quarkiverse.langfuse.deployment.otel;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import jakarta.inject.Inject;

import org.jboss.logmanager.ExtLogRecord;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.semconv.incubating.GenAiIncubatingAttributes;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.runtime.otel.LangfuseSpanProcessor;
import io.quarkus.test.QuarkusExtensionTest;

/**
 * HTTPS export over a trusted connection to a Langfuse instance that rejects the request with {@code 400 Bad Request}
 * (the symptom reported in issue #158): exactly one warning naming the target URL must be logged.
 */
class LangfuseSpanExportRejectedTests {
    private static final String FAILURE_LOGGER = "io.quarkiverse.langfuse.runtime.otel.FailureLoggingSpanExporter";
    private static final String TLS_CONFIGURATION_NAME = "langfuse-test";
    private static final HttpsLangfuseServer LANGFUSE = new HttpsLangfuseServer(400);

    @RegisterExtension
    static final QuarkusExtensionTest extensionTest = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(HttpsLangfuseServer.class))
            .setBeforeAllCustomizer(LANGFUSE::start)
            .setAfterAllCustomizer(LANGFUSE::stop)
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideConfigKey("quarkus.tls.%s.trust-store.pem.certs".formatted(TLS_CONFIGURATION_NAME),
                    HttpsLangfuseServer.TRUST_CERT)
            .overrideRuntimeConfigKey("quarkus.langfuse.otel.tls-configuration-name", TLS_CONFIGURATION_NAME)
            .overrideRuntimeConfigKey(LangfuseConfig.BASE_URL_KEY, HttpsLangfuseServer.BASE_URL)
            .overrideRuntimeConfigKey(LangfuseConfig.PUBLIC_KEY, "pk-lf-test")
            .overrideRuntimeConfigKey(LangfuseConfig.SECRET_KEY, "sk-lf-test")
            .setLogRecordPredicate(logRecord -> FAILURE_LOGGER.equals(logRecord.getLoggerName())
                    && (logRecord.getLevel().intValue() >= Level.WARNING.intValue()))
            .assertLogRecords(logRecords -> assertThat(logRecords)
                    .singleElement()
                    .satisfies(logRecord -> assertThat(logRecord.getLevel()).isEqualTo(Level.WARNING))
                    .extracting(logRecord -> ((ExtLogRecord) logRecord).getFormattedMessage())
                    .asString()
                    .startsWith("Failed to export 1 span(s) to Langfuse at https://localhost:")
                    .contains("%s: server responded with HTTP 400".formatted(HttpsLangfuseServer.TRACES_PATH)));

    @Inject
    Tracer tracer;

    @Inject
    LangfuseSpanProcessor langfuseSpanProcessor;

    @Test
    void rejectedExportLogsWarning() {
        this.tracer.spanBuilder("langfuse-rejected-https-export-test-chat")
                .setAttribute(GenAiIncubatingAttributes.GEN_AI_OPERATION_NAME, "chat")
                .startSpan()
                .end();

        this.langfuseSpanProcessor.forceFlush()
                .join(15, TimeUnit.SECONDS);

        assertThat(HttpsLangfuseServer.client().find(postRequestedFor(urlPathEqualTo(HttpsLangfuseServer.TRACES_PATH))))
                .as("The OTLP export should have reached Langfuse exactly once before being rejected")
                .hasSize(1);
    }
}
