package io.quarkiverse.langfuse.runtime.otel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLHandshakeException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.opentelemetry.exporter.internal.FailedExportException;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.HttpResponse;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

class FailureLoggingSpanExporterTests {
    private static final URI TARGET_URL = URI.create("https://langfuse.example.com/api/public/otel/v1/traces");

    private SpanExporter delegate;
    private FailureLoggingSpanExporter exporter;

    @BeforeEach
    void setUp() {
        this.delegate = mock(SpanExporter.class);
        this.exporter = new FailureLoggingSpanExporter(this.delegate, TARGET_URL);
    }

    @Test
    void successfulExportIsPassedThrough() {
        var spans = List.of(mock(SpanData.class));
        when(this.delegate.export(any())).thenReturn(CompletableResultCode.ofSuccess());

        assertThat(this.exporter.export(spans).join(1, TimeUnit.SECONDS))
                .returns(true, CompletableResultCode::isSuccess)
                .returns(null, CompletableResultCode::getFailureThrowable);

        verify(this.delegate).export(spans);
    }

    @Test
    void failedExportWithCauseIsPassedThrough() {
        var cause = new IllegalStateException("TLS handshake failed");
        when(this.delegate.export(any())).thenReturn(CompletableResultCode.ofExceptionalFailure(cause));

        assertThat(this.exporter.export(List.of(mock(SpanData.class))).join(1, TimeUnit.SECONDS))
                .returns(false, CompletableResultCode::isSuccess)
                .returns(cause, CompletableResultCode::getFailureThrowable);
    }

    @Test
    void failedExportWithoutCauseIsPassedThrough() {
        when(this.delegate.export(any())).thenReturn(CompletableResultCode.ofFailure());

        assertThat(this.exporter.export(List.of(mock(SpanData.class))).join(1, TimeUnit.SECONDS))
                .returns(false, CompletableResultCode::isSuccess)
                .returns(null, CompletableResultCode::getFailureThrowable);
    }

    @Test
    void pendingExportCompletesWithDelegateOutcome() {
        var pending = new CompletableResultCode();
        when(this.delegate.export(any())).thenReturn(pending);

        var result = this.exporter.export(List.of(mock(SpanData.class)));
        assertThat(result.isDone()).isFalse();

        pending.fail();

        assertThat(result)
                .returns(true, CompletableResultCode::isDone)
                .returns(false, CompletableResultCode::isSuccess);
    }

    @Test
    void describesHttpRejectionWithStatus() {
        var response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(400);
        when(response.getStatusMessage()).thenReturn("Bad Request");

        assertThat(FailureLoggingSpanExporter.describe(FailedExportException.httpFailedWithResponse(response)))
                .isEqualTo("server responded with HTTP 400 Bad Request");
    }

    @Test
    void describesHttpRejectionWithoutStatusMessage() {
        var response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(503);

        assertThat(FailureLoggingSpanExporter.describe(FailedExportException.httpFailedWithResponse(response)))
                .isEqualTo("server responded with HTTP 503");
    }

    @Test
    void describesHttpExecutionFailureWithRootCause() {
        var rootCause = new SSLHandshakeException("unable to find valid certification path to requested target");
        var failure = FailedExportException.httpFailedExceptionally(
                new SSLHandshakeException("Failed to create SSL connection").initCause(rootCause));

        assertThat(FailureLoggingSpanExporter.describe(failure))
                .isEqualTo(rootCause.toString());
    }

    @Test
    void describesOtherFailureWithItself() {
        var failure = new IllegalStateException("boom");

        assertThat(FailureLoggingSpanExporter.describe(failure))
                .isEqualTo(failure.toString());
    }

    @Test
    void flushAndShutdownAreDelegated() {
        when(this.delegate.flush()).thenReturn(CompletableResultCode.ofSuccess());
        when(this.delegate.shutdown()).thenReturn(CompletableResultCode.ofSuccess());

        assertThat(List.of(this.exporter.flush(), this.exporter.shutdown()))
                .allMatch(CompletableResultCode::isSuccess);

        verify(this.delegate).flush();
        verify(this.delegate).shutdown();
    }
}
