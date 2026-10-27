package io.quarkiverse.langfuse.deployment.otel;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.semconv.incubating.GenAiIncubatingAttributes;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.runtime.otel.LangfuseSpanProcessor;
import io.quarkus.test.QuarkusExtensionTest;

/**
 * Verifies that the {@link LangfuseSpanProcessor} registered for {@code export-target=ALL} ships spans over HTTPS to a
 * Langfuse instance whose certificate is trusted through a named Quarkus TLS configuration.
 */
abstract class BaseHttpsSpanExportTests {
    static final String TLS_CONFIGURATION_NAME = "langfuse-test";
    static final String FAILURE_LOGGER = "io.quarkiverse.langfuse.runtime.otel.FailureLoggingSpanExporter";

    private static final String PUBLIC_KEY = "pk-lf-test";
    private static final String SECRET_KEY = "sk-lf-test";
    private static final String SPAN_NAME = "langfuse-https-export-test-chat";

    @Inject
    Tracer tracer;

    @Inject
    LangfuseSpanProcessor langfuseSpanProcessor;

    @Inject
    LangfuseConfig langfuseConfig;

    /**
     * A {@link QuarkusExtensionTest} talking to the given HTTPS server, with the TLS configuration
     * {@value #TLS_CONFIGURATION_NAME} trusting the server's certificate.
     */
    static QuarkusExtensionTest httpsExtensionTest(HttpsLangfuseServer langfuse) {
        return new QuarkusExtensionTest()
                .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                        .addClasses(BaseHttpsSpanExportTests.class, HttpsLangfuseServer.class))
                .setBeforeAllCustomizer(langfuse::start)
                .setAfterAllCustomizer(langfuse::stop)
                .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
                .overrideConfigKey("quarkus.tls.%s.trust-store.pem.certs".formatted(TLS_CONFIGURATION_NAME),
                        HttpsLangfuseServer.TRUST_CERT)
                .overrideRuntimeConfigKey(LangfuseConfig.BASE_URL_KEY, HttpsLangfuseServer.BASE_URL)
                .overrideRuntimeConfigKey(LangfuseConfig.PUBLIC_KEY, PUBLIC_KEY)
                .overrideRuntimeConfigKey(LangfuseConfig.SECRET_KEY, SECRET_KEY)
                .setLogRecordPredicate(logRecord -> FAILURE_LOGGER.equals(logRecord.getLoggerName())
                        && (logRecord.getLevel().intValue() >= Level.WARNING.intValue()))
                .assertLogRecords(logRecords -> assertThat(logRecords)
                        .as("A successful export must not log any failure")
                        .isEmpty());
    }

    @Test
    void genAiSpanExportedToLangfuseOverHttps() {
        assertThat(this.langfuseConfig.otel().tlsConfigurationName())
                .hasValue(TLS_CONFIGURATION_NAME);

        this.tracer.spanBuilder(SPAN_NAME)
                .setAttribute(GenAiIncubatingAttributes.GEN_AI_OPERATION_NAME, "chat")
                .startSpan()
                .end();

        assertThat(this.langfuseSpanProcessor.forceFlush().join(10, TimeUnit.SECONDS).isSuccess())
                .as("Langfuse span export over HTTPS should complete successfully")
                .isTrue();

        var expectedAuthorization = "Basic %s".formatted(Base64.getEncoder()
                .encodeToString("%s:%s".formatted(PUBLIC_KEY, SECRET_KEY).getBytes(StandardCharsets.UTF_8)));

        var langfuseExports = HttpsLangfuseServer.client().find(
                postRequestedFor(urlPathEqualTo(HttpsLangfuseServer.TRACES_PATH))
                        .withHeader("x-langfuse-ingestion-version", equalTo("4"))
                        .withHeader("Authorization", equalTo(expectedAuthorization))
                        .withHeader("Content-Type", equalTo("application/x-protobuf")));

        assertThat(langfuseExports)
                .as("Exactly one OTLP export should have reached Langfuse over HTTPS")
                .singleElement()
                .extracting(request -> new String(request.getBody(), StandardCharsets.ISO_8859_1))
                .asString()
                .contains(SPAN_NAME);
    }
}
