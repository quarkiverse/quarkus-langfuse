package io.quarkiverse.langfuse.runtime.otel;

import java.util.Collection;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

sealed abstract class DelegatingSpanExporter implements SpanExporter
        permits FilteringAISpanExporter, LangfuseAttributeEnrichingSpanExporter {
    private final SpanExporter delegate;

    protected DelegatingSpanExporter(SpanExporter delegate) {
        this.delegate = delegate;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        return delegate.export(spans);
    }

    @Override
    public CompletableResultCode flush() {
        return delegate.flush();
    }

    @Override
    public CompletableResultCode shutdown() {
        return delegate.shutdown();
    }

    @Override
    public void close() {
        this.delegate.close();
    }
}
