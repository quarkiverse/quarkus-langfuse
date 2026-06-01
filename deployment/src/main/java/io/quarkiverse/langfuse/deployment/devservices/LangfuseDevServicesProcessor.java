package io.quarkiverse.langfuse.deployment.devservices;

import java.net.Socket;
import java.util.List;

import org.jboss.logging.Logger;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.deployment.config.LangfuseBuildTimeConfig;
import io.quarkiverse.langfuse.deployment.devservices.config.LangfuseDevServicesConfig;
import io.quarkus.deployment.IsProduction;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.DevServicesComposeProjectBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DockerStatusBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.dev.devservices.DevServicesConfig.Enabled;
import io.quarkus.devservices.common.ComposeLocator;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.LaunchMode;

@BuildSteps(onlyIfNot = IsProduction.class, onlyIf = Enabled.class)
public class LangfuseDevServicesProcessor {
    private static final Logger LOG = Logger.getLogger(LangfuseDevServicesProcessor.class);

    public static final String FEATURE = "langfuse-dev-service";
    public static final String PROVIDER = "langfuse";

    static final String DEV_SERVICE_LABEL = "quarkus-dev-service-langfuse";

    private static final ContainerLocator LANGFUSE_CONTAINER_LOCATOR = ContainerLocator
            .locateContainerWithLabels(com.langfuse.testcontainers.config.LangfuseConfig.DEFAULT_PORT, DEV_SERVICE_LABEL);

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void startLangfuseDevServices(
            LaunchModeBuildItem launchMode,
            DockerStatusBuildItem dockerStatusBuildItem,
            DevServicesComposeProjectBuildItem composeProjectBuildItem,
            LangfuseBuildTimeConfig langfuseBuildTimeConfig,
            BuildProducer<DevServicesResultBuildItem> devServicesResult) {

        var langfuseDevServicesConfig = langfuseBuildTimeConfig.devservices();

        if (!langfuseDevServicesDisabled(dockerStatusBuildItem, langfuseDevServicesConfig)) {
            var devServicesResultBuildItem = discoverRunningService(composeProjectBuildItem, langfuseDevServicesConfig,
                    launchMode.getLaunchMode());

            devServicesResult.produce(devServicesResultBuildItem);
        }
    }

    private static DevServicesResultBuildItem discoverRunningService(DevServicesComposeProjectBuildItem composeProjectBuildItem,
            LangfuseDevServicesConfig devServicesConfig,
            LaunchMode launchMode) {

        var langfuseContainerConfig = devServicesConfig.toLangfuseContainerConfig();

        return LANGFUSE_CONTAINER_LOCATOR
                .locateContainer(devServicesConfig.serviceName(), devServicesConfig.shared(), launchMode)
                .or(() -> ComposeLocator.locateContainer(composeProjectBuildItem,
                        List.of(devServicesConfig.langfuse().imageName()),
                        com.langfuse.testcontainers.config.LangfuseConfig.DEFAULT_PORT, launchMode, false))
                .map(containerAddress -> {
                    var config = QuarkusLangfuseContainer.getExposedConfig(devServicesConfig, containerAddress.getHost(),
                            containerAddress.getPort());

                    return DevServicesResultBuildItem.discovered()
                            .feature(FEATURE)
                            .containerId(containerAddress.getId())
                            .description("Langfuse")
                            .config(config)
                            .build();
                })
                .orElseGet(() -> DevServicesResultBuildItem.owned()
                        .feature(FEATURE)
                        .description("Langfuse")
                        .serviceName(devServicesConfig.serviceName())
                        .serviceConfig(langfuseContainerConfig)
                        .startable(() -> new QuarkusLangfuseContainer(devServicesConfig))
                        .postStartHook(LangfuseDevServicesProcessor::devServiceStarted)
                        .configProvider(QuarkusLangfuseContainer.getExposedConfig(devServicesConfig))
                        .build());
    }

    private static boolean langfuseDevServicesDisabled(DockerStatusBuildItem dockerStatusBuildItem,
            LangfuseDevServicesConfig devServicesConfig) {
        if (isLangfuseRunning()) {
            LOG.infof("Not starting Langfuse dev services container as it is already running on port %d",
                    com.langfuse.testcontainers.config.LangfuseConfig.DEFAULT_PORT);
            return true;
        }

        if (!devServicesConfig.enabled()) {
            // explicitly disabled
            LOG.info("Not starting devservices for langfuse as it has been disabled in the config");
            return true;
        }

        if (!dockerStatusBuildItem.isContainerRuntimeAvailable()) {
            LOG.warn("Please get a working container runtime");
            return true;
        }

        return false;
    }

    private static boolean isLangfuseRunning() {
        try (var s = new Socket("localhost", com.langfuse.testcontainers.config.LangfuseConfig.DEFAULT_PORT)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void devServiceStarted(QuarkusLangfuseContainer container) {
        LOG.infof(
                """

                        Dev Services for Langfuse started.
                          Other applications in dev mode will find it automatically.
                          For Quarkus applications in production mode, you can connect to this instance by starting you application with -D%s=%s
                        """,
                LangfuseConfig.BASE_URL_KEY, container.getConnectionInfo());
    }
}
