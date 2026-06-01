package io.quarkiverse.langfuse.deployment.devservices.config;

import java.util.Map;

import com.langfuse.testcontainers.config.ClickHouseConfig;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface ClickHouseDevServicesConfig {
    /**
     * The container image name to use for the ClickHouse analytics database.
     */
    @WithDefault(ClickHouseConfig.DEFAULT_IMAGE)
    String imageName();

    /**
     * The database username.
     */
    @WithDefault("quarkus")
    String username();

    /**
     * The database password.
     */
    @WithDefault("quarkusclickhouse")
    String password();

    /**
     * The database name.
     */
    @WithDefault("quarkusclickhouse")
    String databaseName();

    /**
     * Environment variables that are passed to the ClickHouse container.
     */
    Map<String, String> containerEnv();
}
