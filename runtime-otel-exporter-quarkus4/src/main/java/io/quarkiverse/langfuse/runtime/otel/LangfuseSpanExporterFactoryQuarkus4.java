package io.quarkiverse.langfuse.runtime.otel;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.internal.HttpExporter;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.common.internal.ComponentId;
import io.opentelemetry.sdk.common.internal.StandardComponentId;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.quarkus.opentelemetry.runtime.exporter.otlp.sender.VertxHttpSender;
import io.quarkus.opentelemetry.runtime.exporter.otlp.tracing.VertxHttpSpanExporter;

/**
 * Mirrors how {@code quarkus-opentelemetry} builds its own OTLP HTTP span exporter, reusing its Vert.x sender so that
 * no additional OTel sender dependency is needed on the application classpath.
 */
public class LangfuseSpanExporterFactoryQuarkus4 implements LangfuseSpanExporterFactory {
    @Override
    public SpanExporter createExporter(LangfuseSpanExporterConfig config) {
        var sender = new VertxHttpSender(
                config.baseUri(),
                config.signalPath(),
                false,
                config.timeout(),
                config.headers(),
                config.contentType(),
                options -> {
                },
                config.vertx());

        var httpExporter = new HttpExporter(
                ComponentId.generateLazy(StandardComponentId.ExporterType.OTLP_HTTP_SPAN_EXPORTER),
                sender,
                MeterProvider::noop,
                InternalTelemetryVersion.LATEST,
                config.baseUri(),
                false);

        return new VertxHttpSpanExporter(httpExporter, config.memoryMode());
    }
}
