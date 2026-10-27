package io.quarkiverse.langfuse.runtime.otel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkiverse.langfuse.config.LangfuseOtelConfig;
import io.quarkiverse.langfuse.config.LangfuseOtelConfig.ProxyOptionsConfig;
import io.quarkus.tls.TlsConfiguration;
import io.quarkus.tls.TlsConfigurationRegistry;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;
import io.vertx.core.net.SSLOptions;

class LangfuseHttpClientOptionsCustomizerTests {
    private static final URI HTTPS_URI = URI.create("https://langfuse.example.com");
    private static final URI HTTP_URI = URI.create("http://langfuse.example.com");
    private static final String TLS_CONFIGURATION_NAME = "langfuse";
    private static final List<String> JDK_PROXY_PROPERTIES = List.of(
            "http.proxyHost", "http.proxyPort", "http.proxyUser", "http.proxyPassword",
            "https.proxyHost", "https.proxyPort", "https.proxyUser", "https.proxyPassword");

    private final PemTrustOptions namedTrustOptions = new PemTrustOptions().addCertPath("named-ca.crt");
    private final PemTrustOptions defaultTrustOptions = new PemTrustOptions().addCertPath("default-ca.crt");

    private LangfuseOtelConfig config;
    private TlsConfigurationRegistry registry;
    private TlsConfiguration namedConfiguration;
    private ProxyOptionsConfig proxyConfig;
    private Map<String, Optional<String>> originalJdkProxyProperties;

    @BeforeEach
    void setUp() {
        this.originalJdkProxyProperties = JDK_PROXY_PROPERTIES.stream()
                .collect(Collectors.toUnmodifiableMap(Function.identity(),
                        name -> Optional.ofNullable(System.getProperty(name))));
        JDK_PROXY_PROPERTIES.forEach(System::clearProperty);

        this.proxyConfig = mock(ProxyOptionsConfig.class);
        when(this.proxyConfig.enabled()).thenReturn(false);
        when(this.proxyConfig.host()).thenReturn(Optional.empty());
        when(this.proxyConfig.port()).thenReturn(OptionalInt.empty());
        when(this.proxyConfig.username()).thenReturn(Optional.empty());
        when(this.proxyConfig.password()).thenReturn(Optional.empty());

        this.config = mock(LangfuseOtelConfig.class);
        when(this.config.tlsConfigurationName()).thenReturn(Optional.empty());
        when(this.config.proxyOptions()).thenReturn(this.proxyConfig);

        this.namedConfiguration = mockTlsConfiguration(this.namedTrustOptions, false);
        var defaultConfiguration = mockTlsConfiguration(this.defaultTrustOptions, false);

        this.registry = mock(TlsConfigurationRegistry.class);
        when(this.registry.get(anyString())).thenReturn(Optional.empty());
        when(this.registry.get(TLS_CONFIGURATION_NAME)).thenReturn(Optional.of(this.namedConfiguration));
        when(this.registry.getDefault()).thenReturn(Optional.of(defaultConfiguration));
    }

    @AfterEach
    void restoreJdkProxyProperties() {
        for (var property : this.originalJdkProxyProperties.entrySet()) {
            property.getValue()
                    .ifPresentOrElse(value -> System.setProperty(property.getKey(), value),
                            () -> System.clearProperty(property.getKey()));
        }
    }

    @Test
    void httpsUsesNamedTlsConfiguration() {
        when(this.config.tlsConfigurationName()).thenReturn(Optional.of(TLS_CONFIGURATION_NAME));

        var options = customize(HTTPS_URI);

        assertThat(options)
                .returns(true, HttpClientOptions::isSsl)
                .returns(true, HttpClientOptions::isUseAlpn)
                .returns(false, HttpClientOptions::isTrustAll)
                .returns(true, HttpClientOptions::isVerifyHost)
                .returns(this.namedTrustOptions, HttpClientOptions::getTrustOptions);
    }

    @Test
    void httpsWithoutNameUsesDefaultTlsConfiguration() {
        var options = customize(HTTPS_URI);

        assertThat(options)
                .returns(true, HttpClientOptions::isSsl)
                .returns(true, HttpClientOptions::isUseAlpn)
                .returns(this.defaultTrustOptions, HttpClientOptions::getTrustOptions);

        verify(this.registry, never()).get(anyString());
    }

    @Test
    void httpsWithoutAnyTlsConfigurationStillEnablesSslAndAlpn() {
        when(this.registry.getDefault()).thenReturn(Optional.empty());

        var options = customize(HTTPS_URI);

        assertThat(options)
                .returns(true, HttpClientOptions::isSsl)
                .returns(true, HttpClientOptions::isUseAlpn)
                .returns(null, HttpClientOptions::getTrustOptions);
    }

    @Test
    void httpsAlwaysUsesAlpnEvenIfTlsConfigurationDisablesIt() {
        when(this.config.tlsConfigurationName()).thenReturn(Optional.of(TLS_CONFIGURATION_NAME));
        when(this.namedConfiguration.getSSLOptions()).thenReturn(new SSLOptions().setUseAlpn(false));

        var options = customize(HTTPS_URI);

        assertThat(options)
                .returns(true, HttpClientOptions::isSsl)
                .returns(true, HttpClientOptions::isUseAlpn);
    }

    @Test
    void httpsSchemeIsCaseInsensitive() {
        var options = customize(URI.create("HTTPS://langfuse.example.com"));

        assertThat(options)
                .returns(true, HttpClientOptions::isSsl)
                .returns(true, HttpClientOptions::isUseAlpn);
    }

    @Test
    void httpLeavesOptionsUntouched() {
        when(this.config.tlsConfigurationName()).thenReturn(Optional.of(TLS_CONFIGURATION_NAME));

        var options = customize(HTTP_URI);

        assertThat(options.toJson())
                .isEqualTo(new HttpClientOptions().toJson());
    }

    @Test
    void httpDoesNotTouchTlsRegistry() {
        when(this.config.tlsConfigurationName()).thenReturn(Optional.of(TLS_CONFIGURATION_NAME));

        customize(HTTP_URI);

        verifyNoInteractions(this.registry);
    }

    @Test
    void httpIgnoresUnknownTlsConfigurationName() {
        when(this.config.tlsConfigurationName()).thenReturn(Optional.of("does-not-exist"));

        var options = customize(HTTP_URI);

        assertThat(options.toJson())
                .isEqualTo(new HttpClientOptions().toJson());
    }

    @Test
    void unknownTlsConfigurationNameFailsFast() {
        when(this.config.tlsConfigurationName()).thenReturn(Optional.of("does-not-exist"));

        assertThatThrownBy(() -> new LangfuseHttpClientOptionsCustomizer(HTTPS_URI, this.config, this.registry))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does-not-exist");
    }

    @Test
    void trustAllDisablesCertificateAndHostVerification() {
        var trustAllConfiguration = mockTlsConfiguration(null, true);
        when(this.registry.get("trust-all")).thenReturn(Optional.of(trustAllConfiguration));
        when(this.config.tlsConfigurationName()).thenReturn(Optional.of("trust-all"));

        var options = customize(HTTPS_URI);

        assertThat(options)
                .returns(true, HttpClientOptions::isSsl)
                .returns(true, HttpClientOptions::isTrustAll)
                .returns(false, HttpClientOptions::isVerifyHost);
    }

    @Test
    void defaultTrustAllDisablesCertificateAndHostVerification() {
        var trustAllConfiguration = mockTlsConfiguration(null, true);
        when(this.registry.getDefault()).thenReturn(Optional.of(trustAllConfiguration));

        var options = customize(HTTPS_URI);

        assertThat(options)
                .returns(true, HttpClientOptions::isTrustAll)
                .returns(false, HttpClientOptions::isVerifyHost);
    }

    @Test
    void proxyDisabledIgnoresConfiguredHostAndJdkSystemProperties() {
        when(this.proxyConfig.host()).thenReturn(Optional.of("proxy.example.com"));
        System.setProperty("https.proxyHost", "jdk-proxy.example.com");
        System.setProperty("http.proxyHost", "jdk-proxy.example.com");

        assertThat(List.of(customize(HTTPS_URI), customize(HTTP_URI)))
                .extracting(HttpClientOptions::getProxyOptions)
                .containsOnlyNulls();
    }

    @Test
    void httpsUsesConfiguredProxy() {
        enableProxy("proxy.example.com", 8888, "proxy-user", "proxy-secret");
        System.setProperty("https.proxyHost", "jdk-proxy.example.com");

        var options = customize(HTTPS_URI);

        assertThat(options)
                .returns(true, HttpClientOptions::isSsl)
                .extracting(HttpClientOptions::getProxyOptions)
                .isNotNull()
                .returns(ProxyType.HTTP, ProxyOptions::getType)
                .returns("proxy.example.com", ProxyOptions::getHost)
                .returns(8888, ProxyOptions::getPort)
                .returns("proxy-user", ProxyOptions::getUsername)
                .returns("proxy-secret", ProxyOptions::getPassword);
    }

    @Test
    void httpUsesConfiguredProxyWithoutTls() {
        enableProxy("proxy.example.com", 8888, "proxy-user", "proxy-secret");

        var options = customize(HTTP_URI);

        assertThat(options)
                .returns(false, HttpClientOptions::isSsl)
                .returns(null, HttpClientOptions::getTrustOptions)
                .extracting(HttpClientOptions::getProxyOptions)
                .isNotNull()
                .returns("proxy.example.com", ProxyOptions::getHost)
                .returns(8888, ProxyOptions::getPort);

        verifyNoInteractions(this.registry);
    }

    @Test
    void configuredProxyWithoutPortAndCredentialsUsesVertxDefaults() {
        when(this.proxyConfig.enabled()).thenReturn(true);
        when(this.proxyConfig.host()).thenReturn(Optional.of("proxy.example.com"));

        var options = customize(HTTPS_URI);

        assertThat(options.getProxyOptions())
                .isNotNull()
                .returns("proxy.example.com", ProxyOptions::getHost)
                .returns(ProxyOptions.DEFAULT_PORT, ProxyOptions::getPort)
                .returns(null, ProxyOptions::getUsername)
                .returns(null, ProxyOptions::getPassword);
    }

    @Test
    void httpsProxyFallsBackToJdkHttpsSystemProperties() {
        when(this.proxyConfig.enabled()).thenReturn(true);
        System.setProperty("https.proxyHost", "https-proxy.example.com");
        System.setProperty("https.proxyPort", "8443");
        System.setProperty("https.proxyUser", "https-user");
        System.setProperty("https.proxyPassword", "https-secret");
        System.setProperty("http.proxyHost", "http-proxy.example.com");
        System.setProperty("http.proxyPort", "8080");

        var options = customize(HTTPS_URI);

        assertThat(options.getProxyOptions())
                .isNotNull()
                .returns(ProxyType.HTTP, ProxyOptions::getType)
                .returns("https-proxy.example.com", ProxyOptions::getHost)
                .returns(8443, ProxyOptions::getPort)
                .returns("https-user", ProxyOptions::getUsername)
                .returns("https-secret", ProxyOptions::getPassword);
    }

    @Test
    void httpProxyFallsBackToJdkHttpSystemProperties() {
        when(this.proxyConfig.enabled()).thenReturn(true);
        System.setProperty("http.proxyHost", "http-proxy.example.com");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("http.proxyUser", "http-user");
        System.setProperty("http.proxyPassword", "http-secret");
        System.setProperty("https.proxyHost", "https-proxy.example.com");

        var options = customize(HTTP_URI);

        assertThat(options)
                .returns(false, HttpClientOptions::isSsl)
                .extracting(HttpClientOptions::getProxyOptions)
                .isNotNull()
                .returns("http-proxy.example.com", ProxyOptions::getHost)
                .returns(8080, ProxyOptions::getPort)
                .returns("http-user", ProxyOptions::getUsername)
                .returns("http-secret", ProxyOptions::getPassword);
    }

    @Test
    void jdkSystemPropertiesWithoutPortUseSchemeDefaultPort() {
        when(this.proxyConfig.enabled()).thenReturn(true);
        System.setProperty("https.proxyHost", "https-proxy.example.com");
        System.setProperty("http.proxyHost", "http-proxy.example.com");

        assertThat(List.of(customize(HTTPS_URI), customize(HTTP_URI)))
                .extracting(HttpClientOptions::getProxyOptions)
                .extracting(ProxyOptions::getHost, ProxyOptions::getPort)
                .containsExactly(
                        tuple("https-proxy.example.com", 443),
                        tuple("http-proxy.example.com", 80));
    }

    @Test
    void jdkSystemPropertiesIgnoreBlankCredentials() {
        when(this.proxyConfig.enabled()).thenReturn(true);
        System.setProperty("https.proxyHost", "https-proxy.example.com");
        System.setProperty("https.proxyUser", " ");
        System.setProperty("https.proxyPassword", "");

        var options = customize(HTTPS_URI);

        assertThat(options.getProxyOptions())
                .isNotNull()
                .returns(null, ProxyOptions::getUsername)
                .returns(null, ProxyOptions::getPassword);
    }

    @Test
    void jdkProxyHostNoneMeansNoProxy() {
        when(this.proxyConfig.enabled()).thenReturn(true);
        System.setProperty("https.proxyHost", "none");

        var options = customize(HTTPS_URI);

        assertThat(options.getProxyOptions())
                .isNull();
    }

    @Test
    void proxyEnabledWithoutHostOrJdkSystemPropertiesMeansNoProxy() {
        when(this.proxyConfig.enabled()).thenReturn(true);

        assertThat(List.of(customize(HTTPS_URI), customize(HTTP_URI)))
                .extracting(HttpClientOptions::getProxyOptions)
                .containsOnlyNulls();
    }

    private void enableProxy(String host, int port, String username, String password) {
        when(this.proxyConfig.enabled()).thenReturn(true);
        when(this.proxyConfig.host()).thenReturn(Optional.of(host));
        when(this.proxyConfig.port()).thenReturn(OptionalInt.of(port));
        when(this.proxyConfig.username()).thenReturn(Optional.of(username));
        when(this.proxyConfig.password()).thenReturn(Optional.of(password));
    }

    private HttpClientOptions customize(URI baseUri) {
        var options = new HttpClientOptions();
        new LangfuseHttpClientOptionsCustomizer(baseUri, this.config, this.registry).accept(options);

        return options;
    }

    private static TlsConfiguration mockTlsConfiguration(PemTrustOptions trustOptions, boolean isTrustAll) {
        var configuration = mock(TlsConfiguration.class);
        when(configuration.getTrustStoreOptions()).thenReturn(trustOptions);
        when(configuration.isTrustAll()).thenReturn(isTrustAll);
        when(configuration.getHostnameVerificationAlgorithm()).thenReturn(Optional.empty());
        when(configuration.getSslEngineOptions()).thenReturn(Optional.empty());

        return configuration;
    }
}
