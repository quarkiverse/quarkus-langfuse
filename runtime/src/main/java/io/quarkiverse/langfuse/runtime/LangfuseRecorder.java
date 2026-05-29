package io.quarkiverse.langfuse.runtime;

import java.util.function.Function;
import java.util.function.Supplier;

import io.quarkiverse.langfuse.LangfuseClient;
import io.quarkiverse.langfuse.client.LangfuseClientBuilder;
import io.quarkiverse.langfuse.client.QuarkusLangfuseClient;
import io.quarkiverse.langfuse.config.LangfuseConfig;
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
                return new LangfuseClientBuilder(config.getValue()).build();
            }
        };
    }

    public Function<SyntheticCreationalContext<LangfuseClient>, LangfuseClient> langfuseClient() {
        return new Function<SyntheticCreationalContext<LangfuseClient>, LangfuseClient>() {
            @Override
            public LangfuseClient apply(SyntheticCreationalContext<LangfuseClient> ctx) {
                return LangfuseClient.builder()
                        .client(ctx.getInjectedReference(QuarkusLangfuseClient.class))
                        .config(config.getValue())
                        .build();
            }
        };
    }
}
