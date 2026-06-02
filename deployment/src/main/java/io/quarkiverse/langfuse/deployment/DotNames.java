package io.quarkiverse.langfuse.deployment;

import org.jboss.jandex.DotName;

import com.langfuse.api.LangfuseApi;

import io.quarkiverse.langfuse.client.QuarkusLangfuseAsyncClient;
import io.quarkiverse.langfuse.client.QuarkusLangfuseClient;

public final class DotNames {
    public static final DotName QUARKUS_LANGFUSE_CLIENT = DotName.createSimple(QuarkusLangfuseClient.class);
    public static final DotName QUARKUS_LANGFUSE_ASYNC_CLIENT = DotName.createSimple(QuarkusLangfuseAsyncClient.class);
    public static final DotName LANGFUSE_API = DotName.createSimple(LangfuseApi.class);
    public static final DotName LANGFUSE_SPAN_PROCESSOR = DotName
            .createSimple("io.quarkiverse.langfuse.runtime.otel.LangfuseSpanProcessor");
    public static final DotName SPAN_PROCESSOR = DotName.createSimple("io.opentelemetry.sdk.trace.SpanProcessor");
    public static final DotName REGISTER_AI_SERVICE = DotName.createSimple("io.quarkiverse.langchain4j.RegisterAiService");

    private DotNames() {
    }
}
