package io.quarkiverse.langfuse.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import jakarta.inject.Inject;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response.Status;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.model.HealthResponse;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusUnitTest;

class QuarkusLangfuseApiLogRequestsResponsesTests extends RequestResponseLoggingTests {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideRuntimeConfigKey(LangfuseConfig.BASE_URL_KEY, wiremockUrlForConfig())
            .overrideRuntimeConfigKey("quarkus.langfuse.log-requests", "true")
            .overrideRuntimeConfigKey("quarkus.langfuse.log-responses", "true")
            .overrideRuntimeConfigKey("quarkus.langfuse.username", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.password", "quarkus");

    @Inject
    LangfuseApi langfuseApi;

    @Test
    void requestResponseLogged() {
        assertThat(langfuseApi.health().healthHealth())
                .isNotNull()
                .extracting(HealthResponse::getStatus)
                .isEqualTo("ok");

        assertThat(LOG_HANDLER.getRecords())
                .hasSize(2)
                .satisfies(
                        logRecord -> assertThat(logRecord)
                                .isNotNull()
                                .extracting(
                                        LogRecord::getLevel,
                                        l -> Objects.toString(l.getParameters()[0]),
                                        l -> l.getParameters()[1])
                                .containsExactly(
                                        Level.INFO,
                                        HttpMethod.GET,
                                        resolvedWiremockUrl("/api/public/health")),
                        atIndex(0))
                .satisfies(
                        logRecord -> assertThat(logRecord)
                                .isNotNull()
                                .extracting(
                                        LogRecord::getLevel,
                                        l -> l.getParameters()[0])
                                .containsExactly(
                                        Level.INFO,
                                        Status.OK.getStatusCode()),
                        atIndex(1));
    }
}
