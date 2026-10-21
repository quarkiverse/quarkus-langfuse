package io.quarkiverse.langfuse.it;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.util.Map;

import com.github.tomakehurst.wiremock.WireMockServer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * Runs a stand-in for the Langfuse OTLP ingestion endpoint in the test JVM and points the application under test at it.
 * Because it lives outside the application process it works unchanged for JVM and native executables.
 */
public class LangfuseStubResource implements QuarkusTestResourceLifecycleManager {
    static final String TRACES_PATH = "/api/public/otel/v1/traces";

    private WireMockServer server;

    @Override
    public Map<String, String> start() {
        this.server = new WireMockServer(options().dynamicPort());
        this.server.start();
        this.server.stubFor(post(urlPathEqualTo(TRACES_PATH)).willReturn(ok()));

        return Map.of("quarkus.langfuse.base-url", "http://localhost:%d".formatted(this.server.port()));
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(this.server,
                new TestInjector.AnnotatedAndMatchesType(InjectLangfuseStub.class, WireMockServer.class));
    }

    @Override
    public void stop() {
        if (this.server != null) {
            this.server.stop();
        }
    }
}
