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
 * A wrapping {@link SpanExporter} that enriches spans with Langfuse-specific attributes
 * before delegating to the next exporter in the chain.
 * <p>
 * For every span, this exporter adds:
 * <ul>
 * <li>{@code langfuse.environment} — the configured environment name</li>
 * </ul>
 * For spans that also contain a {@code gen_ai.prompt} attribute, it additionally adds:
 * <ul>
 * <li>{@code langfuse.trace.input} — copied from {@code gen_ai.prompt}</li>
 * <li>{@code langfuse.trace.output} — copied from {@code gen_ai.completion}, if present</li>
 * </ul>
 * This exporter is placed before both {@link FilteringAISpanExporter} and the raw OTLP exporter,
 * so it works regardless of the configured {@code spanFilter} mode.
 */
final class LangfuseAttributeEnrichingSpanExporter extends DelegatingSpanExporter {
    static final AttributeKey<String> LANGFUSE_ENVIRONMENT = AttributeKey.stringKey("langfuse.environment");
    static final AttributeKey<String> LANGFUSE_TRACE_INPUT = AttributeKey.stringKey("langfuse.trace.input");
    static final AttributeKey<String> LANGFUSE_TRACE_OUTPUT = AttributeKey.stringKey("langfuse.trace.output");

    private final LangfuseConfig config;

    LangfuseAttributeEnrichingSpanExporter(SpanExporter delegate, LangfuseConfig config) {
        super(delegate);
        this.config = config;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        var enriched = spans.stream()
                .map(this::enrich)
                .toList();

        return super.export(enriched);
    }

    private SpanData enrich(SpanData span) {
        var builder = span.getAttributes()
                .toBuilder()
                .put(LANGFUSE_ENVIRONMENT, this.config.environment());

        var prompt = span.getAttributes().get(GenAiIncubatingAttributes.GEN_AI_PROMPT);

        if (prompt != null) {
            builder.put(LANGFUSE_TRACE_INPUT, prompt);

            var completion = span.getAttributes().get(GenAiIncubatingAttributes.GEN_AI_COMPLETION);
            if (completion != null) {
                builder.put(LANGFUSE_TRACE_OUTPUT, completion);
            }
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
}