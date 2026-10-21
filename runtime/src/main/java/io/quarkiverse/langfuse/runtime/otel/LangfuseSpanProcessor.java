package io.quarkiverse.langfuse.runtime.otel;

import java.net.URI;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.semconv.incubating.GenAiIncubatingAttributes;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.config.LangfuseOtelConfig.SpanFilterType;
import io.vertx.core.Vertx;

/**
 * Implementation of the {@link SpanProcessor} interface for integrating with Langfuse.
 * This processor acts as a wrapper around an underlying {@link SpanProcessor}
 * that is configured based on Langfuse-specific settings.
 *
 * The processor sends span data to the Langfuse server via the OTLP HTTP exporter.
 * The configuration for the server endpoint, authentication, and other runtime
 * settings are derived from a {@link LangfuseConfig} instance.
 *
 * The Langfuse processor supports filtering spans based on the provided
 * {@link SpanFilterType}. Depending on the filter, it may include all spans or
 * limit the scope to AI-related spans.
 */
public class LangfuseSpanProcessor implements SpanProcessor {
    private static final Logger LOG = Logger.getLogger(LangfuseSpanProcessor.class);
    private static final Duration EXPORT_TIMEOUT = Duration.ofSeconds(10);
    private static final String INGESTION_VERSION_HEADER = "x-langfuse-ingestion-version";
    private static final String INGESTION_VERSION = "4";
    private static final String PROTOBUF_CONTENT_TYPE = "application/x-protobuf";

    private final SpanProcessor delegate;

    public LangfuseSpanProcessor(LangfuseConfig langfuseConfig, Vertx vertx, LangfuseSpanExporterFactory exporterFactory) {
        super();
        this.delegate = BatchSpanProcessor
                .builder(createActualExporter(langfuseConfig, vertx, exporterFactory))
                .build();
    }

    private LangfuseSpanProcessor() {
        this.delegate = null;
    }

    public static LangfuseSpanProcessor create(LangfuseConfig langfuseConfig, Vertx vertx,
            String exporterFactoryClassName) {
        return new LangfuseSpanProcessor(langfuseConfig, vertx, loadExporterFactory(exporterFactoryClassName));
    }

    public static LangfuseSpanProcessor noop() {
        return new LangfuseSpanProcessor();
    }

    private static LangfuseSpanExporterFactory loadExporterFactory(String className) {
        LOG.debugf("Using Langfuse span exporter factory %s", className);

        try {
            return (LangfuseSpanExporterFactory) Thread.currentThread()
                    .getContextClassLoader()
                    .loadClass(className)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to instantiate LangfuseSpanExporterFactory: %s".formatted(className), e);
        }
    }

    public boolean isNoop() {
        return this.delegate == null;
    }

    private static SpanExporter createActualExporter(LangfuseConfig langfuseConfig, Vertx vertx,
            LangfuseSpanExporterFactory exporterFactory) {
        LOG.debug("Initializing Langfuse OTLP Span Processor");

        var credentials = "%s:%s".formatted(langfuseConfig.publicKey(), langfuseConfig.secretKey());
        var authHeader = "Basic %s".formatted(Base64.getEncoder().encodeToString(credentials.getBytes()));
        var baseUri = URI.create(langfuseConfig.baseUrl());
        var signalPath = langfuseConfig.otel().traceIngestionPath();

        if (!signalPath.startsWith("/")) {
            signalPath = "/" + signalPath;
        }

        var memoryMode = ConfigProvider.getConfig()
                .getOptionalValue("quarkus.otel.exporter.otlp.memory-mode", MemoryMode.class)
                .orElse(MemoryMode.IMMUTABLE_DATA);

        var exporterConfig = new LangfuseSpanExporterConfig(
                baseUri,
                signalPath,
                EXPORT_TIMEOUT,
                Map.of("Authorization", authHeader, INGESTION_VERSION_HEADER, INGESTION_VERSION),
                PROTOBUF_CONTENT_TYPE,
                memoryMode,
                vertx);

        var exporter = exporterFactory.createExporter(exporterConfig);
        var filteredExporter = switch (langfuseConfig.otel().spanFilter()) {
            case ALL -> exporter;
            case AI_ONLY -> new FilteringAISpanExporter(exporter);
        };

        return new LangfuseAttributeEnrichingSpanExporter(filteredExporter, langfuseConfig);
    }

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        Optional.ofNullable(
                Baggage.fromContext(parentContext).getEntryValue(GenAiIncubatingAttributes.GEN_AI_CONVERSATION_ID.getKey()))
                .ifPresent(
                        conversationId -> span.setAttribute(GenAiIncubatingAttributes.GEN_AI_CONVERSATION_ID, conversationId));

        this.delegate.onStart(parentContext, span);
    }

    @Override
    public boolean isStartRequired() {
        return !isNoop();
    }

    @Override
    public void onEnd(ReadableSpan span) {
        if (!isNoop()) {
            this.delegate.onEnd(span);
        }
    }

    @Override
    public boolean isEndRequired() {
        return !isNoop() && this.delegate.isEndRequired();
    }

    @Override
    public CompletableResultCode shutdown() {
        return isNoop() ? CompletableResultCode.ofSuccess() : this.delegate.shutdown();
    }

    @Override
    public CompletableResultCode forceFlush() {
        return isNoop() ? CompletableResultCode.ofSuccess() : this.delegate.forceFlush();
    }

    @Override
    public void close() {
        if (!isNoop()) {
            this.delegate.close();
        }
    }
}
