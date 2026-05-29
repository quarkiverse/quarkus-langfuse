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
    String BASE_URL_KEY = "quarkus.docling.base-url";

    /**
     * The key for the username
     */
    String USERNAME_KEY = "quarkus.docling.username";

    /**
     * The key for the password of the
     */
    String PASSWORD_KEY = "quarkus.docling.password";

    /**
     * The default base url of where docling is
     */
    String baseUrl();

    /**
     * Retrieves the username used for authentication.
     */
    String username();

    /**
     * Retrieves the password used for authentication.
     */
    String password();

    /**
     * Timeout for Docling calls
     */
    @WithDefault("1m")
    Duration timeout();

    /**
     * Whether the Docling client should log requests
     */
    @WithDefault("false")
    Boolean logRequests();

    /**
     * Whether the Docling client should log responses
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
}
