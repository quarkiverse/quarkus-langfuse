package io.quarkiverse.langfuse.runtime.otel;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
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
import io.quarkus.logging.Log;

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
final class FilteringAISpanExporter extends DelegatingSpanExporter {
    private static final Logger LOG = Logger.getLogger(FilteringAISpanExporter.class);

    FilteringAISpanExporter(SpanExporter delegate) {
        super(delegate);
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        LOG.debug("Exporting spans to Langfuse");
        var exportMap = filterAiSpansWithAncestors(spans);

        if (Log.isDebugEnabled()) {
            Log.debugf("Exporting %d spans to Langfuse", exportMap.size());
            exportMap.forEach((k, v) -> Log.debugf("Exporting span id %s (name = %s):\n%s\n", k, v.getName(), v));
        }

        return exportMap.isEmpty() ? CompletableResultCode.ofSuccess() : super.export(exportMap.values());
    }

    private static Map<String, SpanData> filterAiSpansWithAncestors(Collection<SpanData> spans) {
        var spansById = spans.stream()
                .collect(Collectors.toUnmodifiableMap(SpanData::getSpanId, Function.identity()));

        var seen = new HashSet<String>();
        var result = new HashMap<String, SpanData>();

        spansById.values().stream()
                .filter(FilteringAISpanExporter::isGenAiSpan)
                .flatMap(span -> Stream.iterate(span, Objects::nonNull, current -> spansById.get(current.getParentSpanId()))
                        .takeWhile(current -> seen.add(current.getSpanId())))
                .forEach(span -> result.put(span.getSpanId(), span));

        return result;
    }

    private static boolean isGenAiSpan(SpanData span) {
        var isGenAiSpen = spanAttributeKeysMatchCriteria(
                span,
                key -> !GenAiIncubatingAttributes.GEN_AI_CONVERSATION_ID.getKey().equalsIgnoreCase(key.getKey())
                        && key.getKey().startsWith("gen_ai."));

        Log.debugf(
                "Span %s: isGenAiSpen = %s (span does NOT have the '%s' attribute AND does NOT have any attribute starting with 'gen_ai.')",
                span.getName(), isGenAiSpen, GenAiIncubatingAttributes.GEN_AI_CONVERSATION_ID.getKey());

        return isGenAiSpen;
    }

    private static boolean spanAttributeKeysMatchCriteria(SpanData span, Predicate<AttributeKey> predicate) {
        return span.getAttributes()
                .asMap()
                .keySet()
                .stream()
                .anyMatch(predicate);
    }
}
