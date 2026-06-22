package io.quarkiverse.langfuse.runtime.otel;

import io.opentelemetry.sdk.trace.export.SpanExporter;

@FunctionalInterface
public interface LangfuseSpanExporterFactory {
    SpanExporter createExporter(LangfuseSpanExporterConfig config);
}
