package io.quarkiverse.langfuse.deployment.devservices.config;

import java.util.Map;

import com.langfuse.testcontainers.config.PostgresConfig;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface PostgresDevServicesConfig {
    /**
     * The container image name to use for the PostgreSQL database.
     */
    @WithDefault(PostgresConfig.DEFAULT_IMAGE)
    String imageName();

    /**
     * The database username.
     */
    @WithDefault("quarkus")
    String username();

    /**
     * The database password.
     */
    @WithDefault("quarkuspostgres")
    String password();

    /**
     * The database name.
     */
    @WithDefault("quarkus-langfuse")
    String databaseName();

    /**
     * Environment variables that are passed to the PostgreSQL container.
     */
    Map<String, String> containerEnv();
}
