package io.quarkiverse.langfuse.runtime.otel;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import org.eclipse.microprofile.config.ConfigValue;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jboss.logging.Logger;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.config.PropertiesConfigSource;

/**
 * A {@link ConfigSourceFactory} that derives OpenTelemetry OTLP exporter configuration from
 * existing Langfuse connection properties ({@code quarkus.langfuse.base-url}, {@code username},
 * and {@code password}).
 * <p>
 * When all three Langfuse properties are present, this factory produces a {@link ConfigSource}
 * that sets the OTel OTLP endpoint, authorization headers, and traces protocol so that
 * traces are exported directly to the Langfuse OTLP ingestion API. When any property is
 * missing or blank the factory returns an empty list, leaving OTel configuration unchanged.
 * <p>
 * The produced config source uses a low {@link #ORDINAL ordinal} so that explicit user
 * configuration in {@code application.properties}, environment variables, or system properties
 * always takes precedence.
 *
 * @see LangfuseOtelConfigBuilder
 */
public class LangfuseOtelConfigSourceFactory implements ConfigSourceFactory {
    private static final Logger LOG = Logger.getLogger(LangfuseOtelConfigSourceFactory.class);

    /**
     * Config source ordinal. Set to 50, well below {@code application.properties} (250),
     * environment variables (300), and system properties (400) so that any explicit OTel
     * configuration by the user takes precedence over the auto-derived values.
     */
    public static final int ORDINAL = 50;

    /**
     * The name assigned to the {@link ConfigSource} returned by this factory, used for
     * identification in config diagnostics and logging.
     */
    public static final String CONFIG_SOURCE_NAME = "LangfuseOtelConfigSource";

    /**
     * Configuration key for the OTLP traces exporter protocol.
     */
    public static final String OTEL_TRACES_PROTOCOL_KEY = "quarkus.otel.exporter.otlp.traces.protocol";

    /**
     * Configuration key for the OTLP exporter endpoint URL.
     */
    public static final String OTEL_ENDPOINT_KEY = "quarkus.otel.exporter.otlp.endpoint";

    /**
     * Configuration key for the OTLP exporter request headers (used to carry the
     * {@code Authorization} header with Langfuse credentials).
     */
    public static final String OTEL_HEADERS_KEY = "quarkus.otel.exporter.otlp.headers";

    /**
     * The OTLP protocol value required by the Langfuse ingestion API.
     */
    public static final String TRACES_PROTOCOL_VALUE = "http/protobuf";

    /**
     * The path appended to the Langfuse base URL to form the OTLP ingestion endpoint.
     */
    public static final String OTEL_PATH = "/api/public/otel";

    /**
     * Reads {@code quarkus.langfuse.base-url}, {@code quarkus.langfuse.username}, and
     * {@code quarkus.langfuse.password} from the config context and, if all three are
     * present and non-blank, returns a single {@link ConfigSource} that provides the
     * derived OTel OTLP exporter properties. Returns an empty iterable otherwise.
     */
    @Override
    public Iterable<ConfigSource> getConfigSources(ConfigSourceContext context) {
        var baseUrl = resolveValue(context, LangfuseConfig.BASE_URL_KEY);
        var username = resolveValue(context, LangfuseConfig.USERNAME_KEY);
        var password = resolveValue(context, LangfuseConfig.PASSWORD_KEY);

        if (baseUrl.isEmpty() || username.isEmpty() || password.isEmpty()) {
            LOG.debug(
                    "Langfuse OTel auto-configuration skipped: one or more of quarkus.langfuse.base-url, username, or password is not set");
            return Collections.emptyList();
        }

        var endpoint = baseUrl
                .filter(url -> url.endsWith("/"))
                .map(url -> url + OTEL_PATH.substring(1))
                .orElseGet(() -> baseUrl.get() + OTEL_PATH);

        var credentials = Base64.getEncoder()
                .encodeToString("%s:%s".formatted(username.get(), password.get()).getBytes(StandardCharsets.UTF_8));

        var properties = Map.of(
                OTEL_TRACES_PROTOCOL_KEY, TRACES_PROTOCOL_VALUE,
                OTEL_ENDPOINT_KEY, endpoint,
                OTEL_HEADERS_KEY, "Authorization=Basic %s".formatted(credentials));

        LOG.infof("Langfuse OTel auto-configuration: setting OTel OTLP exporter endpoint to %s", endpoint);

        return List.of(new PropertiesConfigSource(properties, CONFIG_SOURCE_NAME, ORDINAL));
    }

    /**
     * Returns a low priority so this factory runs after higher-priority config sources
     * (such as DevServices and {@code application.properties}) have been established,
     * ensuring the Langfuse properties are available for reading.
     */
    @Override
    public OptionalInt getPriority() {
        return OptionalInt.of(Integer.MIN_VALUE + 100);
    }

    private static Optional<String> resolveValue(ConfigSourceContext context, String key) {
        return Optional.ofNullable(context.getValue(key))
                .map(ConfigValue::getValue)
                .filter(rawValue -> !rawValue.isBlank());
    }
}
