package io.quarkiverse.langfuse.deployment.devservices.config;

import java.util.Map;

import com.langfuse.testcontainers.config.RedisConfig;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface RedisDevServicesConfig {
    /**
     * The container image name to use for the Redis cache.
     */
    @WithDefault(RedisConfig.DEFAULT_IMAGE)
    String imageName();

    /**
     * The Redis password.
     */
    @WithDefault("quarkusredis")
    String password();

    /**
     * Whether TLS is enabled for the Redis connection.
     */
    @WithDefault("false")
    boolean tlsEnabled();

    /**
     * Environment variables that are passed to the Redis container.
     */
    Map<String, String> containerEnv();
}
