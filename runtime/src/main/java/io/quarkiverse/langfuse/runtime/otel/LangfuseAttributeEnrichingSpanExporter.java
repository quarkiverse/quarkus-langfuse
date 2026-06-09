package io.quarkiverse.langfuse.runtime.otel;

import java.util.Collection;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.DelegatingSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.incubating.GenAiIncubatingAttributes;
import io.quarkiverse.langfuse.config.LangfuseConfig;

/**
 * A wrapping {@link SpanExporter} that enriches AI spans with Langfuse-specific attributes
 * before delegating to the next exporter in the chain.
 * <p>
 * For spans that contain a {@code gen_ai.prompt} attribute, this exporter adds:
 * <ul>
 * <li>{@code langfuse.environment} — the configured environment name</li>
 * <li>{@code langfuse.trace.input} — copied from {@code gen_ai.prompt}</li>
 * <li>{@code langfuse.trace.output} — copied from {@code gen_ai.completion}, if present</li>
 * </ul>
 * Spans without {@code gen_ai.prompt} are passed through unchanged.
 * <p>
 * This exporter is placed before both {@link FilteringAISpanExporter} and the raw OTLP exporter,
 * so it works regardless of the configured {@code spanFilter} mode.
 */
final class LangfuseAttributeEnrichingSpanExporter implements SpanExporter {
    static final AttributeKey<String> LANGFUSE_ENVIRONMENT = AttributeKey.stringKey("langfuse.environment");
    static final AttributeKey<String> LANGFUSE_TRACE_INPUT = AttributeKey.stringKey("langfuse.trace.input");
    static final AttributeKey<String> LANGFUSE_TRACE_OUTPUT = AttributeKey.stringKey("langfuse.trace.output");

    private final SpanExporter delegate;
    private final LangfuseConfig config;

    LangfuseAttributeEnrichingSpanExporter(SpanExporter delegate, LangfuseConfig config) {
        this.delegate = delegate;
        this.config = config;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        var enriched = spans.stream()
                .map(this::enrichIfAiSpan)
                .toList();

        return this.delegate.export(enriched);
    }

    private SpanData enrichIfAiSpan(SpanData span) {
        var prompt = span.getAttributes().get(GenAiIncubatingAttributes.GEN_AI_PROMPT);

        if (prompt != null) {
            var builder = span.getAttributes()
                    .toBuilder()
                    .put(LANGFUSE_ENVIRONMENT, this.config.environment())
                    .put(LANGFUSE_TRACE_INPUT, prompt);

            var completion = span.getAttributes().get(GenAiIncubatingAttributes.GEN_AI_COMPLETION);
            if (completion != null) {
                builder.put(LANGFUSE_TRACE_OUTPUT, completion);
            }

            var enrichedAttributes = builder.build();

            return new DelegatingSpanData(span) {
                @Override
                public Attributes getAttributes() {
                    return enrichedAttributes;
                }

                @Override
                public int getTotalAttributeCount() {
                    return enrichedAttributes.size();
                }
            };
        }

        return span;
    }

    @Override
    public CompletableResultCode flush() {
        return this.delegate.flush();
    }

    @Override
    public CompletableResultCode shutdown() {
        return this.delegate.shutdown();
    }

    @Override
    public void close() {
        this.delegate.close();
    }
}