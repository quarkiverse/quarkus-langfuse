package io.quarkiverse.langfuse.deployment.devservices.config;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface LangfuseDevServicesConfig {
    /**
     * If DevServices has been explicitly enabled or disabled. DevServices are generally enabled
     * by default, unless there is an existing configuration present.
     * <p>
     * When DevServices is enabled, Quarkus will attempt to automatically configure and start a
     * Langfuse server when running in Dev or Test mode.
     * </p>
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * Indicates if the Langfuse server managed by Quarkus Dev Services is shared.
     * When shared, Quarkus looks for running containers using label-based service discovery.
     * If a matching container is found, it is used, and so a second one is not started.
     * Otherwise, Dev Services for Langfuse starts a new container.
     * <p>
     * The discovery uses the {@code quarkus-dev-service-langfuse} label.
     * The value is configured using the {@code service-name} property.
     * <p>
     * Container sharing is only used in dev mode.
     */
    @WithDefault("true")
    boolean shared();

    /**
     * The value of the {@code quarkus-dev-service-langfuse} label attached to the started container.
     * This property is used when {@code shared} is set to {@code true}.
     * In this case, before starting a container, Dev Services for Langfuse looks for a container with the
     * {@code quarkus-dev-service-langfuse} label
     * set to the configured value. If found, it will use this container instead of starting a new one. Otherwise, it
     * starts a new container with the {@code quarkus-dev-service-langfuse} label set to the specified value.
     * <p>
     * This property is used when you need multiple shared Langfuse servers.
     */
    @WithDefault("langfuse")
    String serviceName();

    /**
     * Retrieves the username used for authentication.
     */
    String username();

    /**
     * Retrieves the password used for authentication.
     */
    String password();
}
