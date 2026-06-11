package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApi;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Integration tests for the Health API.
 *
 */
@QuarkusTest
class HealthApiTest {

    @Inject
    LangfuseApi client;

    @Test
    void healthEndpointReturnsOk() {
        assertThat(client.health().healthHealth())
                .satisfies(health -> {
                    assertThat(health.getStatus()).isEqualTo("OK");
                    assertThat(health.getVersion()).isNotBlank().matches("\\d+\\.\\d+\\.\\d+.*");
                });
    }
}
