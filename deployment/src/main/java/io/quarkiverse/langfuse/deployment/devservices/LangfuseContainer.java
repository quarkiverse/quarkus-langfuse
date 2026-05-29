package io.quarkiverse.langfuse.deployment.devservices;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.jboss.logging.Logger;

import io.quarkus.deployment.builditem.Startable;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.deployment.devservices.config.LangfuseDevServicesConfig;

public class LangfuseContainer implements Startable {
    private static final Logger LOG = Logger.getLogger(LangfuseContainer.class);
    public static final int DEFAULT_LANGFUSE_PORT = 3000;

    /**
     * Configuration key for the UI endpoint used by the Docling dev service
     */
    public static final String CONFIG_LANGFUSE_UI = LangfuseDevServicesProcessor.FEATURE + ".langfuse.ui";;

    /**
     * Configuration key for the port number used by the Langfuse dev service
     */
    public static final String CONFIG_LANGFUSE_PORT = LangfuseDevServicesProcessor.FEATURE + ".langfuse.port";

    /**
     * Configuration key for the host name used by the Langfuse dev service
     */
    public static final String CONFIG_LANGFUSE_HTTP_SERVER = LangfuseDevServicesProcessor.FEATURE + ".langfuse.host";

    /**
     * Configuration key for the API endpoint used by the Langfuse dev service
     */
    public static final String CONFIG_LANGFUSE_API_ENDPOINT = LangfuseDevServicesProcessor.FEATURE
            + ".langfuse.api.endpoint";

    private final LangfuseDevServicesConfig config;

    public LangfuseContainer(LangfuseDevServicesConfig config) {
        this.config = config;
    }

    @Override
    public void start() {

    }

    @Override
    public String getConnectionInfo() {
        return "";
    }

    @Override
    public String getContainerId() {
        return "";
    }

    @Override
    public void close() throws IOException {

    }

    public static Map<String, String> getExposedConfig(LangfuseDevServicesConfig config, String host, int port) {
        var apiEndpoint = "http://%s:%d".formatted(host, port);

        return Map.of(
                CONFIG_LANGFUSE_PORT, Objects.toString(port),
                CONFIG_LANGFUSE_HTTP_SERVER, host,
                CONFIG_LANGFUSE_API_ENDPOINT, apiEndpoint,
                LangfuseConfig.BASE_URL_KEY, apiEndpoint,
                LangfuseConfig.USERNAME_KEY, config.username(),
                LangfuseConfig.PASSWORD_KEY, config.password());
    }

    public static Map<String, Function<LangfuseContainer, String>> getExposedConfig(LangfuseDevServicesConfig config) {
        Function<LangfuseContainer, String> apiEndpointFunction = LangfuseContainer::getConnectionInfo;

        return Map.of(
                CONFIG_LANGFUSE_PORT, c -> Objects.toString(c.getPort()),
                CONFIG_LANGFUSE_HTTP_SERVER, LangfuseContainer::getHost,
                CONFIG_LANGFUSE_API_ENDPOINT, apiEndpointFunction,
                LangfuseConfig.BASE_URL_KEY, apiEndpointFunction,
                LangfuseConfig.USERNAME_KEY, c -> config.username(),
                LangfuseConfig.PASSWORD_KEY, c -> config.password());
    }

    private String getHost() {
        return null;
    }

    private int getPort() {
        return DEFAULT_LANGFUSE_PORT;
    }
}
