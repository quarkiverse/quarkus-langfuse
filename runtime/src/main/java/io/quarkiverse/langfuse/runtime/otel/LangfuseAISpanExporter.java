package io.quarkiverse.langfuse.runtime.otel;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.logging.Logger;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.DelegatingSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.incubating.GenAiIncubatingAttributes;
import io.quarkus.logging.Log;

/**
 * The {@code LangfuseAISpanExporter} class is responsible for exporting spans to a delegate span exporter
 * while enriching specific spans with additional attributes based on Langfuse-specific criteria.
 * It processes spans to identify and extract AI-related spans (general AI spans) and their ancestors
 * and conditions them to include enriched data before passing them to the underlying exporter.
 *
 * <p>
 * Primary functionality includes:
 * <ul>
 * <li>Filtering general AI-related spans and their associated ancestors.</li>
 * <li>Identifying the first "completed" General AI span within a specific trace span group.</li>
 * <li>Adding or enriching custom attributes (e.g., Langfuse trace input and output values) for specific spans.</li>
 * <li>Delegating enriched spans for export via another exporter.</li>
 * </ul>
 */
public class LangfuseAISpanExporter implements SpanExporter {
    private static final Logger LOG = Logger.getLogger(LangfuseAISpanExporter.class);
    private static final String LANGFUSE_TRACE_INPUT_ATTRIBUTE_KEY = "langfuse.trace.input";
    private static final String LANGFUSE_TRACE_OUTPUT_ATTRIBUTE_KEY = "langfuse.trace.output";

    private final SpanExporter delegate;

    public LangfuseAISpanExporter(SpanExporter delegate) {
        this.delegate = delegate;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        Log.debug("Exporting spans to Langfuse");
        var exportMap = filterAiSpansWithAncestors(spans);

        if (!exportMap.isEmpty()) {
            var spansByTraceMap = exportMap.values()
                    .stream()
                    .collect(Collectors.groupingBy(SpanData::getTraceId));

            spansByTraceMap.values()
                    .forEach(spansForTrace -> findFirstCompletedGenAiSpan(spansForTrace)
                            .ifPresent(llmSpan -> exportMap.put(
                                    llmSpan.getSpanId(),
                                    computeLangfuseSpan(llmSpan))));

            return this.delegate.export(exportMap.values());
        }

        return CompletableResultCode.ofSuccess();
    }

    private static Optional<SpanData> findFirstCompletedGenAiSpan(Collection<SpanData> llmSpans) {
        return llmSpans.stream()
                .filter(span -> isGenAiSpan(span) && isCompletionSpan(span))
                .min(Comparator.comparingLong(SpanData::getStartEpochNanos));
    }

    private static SpanData computeLangfuseSpan(SpanData span) {
        var attributes = span.getAttributes();
        var attributesBuilder = attributes.toBuilder();

        Optional.ofNullable(attributes.get(GenAiIncubatingAttributes.GEN_AI_PROMPT))
                .ifPresent(v -> attributesBuilder.put(LANGFUSE_TRACE_INPUT_ATTRIBUTE_KEY, v));

        Optional.ofNullable(attributes.get(GenAiIncubatingAttributes.GEN_AI_COMPLETION))
                .ifPresent(v -> attributesBuilder.put(LANGFUSE_TRACE_OUTPUT_ATTRIBUTE_KEY, v));

        var newAttributes = attributesBuilder.build();
        return newAttributes.isEmpty() ? span : new EnrichedSpanData(span, newAttributes);
    }

    private static Map<String, SpanData> filterAiSpansWithAncestors(Collection<SpanData> spans) {
        var spansById = spans.stream().collect(Collectors.toUnmodifiableMap(SpanData::getSpanId, Function.identity()));

        var seen = new HashSet<String>();
        var result = new HashMap<String, SpanData>();

        spansById.values().stream()
                .filter(LangfuseAISpanExporter::isGenAiSpan)
                .flatMap(span -> Stream.iterate(span, Objects::nonNull, current -> spansById.get(current.getParentSpanId()))
                        .takeWhile(current -> seen.add(current.getSpanId())))
                .forEach(span -> result.put(span.getSpanId(), span));

        return result;
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

    private static boolean isGenAiSpan(SpanData span) {
        return spanAttributeKeysMatchCriteria(
                span,
                key -> !GenAiIncubatingAttributes.GEN_AI_CONVERSATION_ID.getKey().equalsIgnoreCase(key.getKey())
                        && key.getKey().startsWith("gen_ai."));
    }

    private static boolean isCompletionSpan(SpanData span) {
        var finishReasons = span.getAttributes().get(AttributeKey.stringKey("gen_ai.response.finish_reasons"));

        return (finishReasons != null) && finishReasons.toUpperCase().contains("STOP");
    }

    private static boolean spanAttributeKeysMatchCriteria(SpanData span, Predicate<AttributeKey> predicate) {
        return span.getAttributes()
                .asMap()
                .keySet()
                .stream()
                .anyMatch(predicate);
    }

    private static final class EnrichedSpanData extends DelegatingSpanData {
        private final Attributes mergedAttributes;

        private EnrichedSpanData(SpanData delegate, Attributes mergedAttributes) {
            super(delegate);
            this.mergedAttributes = mergedAttributes;
        }

        @Override
        public Attributes getAttributes() {
            return this.mergedAttributes;
        }

        @Override
        public int getTotalAttributeCount() {
            return mergedAttributes.size();
        }
    }
}
