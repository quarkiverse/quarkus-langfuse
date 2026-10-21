package io.quarkiverse.langfuse.runtime;

import java.util.Optional;
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
import io.vertx.core.Vertx;

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

    public Supplier<LangfuseSpanProcessor> langfuseSpanProcessor(String exporterFactoryClassName,
            Optional<RuntimeValue<Boolean>> otelSdkEnabled, Supplier<Vertx> vertx) {
        return otelSdkEnabled
                .filter(RuntimeValue::getValue)
                .<Supplier<LangfuseSpanProcessor>> map(
                        ignored -> new Supplier<LangfuseSpanProcessor>() {
                            @Override
                            public LangfuseSpanProcessor get() {
                                return LangfuseSpanProcessor.create(config.getValue(), vertx.get(),
                                        exporterFactoryClassName);
                            }
                        })
                .orElseGet(new Supplier<Supplier<LangfuseSpanProcessor>>() {
                    @Override
                    public Supplier<LangfuseSpanProcessor> get() {
                        return LangfuseSpanProcessor::noop;
                    }
                });
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
