package io.quarkiverse.langfuse.runtime.otel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

class FilteringAISpanExporterTests {
    private SpanExporter delegate;
    private FilteringAISpanExporter exporter;

    @BeforeEach
    void setUp() {
        this.delegate = mock(SpanExporter.class);
        when(this.delegate.export(any())).thenReturn(CompletableResultCode.ofSuccess());
        this.exporter = new FilteringAISpanExporter(this.delegate);
    }

    @Test
    void keepsSpanWithGenAiAttribute() {
        var span = span("llm", "1", "0", Attributes.of(AttributeKey.stringKey("gen_ai.operation.name"), "chat"));

        this.exporter.export(List.of(span));

        assertThat(exportedSpanIds()).containsExactly("1");
    }

    @Test
    void dropsSpanWithOnlyConversationId() {
        var span = span("http", "1", "0", Attributes.of(AttributeKey.stringKey("gen_ai.conversation.id"), "abc"));

        this.exporter.export(List.of(span));

        verify(this.delegate, never()).export(any());
    }

    @Test
    void keepsSpanWithLangfuseObservationTypeAndItsAncestors() {
        var root = span("root", "1", "0", Attributes.empty());
        var chain = span("chain", "2", "1", Attributes.of(AttributeKey.stringKey("langfuse.observation.type"), "chain"));
        var unrelated = span("http", "3", "1", Attributes.empty());

        this.exporter.export(List.of(root, chain, unrelated));

        assertThat(exportedSpanIds()).containsExactlyInAnyOrder("1", "2");
    }

    @Test
    void keepsSpanWithAnyLangfuseObservationAttribute() {
        var span = span("obs", "1", "0", Attributes.of(AttributeKey.stringKey("langfuse.observation.level"), "WARNING"));

        this.exporter.export(List.of(span));

        assertThat(exportedSpanIds()).containsExactly("1");
    }

    @Test
    void dropsSpanWithOnlyNonObservationLangfuseAttribute() {
        var span = span("http", "1", "0", Attributes.of(AttributeKey.stringKey("langfuse.environment"), "test"));

        this.exporter.export(List.of(span));

        verify(this.delegate, never()).export(any());
    }

    private List<String> exportedSpanIds() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<SpanData>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(this.delegate).export(captor.capture());

        return captor.getValue().stream()
                .map(SpanData::getSpanId)
                .toList();
    }

    private static SpanData span(String name, String spanId, String parentSpanId, Attributes attributes) {
        var span = mock(SpanData.class);
        when(span.getName()).thenReturn(name);
        when(span.getSpanId()).thenReturn(spanId);
        when(span.getParentSpanId()).thenReturn(parentSpanId);
        when(span.getAttributes()).thenReturn(attributes);

        return span;
    }
}
