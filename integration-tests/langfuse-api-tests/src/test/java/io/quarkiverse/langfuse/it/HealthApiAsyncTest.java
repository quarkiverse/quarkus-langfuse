package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApi;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Async integration tests for the Health API.
 *
 */
@QuarkusTest
class HealthApiAsyncTest {

    @Inject
    LangfuseApi client;

    @Test
    void healthEndpointReturnsOk() {
        assertThat(client.asyncHealth().healthHealth())
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(health -> {
                    assertThat(health.getStatus())
                            .isEqualTo("OK");

                    assertThat(health.getVersion())
                            .isNotBlank();
                });
    }
}
