package io.quarkiverse.langfuse.deployment.devservices.config;

import java.util.Map;

import com.langfuse.testcontainers.config.WorkerConfig;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface WorkerDevServicesConfig {
    /**
     * The container image name to use for the Langfuse worker.
     */
    @WithDefault(WorkerConfig.DEFAULT_IMAGE)
    String imageName();

    /**
     * Environment variables that are passed to the Langfuse worker container.
     */
    Map<String, String> containerEnv();
}
