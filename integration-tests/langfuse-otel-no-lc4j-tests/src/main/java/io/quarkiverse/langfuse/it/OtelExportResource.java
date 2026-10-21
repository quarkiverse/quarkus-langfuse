package io.quarkiverse.langfuse.it;

import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Tracer;
import io.quarkiverse.langfuse.runtime.otel.LangfuseSpanProcessor;

/**
 * Emits a GenAI span and flushes the {@link LangfuseSpanProcessor}, so a test can verify that the exporter works in the
 * running application, including when it is a native executable.
 */
@Path("/otel-export")
public class OtelExportResource {
    private static final AttributeKey<String> GEN_AI_OPERATION_NAME = AttributeKey.stringKey("gen_ai.operation.name");

    private final Tracer tracer;
    private final LangfuseSpanProcessor langfuseSpanProcessor;

    OtelExportResource(Tracer tracer, LangfuseSpanProcessor langfuseSpanProcessor) {
        this.tracer = tracer;
        this.langfuseSpanProcessor = langfuseSpanProcessor;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String export(@QueryParam("name") String spanName) {
        this.tracer.spanBuilder(spanName)
                .setAttribute(GEN_AI_OPERATION_NAME, "chat")
                .startSpan()
                .end();

        return Boolean.toString(this.langfuseSpanProcessor.forceFlush().join(10, TimeUnit.SECONDS).isSuccess());
    }
}
