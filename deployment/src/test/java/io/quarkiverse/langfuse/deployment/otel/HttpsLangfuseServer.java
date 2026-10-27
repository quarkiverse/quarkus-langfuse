package io.quarkiverse.langfuse.deployment.otel;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import io.smallrye.certs.CertificateGenerator;
import io.smallrye.certs.CertificateRequest;
import io.smallrye.certs.Format;
import io.smallrye.certs.PemCertificateFiles;
import io.smallrye.certs.Pkcs12CertificateFiles;

/**
 * An HTTPS-only WireMock server standing in for a Langfuse instance behind a TLS ingress, backed by a freshly generated
 * self-signed {@code localhost} certificate.
 * <p>
 * {@code QuarkusExtensionTest} loads the test class a second time in the Quarkus application class loader, so static
 * initializers run twice. To get exactly one server (and one certificate) per test class, the server is started and
 * stopped through {@code setBeforeAllCustomizer}/{@code setAfterAllCustomizer}, which only run in the JUnit class
 * loader, and the dynamic port and certificate path are handed over through system properties. Config values reference
 * them through the {@link #BASE_URL} and {@link #TRUST_CERT} expressions, which are resolved when the application
 * boots, after the server has started.
 * </p>
 */
final class HttpsLangfuseServer {
    static final String TRACES_PATH = "/api/public/otel/v1/traces";
    static final String BASE_URL = "https://localhost:${langfuse.test.https.port}";
    static final String TRUST_CERT = "${langfuse.test.https.trust-cert}";

    private static final String PORT_PROPERTY = "langfuse.test.https.port";
    private static final String TRUST_CERT_PROPERTY = "langfuse.test.https.trust-cert";
    private static final String CERTIFICATE_NAME = "langfuse";
    private static final String KEYSTORE_PASSWORD = "langfuse-secret";

    private final int tracesResponseStatus;

    private Optional<WireMockServer> server = Optional.empty();
    private Optional<Path> certificateDirectory = Optional.empty();

    /**
     * A server accepting every OTLP export with {@code 200 OK}.
     */
    HttpsLangfuseServer() {
        this(200);
    }

    /**
     * A server answering every OTLP export with the given HTTP status.
     */
    HttpsLangfuseServer(int tracesResponseStatus) {
        this.tracesResponseStatus = tracesResponseStatus;
    }

    void start() {
        try {
            var directory = Files.createTempDirectory("langfuse-tls-");
            this.certificateDirectory = Optional.of(directory);

            var certificateFiles = new CertificateGenerator(directory, true)
                    .generate(new CertificateRequest()
                            .withName(CERTIFICATE_NAME)
                            .withCN("localhost")
                            .withSubjectAlternativeName("DNS:localhost")
                            .withPassword(KEYSTORE_PASSWORD)
                            .withFormats(List.of(Format.PEM, Format.PKCS12)));

            var keyStore = certificateFiles.stream()
                    .filter(Pkcs12CertificateFiles.class::isInstance)
                    .map(Pkcs12CertificateFiles.class::cast)
                    .map(Pkcs12CertificateFiles::keyStoreFile)
                    .findFirst()
                    .orElseThrow();

            var trustCert = certificateFiles.stream()
                    .filter(PemCertificateFiles.class::isInstance)
                    .map(PemCertificateFiles.class::cast)
                    .map(PemCertificateFiles::trustFile)
                    .findFirst()
                    .orElseThrow();

            var wireMockServer = new WireMockServer(WireMockConfiguration.options()
                    .httpDisabled(true)
                    .httpsPort(0)
                    .keystoreType("PKCS12")
                    .keystorePath(keyStore.toString())
                    .keystorePassword(KEYSTORE_PASSWORD)
                    .keyManagerPassword(KEYSTORE_PASSWORD));

            wireMockServer.start();
            wireMockServer.stubFor(post(urlPathEqualTo(TRACES_PATH)).willReturn(status(this.tracesResponseStatus)));
            this.server = Optional.of(wireMockServer);

            System.setProperty(PORT_PROPERTY, String.valueOf(wireMockServer.httpsPort()));
            System.setProperty(TRUST_CERT_PROPERTY, trustCert.toAbsolutePath().toString());
        } catch (Exception e) {
            stop();
            throw new IllegalStateException("Unable to start the HTTPS Langfuse WireMock server", e);
        }
    }

    void stop() {
        this.server.ifPresent(WireMockServer::stop);
        this.server = Optional.empty();
        this.certificateDirectory.ifPresent(HttpsLangfuseServer::deleteRecursively);
        this.certificateDirectory = Optional.empty();
        System.clearProperty(PORT_PROPERTY);
        System.clearProperty(TRUST_CERT_PROPERTY);
    }

    /**
     * An admin client for the running server. Usable from either class loader because it only relies on the port
     * system property. WireMock's admin client trusts any certificate, so it needs no TLS configuration.
     */
    static WireMock client() {
        return new WireMock("https", "localhost", Integer.getInteger(PORT_PROPERTY));
    }

    private static void deleteRecursively(Path directory) {
        try (var paths = Files.walk(directory)) {
            var sortedPaths = paths
                    .sorted(Comparator.reverseOrder())
                    .toList();

            for (var path : sortedPaths) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
