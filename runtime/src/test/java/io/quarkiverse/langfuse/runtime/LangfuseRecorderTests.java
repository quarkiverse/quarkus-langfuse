package io.quarkiverse.langfuse.runtime;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.runtime.RuntimeValue;

class LangfuseRecorderTests {
    @Test
    void noBaseUrlSet() {
        var config = mock(LangfuseConfig.class);
        var recorder = new LangfuseRecorder(new RuntimeValue<>(config));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> recorder.quarkusLangfuseClient().get())
                .withFailMessage("'quarkus.langfuse.base-url' cannot be null or empty");
    }

    @Test
    void noUsernameSet() {
        var config = mock(LangfuseConfig.class);
        when(config.baseUrl()).thenReturn("http://localhost:8080");

        var recorder = new LangfuseRecorder(new RuntimeValue<>(config));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> recorder.quarkusLangfuseClient().get())
                .withFailMessage("'quarkus.langfuse.username' cannot be null or empty");
    }

    @Test
    void noPasswordSet() {
        var config = mock(LangfuseConfig.class);
        when(config.baseUrl()).thenReturn("http://localhost:8080");
        when(config.username()).thenReturn("username");

        var recorder = new LangfuseRecorder(new RuntimeValue<>(config));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> recorder.quarkusLangfuseClient().get())
                .withFailMessage("'quarkus.langfuse.password' cannot be null or empty");
    }
}
