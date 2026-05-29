package io.quarkiverse.langfuse.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class NoLangfuseBaseUrlSetTests {
    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .assertException(t -> assertThat(t)
                    .isNotNull()
                    .hasMessageContaining("Configuration validation failed")
                    .hasMessageContaining(
                            "The config property quarkus.langfuse.base-url is required but it could not be found"));

    @Test
    void test() {
        fail("Should not be reached");
    }
}
