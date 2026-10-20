package io.quarkiverse.langfuse.deployment.devservices.config;

import java.util.Map;

import com.langfuse.testcontainers.config.WorkerConfig;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface WorkerDevServicesConfig {
    /**
     * The container image name to use for the Langfuse worker.
     * <p>
     * Defaults to the official worker image at the release set by
     * {@code quarkus.langfuse.devservices.langfuse.image-version}, so the worker always runs the same
     * release as the web server.
     */
    @WithDefault(WorkerConfig.IMAGE_REPOSITORY + ":${quarkus.langfuse.devservices.langfuse.image-version}")
    String imageName();

    /**
     * Environment variables that are passed to the Langfuse worker container.
     */
    Map<String, String> containerEnv();
}
