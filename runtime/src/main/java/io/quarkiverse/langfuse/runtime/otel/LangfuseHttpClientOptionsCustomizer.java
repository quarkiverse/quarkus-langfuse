package io.quarkiverse.langfuse.runtime.otel;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;

import org.jboss.logging.Logger;

import io.quarkiverse.langfuse.config.LangfuseOtelConfig;
import io.quarkiverse.langfuse.config.LangfuseOtelConfig.ProxyOptionsConfig;
import io.quarkus.tls.TlsConfiguration;
import io.quarkus.tls.TlsConfigurationRegistry;
import io.quarkus.tls.runtime.config.TlsConfigUtils;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.ProxyOptions;

/**
 * Customizes the Vert.x {@link HttpClientOptions} of the dedicated Langfuse OTLP exporter.
 * <p>
 * For {@code https} URLs this enables SSL and ALPN and applies a {@link TlsConfiguration} from the Quarkus TLS registry:
 * the one named by {@link LangfuseOtelConfig#tlsConfigurationName()} if set, otherwise the registry's default
 * configuration. No TLS settings are applied to {@code http} URLs and the TLS registry is not consulted for them.
 * </p>
 * <p>
 * The TLS logic is delegated to {@link TlsConfigUtils#configure(HttpClientOptions, TlsConfiguration)}, which has the
 * same signature on Quarkus 3.x and 4.x, so this class works on both lines.
 * </p>
 * <p>
 * If {@link ProxyOptionsConfig#enabled()} is set, a proxy is configured for both {@code http} and {@code https} URLs,
 * with the same semantics as the Quarkus OpenTelemetry exporter: the explicitly configured proxy host, otherwise the
 * JDK {@code https.proxy*} / {@code http.proxy*} system properties.
 * </p>
 */
final class LangfuseHttpClientOptionsCustomizer implements Consumer<HttpClientOptions> {
    private static final Logger LOG = Logger.getLogger(LangfuseHttpClientOptionsCustomizer.class);
    private static final String HTTPS_SCHEME = "https";
    private static final String NO_PROXY_HOST = "none";

    private final boolean isHttps;
    private final Optional<TlsConfiguration> tlsConfiguration;
    private final ProxyOptionsConfig proxyConfig;

    LangfuseHttpClientOptionsCustomizer(URI baseUri, LangfuseOtelConfig config, TlsConfigurationRegistry registry) {
        this.isHttps = HTTPS_SCHEME.equalsIgnoreCase(baseUri.getScheme());
        this.tlsConfiguration = this.isHttps ? resolveTlsConfiguration(config, registry) : Optional.empty();
        this.proxyConfig = config.proxyOptions();
    }

    private static Optional<TlsConfiguration> resolveTlsConfiguration(LangfuseOtelConfig config,
            TlsConfigurationRegistry registry) {
        return TlsConfiguration.from(registry, config.tlsConfigurationName())
                .or(registry::getDefault);
    }

    @Override
    public void accept(HttpClientOptions options) {
        if (this.isHttps) {
            configureTls(options);
        }

        if (this.proxyConfig.enabled()) {
            configureProxy(options);
        }
    }

    private void configureTls(HttpClientOptions options) {
        options.setSsl(true);
        this.tlsConfiguration.ifPresent(configuration -> TlsConfigUtils.configure(options, configuration));

        // TlsConfigUtils copies the ALPN flag from the TLS configuration, so it has to be forced afterwards
        options.setUseAlpn(true);

        this.tlsConfiguration
                .filter(TlsConfiguration::isTrustAll)
                .ifPresent(tlcConfig -> {
                    LOG.warn("Langfuse OpenTelemetry exporter TLS is configured with trustAll=true."
                            + " This disables certificate and hostname verification, making the connection vulnerable to"
                            + " MITM attacks. This should not be used in production.");
                    options.setTrustAll(true);
                    options.setVerifyHost(false);
                });
    }

    private void configureProxy(HttpClientOptions options) {
        this.proxyConfig.host()
                .map(this::createConfiguredProxyOptions)
                .or(this::createProxyOptionsFromJdkSystemProperties)
                .ifPresent(options::setProxyOptions);
    }

    private ProxyOptions createConfiguredProxyOptions(String host) {
        var proxyOptions = new ProxyOptions().setHost(host);
        this.proxyConfig.port().ifPresent(proxyOptions::setPort);
        this.proxyConfig.username().ifPresent(proxyOptions::setUsername);
        this.proxyConfig.password().ifPresent(proxyOptions::setPassword);

        return proxyOptions;
    }

    private Optional<ProxyOptions> createProxyOptionsFromJdkSystemProperties() {
        var protocol = this.isHttps ? JdkProxyProtocol.HTTPS : JdkProxyProtocol.HTTP;

        return protocol.systemProperty("proxyHost")
                .filter(host -> !NO_PROXY_HOST.equals(host))
                .map(host -> createProxyOptionsFromJdkSystemProperties(protocol, host));
    }

    private static ProxyOptions createProxyOptionsFromJdkSystemProperties(JdkProxyProtocol protocol, String host) {
        var port = protocol.systemProperty("proxyPort")
                .map(Integer::parseInt)
                .orElse(protocol.defaultPort());

        var proxyOptions = new ProxyOptions()
                .setHost(host)
                .setPort(port);

        protocol.systemProperty("proxyUser")
                .filter(user -> !user.isBlank())
                .ifPresent(proxyOptions::setUsername);

        protocol.systemProperty("proxyPassword")
                .filter(password -> !password.isBlank())
                .ifPresent(proxyOptions::setPassword);

        return proxyOptions;
    }

    private enum JdkProxyProtocol {
        HTTP("http", 80),
        HTTPS("https", 443);

        private final String propertyPrefix;
        private final int defaultPort;

        JdkProxyProtocol(String propertyPrefix, int defaultPort) {
            this.propertyPrefix = propertyPrefix;
            this.defaultPort = defaultPort;
        }

        Optional<String> systemProperty(String name) {
            return Optional.ofNullable(System.getProperty("%s.%s".formatted(this.propertyPrefix, name)));
        }

        int defaultPort() {
            return this.defaultPort;
        }
    }
}
