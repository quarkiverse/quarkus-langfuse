package io.quarkiverse.langfuse.deployment.devservices;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

import io.quarkiverse.langfuse.LangfuseClient;

import com.langfuse.api.model.HealthResponse;

class LangfuseDevServiceWorksTests {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideRuntimeConfigKey("quarkus.langfuse.log-requests", "true")
            .overrideRuntimeConfigKey("quarkus.langfuse.log-responses", "true");

    @Inject
    LangfuseClient langfuseClient;

    @Test
    void hello() {
        assertThat(langfuseClient.healthHealth())
                .isNotNull()
                .extracting(HealthResponse::getStatus)
                .isEqualTo("OK");
    }

    // @TODO This is where we need to add some tests for other operations...
}
