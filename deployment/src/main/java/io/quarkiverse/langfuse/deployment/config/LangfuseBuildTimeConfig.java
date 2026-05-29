package io.quarkiverse.langfuse.deployment.config;

import static io.quarkus.runtime.annotations.ConfigPhase.BUILD_TIME;

import io.quarkiverse.langfuse.deployment.devservices.config.LangfuseDevServicesConfig;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigRoot(phase = BUILD_TIME)
@ConfigMapping(prefix = "quarkus.langfuse")
public interface LangfuseBuildTimeConfig {
    /**
     * Dev services configuration.
     */
    LangfuseDevServicesConfig devservices();
}
