package io.quarkiverse.langfuse.runtime.otel;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

import io.opentelemetry.sdk.common.export.MemoryMode;
import io.vertx.core.Vertx;

/**
 * Everything a {@link LangfuseSpanExporterFactory} needs to build an exporter, independent of the Quarkus version.
 */
public record LangfuseSpanExporterConfig(
        URI baseUri,
        String signalPath,
        Duration timeout,
        Map<String, String> headers,
        String contentType,
        MemoryMode memoryMode,
        Vertx vertx) {
}
