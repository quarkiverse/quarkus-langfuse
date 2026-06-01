package io.quarkiverse.langfuse.deployment.devservices.config;

import com.langfuse.testcontainers.config.LangfuseContainerConfig;

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
     * Langfuse web server configuration.
     */
    LangfuseServerDevServicesConfig langfuse();

    /**
     * PostgreSQL database configuration.
     */
    PostgresDevServicesConfig postgres();

    /**
     * ClickHouse analytics database configuration.
     */
    ClickHouseDevServicesConfig clickhouse();

    /**
     * Redis cache configuration.
     */
    RedisDevServicesConfig redis();

    /**
     * MinIO object storage configuration.
     */
    MinIODevServicesConfig minio();

    /**
     * Langfuse worker configuration.
     */
    WorkerDevServicesConfig worker();

    /**
     * Converts this Quarkus configuration into a {@link LangfuseContainerConfig}
     * for the upstream testcontainers library.
     *
     * @return a new {@link LangfuseContainerConfig} built from the current configuration
     */
    default LangfuseContainerConfig toLangfuseContainerConfig() {
        var builder = LangfuseContainerConfig.builder();

        var lf = langfuse();
        var lfBuilder = builder.langfuse()
                .image(lf.imageName())
                .port(lf.port())
                .startupTimeout(lf.startupTimeout())
                .initProjectPublicKey(lf.username())
                .initProjectSecretKey(lf.password())
                .initOrgId(lf.initOrgId())
                .initOrgName(lf.initOrgName())
                .initProjectId(lf.initProjectId())
                .initProjectName(lf.initProjectName())
                .initUserEmail(lf.initUserEmail())
                .initUserName(lf.initUserName())
                .initUserPassword(lf.initUserPassword())
                .enableExperimentalFeatures(lf.enableExperimentalFeatures())
                .batchExportEnabled(lf.batchExportEnabled())
                .containerEnv(lf.containerEnv());

        lf.ingestionQueueDelay().ifPresent(lfBuilder::ingestionQueueDelay);
        lf.ingestionClickhouseWriteInterval().ifPresent(lfBuilder::ingestionClickhouseWriteInterval);
        lf.emailFromAddress().ifPresent(lfBuilder::emailFromAddress);
        lf.smtpConnectionUrl().ifPresent(lfBuilder::smtpConnectionUrl);

        var pg = postgres();
        builder.postgres()
                .image(pg.imageName())
                .username(pg.username())
                .password(pg.password())
                .databaseName(pg.databaseName())
                .containerEnv(pg.containerEnv());

        var ch = clickhouse();
        builder.clickhouse()
                .image(ch.imageName())
                .username(ch.username())
                .password(ch.password())
                .databaseName(ch.databaseName())
                .containerEnv(ch.containerEnv());

        var rd = redis();
        builder.redis()
                .image(rd.imageName())
                .password(rd.password())
                .tlsEnabled(rd.tlsEnabled())
                .containerEnv(rd.containerEnv());

        var mi = minio();
        builder.minio()
                .image(mi.imageName())
                .rootUser(mi.rootUser())
                .rootPassword(mi.rootPassword())
                .bucketName(mi.bucketName())
                .containerEnv(mi.containerEnv());

        var wk = worker();
        builder.worker()
                .image(wk.imageName())
                .containerEnv(wk.containerEnv());

        return builder.build();
    }
}
