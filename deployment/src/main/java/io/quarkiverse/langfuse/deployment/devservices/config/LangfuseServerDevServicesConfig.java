package io.quarkiverse.langfuse.deployment.devservices.config;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import com.langfuse.testcontainers.config.LangfuseConfig;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface LangfuseServerDevServicesConfig {
    /**
     * The Langfuse release to run, shared by the Langfuse web server and the Langfuse worker.
     * <p>
     * Both containers must run the same release, so this is the only property needed to move them.
     * It is used by the default value of {@code image-name} here and of
     * {@code quarkus.langfuse.devservices.worker.image-name}. Setting either {@code image-name}
     * explicitly takes precedence over this property for that container.
     * <p>
     * The default is the release whose OpenAPI specification the client was generated from.
     */
    @WithDefault(LangfuseConfig.SERVER_VERSION)
    String imageVersion();

    /**
     * The container image name to use for the Langfuse web server.
     * <p>
     * Defaults to the official image at the release set by {@code image-version}.
     */
    @WithDefault(LangfuseConfig.IMAGE_REPOSITORY + ":${quarkus.langfuse.devservices.langfuse.image-version}")
    String imageName();

    /**
     * The port the Langfuse web server listens on inside the container.
     */
    @WithDefault("" + LangfuseConfig.DEFAULT_PORT)
    int port();

    /**
     * The publicKey used for authentication with the Langfuse API.
     * <p>
     * This maps to the Langfuse project public key used to initialize the dev instance.
     * </p>
     */
    @WithDefault("quarkus")
    String publicKey();

    /**
     * The secretKey used for authentication with the Langfuse API.
     * <p>
     * This maps to the Langfuse project secret key used to initialize the dev instance.
     * </p>
     */
    @WithDefault("quarkuslangfuse")
    String secretKey();

    /**
     * Maximum duration to wait for the Langfuse container to start up.
     */
    @WithDefault("${quarkus.devservices.timeout:PT3M}")
    Duration startupTimeout();

    /**
     * The organization ID to initialize in the Langfuse instance.
     */
    @WithDefault("quarkus")
    String initOrgId();

    /**
     * The organization name to initialize in the Langfuse instance.
     */
    @WithDefault("Quarkus Dev Org")
    String initOrgName();

    /**
     * The project ID to initialize in the Langfuse instance.
     */
    @WithDefault("quarkus-dev-project")
    String initProjectId();

    /**
     * The project name to initialize in the Langfuse instance.
     */
    @WithDefault("quarkus-dev")
    String initProjectName();

    /**
     * The user email to initialize in the Langfuse instance.
     */
    @WithDefault("quarkus@quarkus.io")
    String initUserEmail();

    /**
     * The user name to initialize in the Langfuse instance.
     */
    @WithDefault("quarkus")
    String initUserName();

    /**
     * The user secretKey to initialize in the Langfuse instance.
     */
    @WithDefault("quarkuslangfuse")
    String initUserPassword();

    /**
     * Whether to enable experimental features in Langfuse.
     */
    @WithDefault("false")
    boolean enableExperimentalFeatures();

    /**
     * Whether to enable batch export in Langfuse.
     */
    @WithDefault("false")
    boolean batchExportEnabled();

    /**
     * The delay between ingestion queue processing cycles.
     */
    Optional<Duration> ingestionQueueDelay();

    /**
     * The interval between ClickHouse write flushes for ingested events.
     */
    Optional<Duration> ingestionClickhouseWriteInterval();

    /**
     * The email "from" address for Langfuse email notifications.
     */
    Optional<String> emailFromAddress();

    /**
     * The SMTP connection URL for Langfuse email notifications.
     */
    Optional<String> smtpConnectionUrl();

    /**
     * Environment variables that are passed to the Langfuse web server container.
     */
    Map<String, String> containerEnv();
}
