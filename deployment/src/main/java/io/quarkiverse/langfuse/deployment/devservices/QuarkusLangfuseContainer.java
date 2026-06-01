package io.quarkiverse.langfuse.deployment.devservices;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.jboss.logging.Logger;

import com.langfuse.testcontainers.LangfuseContainer;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.deployment.devservices.config.LangfuseDevServicesConfig;
import io.quarkus.deployment.builditem.Startable;

public class QuarkusLangfuseContainer extends LangfuseContainer implements Startable {
    private static final Logger LOG = Logger.getLogger(QuarkusLangfuseContainer.class);

    /**
     * Configuration key for the UI endpoint used by the Langfuse dev service
     */
    public static final String CONFIG_LANGFUSE_UI = LangfuseDevServicesProcessor.FEATURE + ".langfuse.ui";

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

    public QuarkusLangfuseContainer(LangfuseDevServicesConfig config) {
        super(config.toLangfuseContainerConfig());
        this.config = config;
    }

    @Override
    public String getConnectionInfo() {
        return getLangfuseUrl();
    }

    @Override
    public void close() {
        stop();
    }

    public static Map<String, String> getExposedConfig(LangfuseDevServicesConfig config, String host, int port) {
        var apiEndpoint = "http://%s:%d".formatted(host, port);

        return Map.of(
                CONFIG_LANGFUSE_PORT, Objects.toString(port),
                CONFIG_LANGFUSE_HTTP_SERVER, host,
                CONFIG_LANGFUSE_API_ENDPOINT, apiEndpoint,
                LangfuseConfig.BASE_URL_KEY, apiEndpoint,
                LangfuseConfig.USERNAME_KEY, config.langfuse().username(),
                LangfuseConfig.PASSWORD_KEY, config.langfuse().password());
    }

    public static Map<String, Function<QuarkusLangfuseContainer, String>> getExposedConfig(LangfuseDevServicesConfig config) {
        Function<QuarkusLangfuseContainer, String> apiEndpointFunction = QuarkusLangfuseContainer::getConnectionInfo;

        return Map.of(
                CONFIG_LANGFUSE_PORT, c -> Objects.toString(c.getPort()),
                CONFIG_LANGFUSE_HTTP_SERVER, QuarkusLangfuseContainer::getHost,
                CONFIG_LANGFUSE_API_ENDPOINT, apiEndpointFunction,
                LangfuseConfig.BASE_URL_KEY, apiEndpointFunction,
                LangfuseConfig.USERNAME_KEY, c -> config.langfuse().username(),
                LangfuseConfig.PASSWORD_KEY, c -> config.langfuse().password());
    }
}
