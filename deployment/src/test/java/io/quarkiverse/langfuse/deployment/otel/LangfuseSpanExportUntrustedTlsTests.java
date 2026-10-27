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
 * HTTPS export to a Langfuse instance whose self-signed certificate is not trusted by any TLS configuration: the export
 * must fail and exactly one warning naming the target URL and carrying the cause must be logged.
 */
class LangfuseSpanExportUntrustedTlsTests {
    private static final String FAILURE_LOGGER = "io.quarkiverse.langfuse.runtime.otel.FailureLoggingSpanExporter";
    private static final HttpsLangfuseServer LANGFUSE = new HttpsLangfuseServer();

    @RegisterExtension
    static final QuarkusExtensionTest extensionTest = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(HttpsLangfuseServer.class))
            .setBeforeAllCustomizer(LANGFUSE::start)
            .setAfterAllCustomizer(LANGFUSE::stop)
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
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
                    .contains(HttpsLangfuseServer.TRACES_PATH)
                    .contains("unable to find valid certification path"));

    @Inject
    Tracer tracer;

    @Inject
    LangfuseSpanProcessor langfuseSpanProcessor;

    @Test
    void untrustedCertificateFailsExportAndLogsWarning() {
        this.tracer.spanBuilder("langfuse-untrusted-https-export-test-chat")
                .setAttribute(GenAiIncubatingAttributes.GEN_AI_OPERATION_NAME, "chat")
                .startSpan()
                .end();

        this.langfuseSpanProcessor.forceFlush()
                .join(15, TimeUnit.SECONDS);

        assertThat(HttpsLangfuseServer.client().find(postRequestedFor(urlPathEqualTo(HttpsLangfuseServer.TRACES_PATH))))
                .as("No OTLP export should reach Langfuse when its certificate is not trusted")
                .isEmpty();
    }
}
