package io.quarkiverse.langfuse.runtime.otel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.incubating.GenAiIncubatingAttributes;
import io.quarkiverse.langfuse.config.LangfuseConfig;

class LangfuseAttributeEnrichingSpanExporterTests {
    private static final String ENVIRONMENT = "test-env";

    private SpanExporter delegate;
    private LangfuseAttributeEnrichingSpanExporter exporter;

    @BeforeEach
    void setUp() {
        var config = mock(LangfuseConfig.class);
        when(config.environment()).thenReturn(ENVIRONMENT);

        this.delegate = mock(SpanExporter.class);
        when(this.delegate.export(any())).thenReturn(CompletableResultCode.ofSuccess());
        this.exporter = new LangfuseAttributeEnrichingSpanExporter(this.delegate, config);
    }

    @Test
    void aiSpanGetsEnriched() {
        var attributes = Attributes.builder()
                .put(GenAiIncubatingAttributes.GEN_AI_PROMPT, "What is Quarkus?")
                .put(GenAiIncubatingAttributes.GEN_AI_COMPLETION, "Quarkus is a Java framework.")
                .build();

        var span = mockSpanData(attributes);
        this.exporter.export(List.of(span));

        var exported = captureExportedSpans();
        assertThat(exported).hasSize(1);

        var enrichedAttributes = exported.iterator().next().getAttributes();
        assertThat(enrichedAttributes.get(LangfuseAttributeEnrichingSpanExporter.LANGFUSE_ENVIRONMENT))
                .isEqualTo(ENVIRONMENT);
        assertThat(enrichedAttributes.get(LangfuseAttributeEnrichingSpanExporter.LANGFUSE_TRACE_INPUT))
                .isEqualTo("What is Quarkus?");
        assertThat(enrichedAttributes.get(LangfuseAttributeEnrichingSpanExporter.LANGFUSE_TRACE_OUTPUT))
                .isEqualTo("Quarkus is a Java framework.");
    }

    @Test
    void nonAiSpanPassedThrough() {
        var attributes = Attributes.builder()
                .put("http.method", "GET")
                .build();

        var span = mockSpanData(attributes);
        this.exporter.export(List.of(span));

        var exported = captureExportedSpans();
        assertThat(exported).hasSize(1);

        var exportedAttributes = exported.iterator().next().getAttributes();
        assertThat(exportedAttributes.get(LangfuseAttributeEnrichingSpanExporter.LANGFUSE_ENVIRONMENT)).isNull();
        assertThat(exportedAttributes.get(LangfuseAttributeEnrichingSpanExporter.LANGFUSE_TRACE_INPUT)).isNull();
        assertThat(exportedAttributes.get(LangfuseAttributeEnrichingSpanExporter.LANGFUSE_TRACE_OUTPUT)).isNull();
    }

    @Test
    void promptOnlySpanGetsEnvironmentAndInput() {
        var attributes = Attributes.builder()
                .put(GenAiIncubatingAttributes.GEN_AI_PROMPT, "Hello")
                .build();

        var span = mockSpanData(attributes);
        this.exporter.export(List.of(span));

        var exported = captureExportedSpans();
        assertThat(exported).hasSize(1);

        var enrichedAttributes = exported.iterator().next().getAttributes();
        assertThat(enrichedAttributes.get(LangfuseAttributeEnrichingSpanExporter.LANGFUSE_ENVIRONMENT))
                .isEqualTo(ENVIRONMENT);
        assertThat(enrichedAttributes.get(LangfuseAttributeEnrichingSpanExporter.LANGFUSE_TRACE_INPUT))
                .isEqualTo("Hello");
        assertThat(enrichedAttributes.get(LangfuseAttributeEnrichingSpanExporter.LANGFUSE_TRACE_OUTPUT)).isNull();
    }

    @SuppressWarnings("unchecked")
    private Collection<SpanData> captureExportedSpans() {
        var captor = ArgumentCaptor.forClass(Collection.class);
        verify(this.delegate).export(captor.capture());
        return (Collection<SpanData>) captor.getValue();
    }

    private static SpanData mockSpanData(Attributes attributes) {
        var span = mock(SpanData.class);
        when(span.getAttributes()).thenReturn(attributes);
        return span;
    }
}