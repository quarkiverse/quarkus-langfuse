package io.quarkiverse.langfuse.config;

import static io.quarkus.runtime.annotations.ConfigPhase.RUN_TIME;

import java.time.Duration;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigRoot(phase = RUN_TIME)
@ConfigMapping(prefix = "quarkus.langfuse")
public interface LangfuseConfig {
    /**
     * The key for the base url
     */
    String BASE_URL_KEY = "quarkus.langfuse.base-url";

    /**
     * The key for the username
     */
    String USERNAME_KEY = "quarkus.langfuse.username";

    /**
     * The key for the password
     */
    String PASSWORD_KEY = "quarkus.langfuse.password";

    /**
     * The base URL of the Langfuse server
     */
    String baseUrl();

    /**
     * Langfuse project public key, used for API authentication.
     * Found in the Langfuse dashboard under Settings &gt; API Keys.
     */
    String username();

    /**
     * Langfuse project secret key, used for API authentication.
     * Found in the Langfuse dashboard under Settings &gt; API Keys.
     */
    String password();

    /**
     * Timeout for Langfuse calls
     */
    @WithDefault("1m")
    Duration timeout();

    /**
     * Whether the Langfuse client should log requests
     */
    @WithDefault("false")
    Boolean logRequests();

    /**
     * Whether the Langfuse client should log responses
     */
    @WithDefault("false")
    Boolean logResponses();

    /**
     * Controls whether request/response bodies are pretty-printed if
     * {@link #logRequests()} or {@link #logResponses()} is set to {@code true}
     */
    @WithDefault("false")
    Boolean prettyPrint();

    /**
     * Timeout to establish a connection to Langfuse.
     * <p>
     * Defaults to {@code quarkus.langfuse.timeout}
     * </p>
     */
    @WithDefault("${quarkus.langfuse.timeout}")
    Duration connectTimeout();

    /**
     * Timeout for receiving a response from the Langfuse.
     * <p>
     * Defaults to {@code quarkus.langfuse.timeout}
     * </p>
     */
    @WithDefault("${quarkus.langfuse.timeout}")
    Duration readTimeout();

    /**
     * Retrieves the configured environment for the Langfuse client. This setting is used to
     * determine the operational environment in which the Langfuse client is running.
     *
     * @return The environment setting as a {@code String}, which defaults to {@code default}
     *         if not explicitly configured.
     */
    @WithDefault("default")
    String environment();

    /**
     * Provides the configuration related to OpenTelemetry for Langfuse integration.
     */
    LangfuseOtelConfig otel();
}
