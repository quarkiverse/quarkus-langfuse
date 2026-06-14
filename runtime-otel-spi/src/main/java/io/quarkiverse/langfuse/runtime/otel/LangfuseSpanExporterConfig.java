package io.quarkiverse.langfuse.runtime.otel;

import java.net.URI;

import io.opentelemetry.sdk.common.export.MemoryMode;
import io.vertx.core.Vertx;

public class LangfuseSpanExporterConfig {
    private final URI baseUri;
    private final String signalPath;
    private final String authHeader;
    private final String traceIngestionUrl;
    private final Vertx vertx;
    private final MemoryMode memoryMode;

    private LangfuseSpanExporterConfig(Builder builder) {
        this.baseUri = builder.baseUri;
        this.signalPath = builder.signalPath;
        this.authHeader = builder.authHeader;
        this.traceIngestionUrl = builder.traceIngestionUrl;
        this.vertx = builder.vertx;
        this.memoryMode = builder.memoryMode;
    }

    public URI getBaseUri() {
        return baseUri;
    }

    public String getSignalPath() {
        return signalPath;
    }

    public String getAuthHeader() {
        return authHeader;
    }

    public String getTraceIngestionUrl() {
        return traceIngestionUrl;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public MemoryMode getMemoryMode() {
        return memoryMode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private URI baseUri;
        private String signalPath;
        private String authHeader;
        private String traceIngestionUrl;
        private Vertx vertx;
        private MemoryMode memoryMode;

        private Builder() {
        }

        public Builder baseUri(URI baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        public Builder signalPath(String signalPath) {
            this.signalPath = signalPath;
            return this;
        }

        public Builder authHeader(String authHeader) {
            this.authHeader = authHeader;
            return this;
        }

        public Builder traceIngestionUrl(String traceIngestionUrl) {
            this.traceIngestionUrl = traceIngestionUrl;
            return this;
        }

        public Builder vertx(Vertx vertx) {
            this.vertx = vertx;
            return this;
        }

        public Builder memoryMode(MemoryMode memoryMode) {
            this.memoryMode = memoryMode;
            return this;
        }

        public LangfuseSpanExporterConfig build() {
            return new LangfuseSpanExporterConfig(this);
        }
    }
}
