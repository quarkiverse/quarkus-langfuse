package io.quarkiverse.langfuse.deployment.devservices.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.langfuse.testcontainers.config.LangfuseConfig;

import io.quarkus.runtime.configuration.DurationConverter;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfigBuilder;

class ImageVersionConfigTests {
    private static LangfuseDevServicesConfig config(Map<String, String> properties) {
        return new SmallRyeConfigBuilder()
                .addDefaultInterceptors()
                .withConverter(Duration.class, 100, new DurationConverter())
                .withMapping(LangfuseDevServicesConfig.class, "quarkus.langfuse.devservices")
                .withSources(new PropertiesConfigSource(properties, "test", 100))
                .build()
                .getConfigMapping(LangfuseDevServicesConfig.class, "quarkus.langfuse.devservices");
    }

    @Test
    void defaultsToTheVersionTheClientWasGeneratedFrom() {
        var config = config(Map.of());

        assertThat(config.langfuse().imageVersion())
                .isEqualTo(LangfuseConfig.SERVER_VERSION);

        assertThat(config.langfuse().imageName())
                .isEqualTo("docker.io/langfuse/langfuse:" + LangfuseConfig.SERVER_VERSION);

        assertThat(config.worker().imageName())
                .isEqualTo("docker.io/langfuse/langfuse-worker:" + LangfuseConfig.SERVER_VERSION);
    }

    @Test
    void imageVersionMovesTheServerAndTheWorkerTogether() {
        var config = config(Map.of("quarkus.langfuse.devservices.langfuse.image-version", "4.55.0"));

        assertThat(config.langfuse().imageName())
                .isEqualTo("docker.io/langfuse/langfuse:4.55.0");

        assertThat(config.worker().imageName())
                .isEqualTo("docker.io/langfuse/langfuse-worker:4.55.0");
    }

    @Test
    void explicitImageNamesTakePrecedenceOverImageVersion() {
        var config = config(Map.of(
                "quarkus.langfuse.devservices.langfuse.image-version", "4.55.0",
                "quarkus.langfuse.devservices.langfuse.image-name", "registry.example.com/lf/langfuse:custom",
                "quarkus.langfuse.devservices.worker.image-name", "registry.example.com/lf/worker:custom"));

        assertThat(config.langfuse().imageName())
                .isEqualTo("registry.example.com/lf/langfuse:custom");

        assertThat(config.worker().imageName())
                .isEqualTo("registry.example.com/lf/worker:custom");
    }
}
