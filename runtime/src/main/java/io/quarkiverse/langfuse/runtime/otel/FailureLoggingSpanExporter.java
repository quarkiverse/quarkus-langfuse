package io.quarkiverse.langfuse.runtime.otel;

import java.net.URI;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import io.opentelemetry.exporter.internal.FailedExportException.HttpExportException;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.quarkus.logging.Log;

/**
 * A wrapping {@link SpanExporter} that logs a warning, including the target URL and the failure cause when there is
 * one, whenever a batch of spans could not be exported to Langfuse.
 * <p>
 * The OTel {@code BatchSpanProcessor} only reports failed exports at {@code FINE} level, which hides problems such as
 * TLS handshake failures or HTTP error responses from the Langfuse ingestion endpoint. This exporter wraps the raw OTLP
 * exporter so that exactly one warning is logged per failed batch, while successful batches stay silent.
 * </p>
 * <p>
 * The warning describes the cause in a single line, to keep repeated failures (e.g. while Langfuse is unreachable)
 * readable: the HTTP status for rejected requests, or the root cause for requests that could not be executed. The full
 * stack trace is logged at {@code DEBUG} level.
 * </p>
 */
final class FailureLoggingSpanExporter extends DelegatingSpanExporter {
    private static final int MAX_CAUSE_DEPTH = 16;

    private final URI targetUrl;

    FailureLoggingSpanExporter(SpanExporter delegate, URI targetUrl) {
        super(delegate);
        this.targetUrl = targetUrl;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        var spanCount = spans.size();
        var result = super.export(spans);

        return result.whenComplete(() -> logFailure(result, spanCount));
    }

    private void logFailure(CompletableResultCode result, int spanCount) {
        if (!result.isSuccess()) {
            Optional.ofNullable(result.getFailureThrowable())
                    .ifPresentOrElse(
                            failure -> logFailureWithCause(failure, spanCount),
                            () -> Log.warnf("Failed to export %d span(s) to Langfuse at %s", spanCount, this.targetUrl));
        }
    }

    private void logFailureWithCause(Throwable failure, int spanCount) {
        Log.warnf("Failed to export %d span(s) to Langfuse at %s: %s", spanCount, this.targetUrl, describe(failure));
        Log.debugf(failure, "Langfuse span export failure details for %s", this.targetUrl);
    }

    static String describe(Throwable failure) {
        return Optional.of(failure)
                .filter(HttpExportException.class::isInstance)
                .map(HttpExportException.class::cast)
                .filter(HttpExportException::failedWithResponse)
                .map(HttpExportException::getResponse)
                .map(response -> "server responded with HTTP %d %s".formatted(response.getStatusCode(),
                        Objects.toString(response.getStatusMessage(), ""))
                        .strip())
                .orElseGet(() -> rootCause(failure).toString());
    }

    private static Throwable rootCause(Throwable failure) {
        return Stream.iterate(failure, Objects::nonNull, Throwable::getCause)
                .limit(MAX_CAUSE_DEPTH)
                .reduce((outer, cause) -> cause)
                .orElse(failure);
    }
}
