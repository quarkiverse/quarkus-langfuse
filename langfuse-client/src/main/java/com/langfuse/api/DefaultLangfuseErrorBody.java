package com.langfuse.api;

import java.util.Optional;

// code() builds its Optional on return rather than holding one: an Optional component would make
// the Optional part of this record's state, and that state travels inside every serialized
// LangfuseApiException - where Optional, not being Serializable, fails the write.
record DefaultLangfuseErrorBody(String message, String rawCode, String rawBody) implements LangfuseErrorBody {

    DefaultLangfuseErrorBody {
        message = (message != null) ? message : "";
        rawBody = (rawBody != null) ? rawBody : "";
    }

    @Override
    public Optional<String> code() {
        return Optional.ofNullable(rawCode);
    }
}
