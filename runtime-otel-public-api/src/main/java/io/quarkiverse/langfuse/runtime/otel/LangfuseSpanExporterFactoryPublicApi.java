package io.quarkiverse.langfuse.runtime.otel;

import java.time.Duration;
import java.util.Map;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.http.HttpExporter;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.common.internal.ComponentId;
import io.opentelemetry.sdk.common.internal.StandardComponentId;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.quarkus.opentelemetry.runtime.exporter.otlp.sender.VertxHttpSender;
import io.quarkus.opentelemetry.runtime.exporter.otlp.tracing.VertxHttpSpanExporter;

public class LangfuseSpanExporterFactoryPublicApi implements LangfuseSpanExporterFactory {
    @Override
    public SpanExporter createExporter(LangfuseSpanExporterConfig config) {
        var sender = new VertxHttpSender(
                config.getBaseUri(),
                config.getSignalPath(),
                false,
                Duration.ofSeconds(10),
                Map.of("Authorization", config.getAuthHeader(), "x-langfuse-ingestion-version", "1"),
                "application/x-protobuf",
                options -> {
                },
                config.getVertx());

        var httpExporter = new HttpExporter(
                ComponentId.generateLazy(StandardComponentId.ExporterType.OTLP_HTTP_SPAN_EXPORTER),
                sender,
                MeterProvider::noop,
                InternalTelemetryVersion.LATEST,
                config.getBaseUri(),
                false);

        return new VertxHttpSpanExporter(httpExporter, config.getMemoryMode());
    }
}
