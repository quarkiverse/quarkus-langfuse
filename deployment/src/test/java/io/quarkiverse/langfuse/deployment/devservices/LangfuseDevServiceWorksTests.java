package io.quarkiverse.langfuse.deployment.devservices;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.model.HealthResponse;

import io.quarkus.test.QuarkusUnitTest;

class LangfuseDevServiceWorksTests {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideRuntimeConfigKey("quarkus.langfuse.log-requests", "true")
            .overrideRuntimeConfigKey("quarkus.langfuse.log-responses", "true");

    @Inject
    LangfuseApi langfuseApi;

    @Test
    void health() {
        assertThat(langfuseApi.health().healthHealth())
                .isNotNull()
                .extracting(HealthResponse::getStatus)
                .isEqualTo("OK");
    }

    @Test
    void healthAsync() {
        assertThat(langfuseApi.asyncHealth().healthHealth())
                .succeedsWithin(Duration.ofSeconds(5))
                .isNotNull()
                .extracting(HealthResponse::getStatus)
                .isEqualTo("OK");
    }

    // @TODO This is where we need to add some tests for other operations...
}
