package io.quarkiverse.langfuse.runtime.otel;

import java.time.Duration;
import java.util.Map;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.http.HttpExporter;
import io.opentelemetry.exporter.internal.otlp.traces.TraceRequestMarshaler;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.StandardComponentId;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.quarkus.opentelemetry.runtime.exporter.otlp.sender.VertxHttpSender;
import io.quarkus.opentelemetry.runtime.exporter.otlp.tracing.VertxHttpSpanExporter;

public class LangfuseSpanExporterFactoryInternalApi implements LangfuseSpanExporterFactory {
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

        var httpExporter = new HttpExporter<TraceRequestMarshaler>(
                ComponentId.generateLazy(StandardComponentId.ExporterType.OTLP_HTTP_SPAN_EXPORTER),
                sender,
                MeterProvider::noop,
                InternalTelemetryVersion.LATEST,
                config.getTraceIngestionUrl());

        return new VertxHttpSpanExporter(httpExporter);
    }
}
