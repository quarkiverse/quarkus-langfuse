package io.quarkiverse.langfuse.client;

import com.langfuse.api.spi.LangfuseApiBuilderFactory;

import io.quarkiverse.langfuse.QuarkusLangfuseApi;
import io.quarkiverse.langfuse.QuarkusLangfuseApi.QuarkusLangfuseApiBuilder;

public class QuarkusLangfuseApiBuilderFactory implements LangfuseApiBuilderFactory {
    @Override
    public QuarkusLangfuseApiBuilder getBuilder() {
        return QuarkusLangfuseApi.builder();
    }
}
