package io.quarkiverse.langfuse.runtime.otel;

import java.util.Base64;
import java.util.Optional;

import org.jboss.logging.Logger;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.context.Context;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.incubating.GenAiIncubatingAttributes;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.config.LangfuseOtelConfig.SpanFilterType;

/**
 * Implementation of the {@link SpanProcessor} interface for integrating with Langfuse.
 * This processor acts as a wrapper around an underlying {@link SpanProcessor}
 * that is configured based on Langfuse-specific settings.
 *
 * The processor sends span data to the Langfuse server via the OTLP HTTP exporter.
 * The configuration for the server endpoint, authentication, and other runtime
 * settings are derived from a {@link LangfuseConfig} instance.
 *
 * The Langfuse processor supports filtering spans based on the provided
 * {@link SpanFilterType}. Depending on the filter, it may include all spans or
 * limit the scope to AI-related spans.
 */
public class LangfuseSpanProcessor implements SpanProcessor {
    private static final Logger LOG = Logger.getLogger(LangfuseSpanProcessor.class);

    private final SpanProcessor delegate;

    public LangfuseSpanProcessor(LangfuseConfig langfuseConfig) {
        LOG.debug("Initializing Langfuse OTLP Span Processor");
        var credentials = "%s:%s".formatted(langfuseConfig.username(), langfuseConfig.password());
        var authHeader = "Basic %s".formatted(Base64.getEncoder().encodeToString(credentials.getBytes()));

        var exporter = OtlpHttpSpanExporter.builder()
                .setEndpoint(langfuseConfig.otel().traceIngestionUrl())
                .addHeader("Authorization", authHeader)
                .addHeader("x-langfuse-ingestion-version", "1")
                .build();

        var filteredExporter = switch (langfuseConfig.otel().spanFilter()) {
            case ALL -> exporter;
            case AI_ONLY -> new FilteringAISpanExporter(exporter);
        };

        var enrichingExporter = new LangfuseAttributeEnrichingSpanExporter(filteredExporter, langfuseConfig);

        this.delegate = BatchSpanProcessor
                .builder(enrichingExporter)
                .build();
    }

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        Optional.ofNullable(
                Baggage.fromContext(parentContext).getEntryValue(GenAiIncubatingAttributes.GEN_AI_CONVERSATION_ID.getKey()))
                .ifPresent(
                        conversationId -> span.setAttribute(GenAiIncubatingAttributes.GEN_AI_CONVERSATION_ID, conversationId));

        this.delegate.onStart(parentContext, span);
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {
        this.delegate.onEnd(span);
    }

    @Override
    public boolean isEndRequired() {
        return this.delegate.isEndRequired();
    }

    @Override
    public CompletableResultCode shutdown() {
        return this.delegate.shutdown();
    }

    @Override
    public CompletableResultCode forceFlush() {
        return this.delegate.forceFlush();
    }

    @Override
    public void close() {
        this.delegate.close();
    }
}
