package io.quarkiverse.langfuse.client;

public class LangfuseNotFoundException extends RuntimeException {
    public LangfuseNotFoundException(String message) {
        super(message);
    }
}
