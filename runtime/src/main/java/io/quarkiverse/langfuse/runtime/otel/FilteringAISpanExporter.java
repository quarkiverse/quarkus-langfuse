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
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.incubating.GenAiIncubatingAttributes;

/**
 * An implementation of the SpanExporter interface designed to filter and export spans
 * related to AI-generated content, particularly spans matching specific criteria
 * associated with generative AI processing.
 *
 * This exporter processes spans to identify generative AI-related spans and their ancestors.
 * It delegates the export task to another SpanExporter after performing this filtering.
 *
 * The functionality includes:
 * - Filtering spans associated with generative AI and their ancestors.
 * - Identification of a "completion" span among the filtered spans, which corresponds
 * to spans with specific attributes indicating the end of an AI operation.
 * - Delegation of the filtered spans to the provided delegate SpanExporter.
 */
public class FilteringAISpanExporter implements SpanExporter {
    private static final Logger LOG = Logger.getLogger(FilteringAISpanExporter.class);
    private final SpanExporter delegate;

    public FilteringAISpanExporter(SpanExporter delegate) {
        this.delegate = delegate;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        LOG.debug("Exporting spans to Langfuse");
        var exportMap = filterAiSpansWithAncestors(spans);

        return exportMap.isEmpty() ? CompletableResultCode.ofSuccess() : this.delegate.export(exportMap.values());
    }

    private static Optional<SpanData> findFirstCompletedGenAiSpan(Collection<SpanData> llmSpans) {
        return llmSpans.stream()
                .filter(span -> isGenAiSpan(span) && isCompletionSpan(span))
                .min(Comparator.comparingLong(SpanData::getStartEpochNanos));
    }

    private static Map<String, SpanData> filterAiSpansWithAncestors(Collection<SpanData> spans) {
        var spansById = spans.stream().collect(Collectors.toUnmodifiableMap(SpanData::getSpanId, Function.identity()));

        var seen = new HashSet<String>();
        var result = new HashMap<String, SpanData>();

        spansById.values().stream()
                .filter(FilteringAISpanExporter::isGenAiSpan)
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
}
