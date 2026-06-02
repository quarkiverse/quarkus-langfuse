package io.quarkiverse.langfuse.runtime;

import java.util.function.Function;
import java.util.function.Supplier;

import com.langfuse.api.LangfuseApi;

import io.quarkiverse.langfuse.QuarkusLangfuseApi;
import io.quarkiverse.langfuse.client.LangfuseClientBuilder;
import io.quarkiverse.langfuse.client.QuarkusLangfuseAsyncClient;
import io.quarkiverse.langfuse.client.QuarkusLangfuseClient;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.runtime.otel.LangfuseSpanProcessor;
import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class LangfuseRecorder {
    private final RuntimeValue<LangfuseConfig> config;

    public LangfuseRecorder(RuntimeValue<LangfuseConfig> config) {
        this.config = config;
    }

    public Supplier<QuarkusLangfuseClient> quarkusLangfuseClient() {
        return new Supplier<QuarkusLangfuseClient>() {
            @Override
            public QuarkusLangfuseClient get() {
                return new LangfuseClientBuilder<>(config.getValue(), QuarkusLangfuseClient.class).build();
            }
        };
    }

    public Supplier<QuarkusLangfuseAsyncClient> quarkusLangfuseAsyncClient() {
        return new Supplier<QuarkusLangfuseAsyncClient>() {
            @Override
            public QuarkusLangfuseAsyncClient get() {
                return new LangfuseClientBuilder<>(config.getValue(), QuarkusLangfuseAsyncClient.class).build();
            }
        };
    }

    public Supplier<LangfuseSpanProcessor> langfuseSpanProcessor() {
        return new Supplier<LangfuseSpanProcessor>() {
            @Override
            public LangfuseSpanProcessor get() {
                return new LangfuseSpanProcessor(config.getValue());
            }
        };
    }

    public Function<SyntheticCreationalContext<LangfuseApi>, LangfuseApi> langfuseApi() {
        return new Function<SyntheticCreationalContext<LangfuseApi>, LangfuseApi>() {
            @Override
            public LangfuseApi apply(SyntheticCreationalContext<LangfuseApi> ctx) {
                return QuarkusLangfuseApi.builder()
                        .client(ctx.getInjectedReference(QuarkusLangfuseClient.class))
                        .asyncClient(ctx.getInjectedReference(QuarkusLangfuseAsyncClient.class))
                        .config(config.getValue())
                        .build();
            }
        };
    }
}
