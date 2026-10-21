package io.quarkiverse.langfuse.runtime.otel;

import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * Creates the OTLP HTTP {@link SpanExporter} that ships spans to Langfuse.
 * <p>
 * The OTel classes used to build the exporter live in different packages depending on the Quarkus version, so each
 * supported Quarkus line provides its own implementation. The deployment module selects the right one at build time.
 */
@FunctionalInterface
public interface LangfuseSpanExporterFactory {
    SpanExporter createExporter(LangfuseSpanExporterConfig config);
}
