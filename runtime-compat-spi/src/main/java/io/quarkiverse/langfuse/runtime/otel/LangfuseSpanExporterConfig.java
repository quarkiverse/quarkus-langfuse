package io.quarkiverse.langfuse.runtime.otel;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

import io.opentelemetry.sdk.common.export.MemoryMode;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;

/**
 * Everything a {@link LangfuseSpanExporterFactory} needs to build an exporter, independent of the Quarkus version.
 * <p>
 * The {@code clientOptionsCustomizer} is applied to the Vert.x {@link HttpClientOptions} used by the exporter's HTTP
 * sender (e.g. to configure TLS or a proxy).
 */
public record LangfuseSpanExporterConfig(
        URI baseUri,
        String signalPath,
        Duration timeout,
        Map<String, String> headers,
        String contentType,
        MemoryMode memoryMode,
        Consumer<HttpClientOptions> clientOptionsCustomizer,
        Vertx vertx) {
}
