package io.quarkiverse.langfuse.deployment.otel;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.semconv.incubating.GenAiIncubatingAttributes;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.deployment.WiremockAware;
import io.quarkiverse.langfuse.runtime.otel.LangfuseSpanProcessor;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Verifies that the {@link LangfuseSpanProcessor} registered for {@code export-target=ALL} actually ships spans to the
 * Langfuse OTLP ingestion endpoint over the {@code quarkus-opentelemetry} Vert.x sender, with the headers Langfuse
 * requires.
 */
class LangfuseSpanExportTests extends WiremockAware {
    private static final String PUBLIC_KEY = "pk-lf-test";
    private static final String SECRET_KEY = "sk-lf-test";
    private static final String TRACES_PATH = "/api/public/otel/v1/traces";
    private static final String SPAN_NAME = "langfuse-export-test-chat";

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideRuntimeConfigKey(LangfuseConfig.BASE_URL_KEY, wiremockUrlForConfig())
            .overrideRuntimeConfigKey(LangfuseConfig.PUBLIC_KEY, PUBLIC_KEY)
            .overrideRuntimeConfigKey(LangfuseConfig.SECRET_KEY, SECRET_KEY)
            .overrideRuntimeConfigKey("quarkus.otel.exporter.otlp.memory-mode", "reusable-data");

    @Inject
    Tracer tracer;

    @Inject
    LangfuseSpanProcessor langfuseSpanProcessor;

    @BeforeEach
    void resetWiremockRequests() {
        resetRequests();
    }

    @Test
    void genAiSpanExportedToLangfuse() {
        tracer.spanBuilder(SPAN_NAME)
                .setAttribute(GenAiIncubatingAttributes.GEN_AI_OPERATION_NAME, "chat")
                .startSpan()
                .end();

        assertThat(langfuseSpanProcessor.forceFlush().join(10, TimeUnit.SECONDS).isSuccess())
                .as("Langfuse span export should complete successfully")
                .isTrue();

        var expectedAuthorization = "Basic %s".formatted(Base64.getEncoder()
                .encodeToString("%s:%s".formatted(PUBLIC_KEY, SECRET_KEY).getBytes(StandardCharsets.UTF_8)));

        var langfuseExports = wiremock().find(
                postRequestedFor(urlPathEqualTo(TRACES_PATH))
                        .withHeader("x-langfuse-ingestion-version", equalTo("4"))
                        .withHeader("Authorization", equalTo(expectedAuthorization))
                        .withHeader("Content-Type", equalTo("application/x-protobuf")));

        assertThat(langfuseExports)
                .as("Exactly one OTLP export should have reached Langfuse")
                .singleElement()
                .extracting(request -> new String(request.getBody(), StandardCharsets.ISO_8859_1))
                .asString()
                .contains(SPAN_NAME);
    }
}
