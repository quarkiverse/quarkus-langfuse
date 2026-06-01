package io.quarkiverse.langfuse.deployment.devservices.config;

import java.util.Map;

import com.langfuse.testcontainers.config.MinIOConfig;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface MinIODevServicesConfig {
    /**
     * The container image name to use for the MinIO object storage.
     */
    @WithDefault(MinIOConfig.DEFAULT_IMAGE)
    String imageName();

    /**
     * The MinIO root user.
     */
    @WithDefault("quarkus")
    String rootUser();

    /**
     * The MinIO root password.
     */
    @WithDefault("quarkusminio")
    String rootPassword();

    /**
     * The name of the S3 bucket to create.
     */
    @WithDefault("quarkuslangfuse")
    String bucketName();

    /**
     * Environment variables that are passed to the MinIO container.
     */
    Map<String, String> containerEnv();
}
