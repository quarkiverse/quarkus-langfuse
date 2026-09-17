package com.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LangfuseErrorBodyTests {

    private static final String RAW_BODY = "{\"message\":\"Dataset my-dataset not found\"}";

    @Test
    @DisplayName("of exposes the message, the code and the raw body")
    void ofExposesEveryField() {
        assertThat(LangfuseErrorBody.of("Dataset my-dataset not found", "resource_not_found", RAW_BODY))
                .returns("Dataset my-dataset not found", LangfuseErrorBody::message)
                .returns(Optional.of("resource_not_found"), LangfuseErrorBody::code)
                .returns(RAW_BODY, LangfuseErrorBody::rawBody);
    }

    @Test
    @DisplayName("of tolerates an absent code")
    void ofToleratesNullCode() {
        assertThat(LangfuseErrorBody.of("Dataset my-dataset not found", null, RAW_BODY))
                .returns("Dataset my-dataset not found", LangfuseErrorBody::message)
                .returns(Optional.empty(), LangfuseErrorBody::code)
                .returns(RAW_BODY, LangfuseErrorBody::rawBody);
    }

    @Test
    @DisplayName("of normalizes a null message and a null raw body to empty strings")
    void ofNormalizesNulls() {
        assertThat(LangfuseErrorBody.of(null, null, null))
                .returns("", LangfuseErrorBody::message)
                .returns(Optional.empty(), LangfuseErrorBody::code)
                .returns("", LangfuseErrorBody::rawBody);
    }

    @Test
    @DisplayName("opaque answers the raw body as the message and carries no code")
    void opaqueAnswersRawBodyAsMessage() {
        var html = "<html><head><title>520 Origin Error</title></head></html>";

        assertThat(LangfuseErrorBody.opaque(html))
                .returns(html, LangfuseErrorBody::message)
                .returns(Optional.empty(), LangfuseErrorBody::code)
                .returns(html, LangfuseErrorBody::rawBody);
    }

    @Test
    @DisplayName("opaque normalizes a null raw body to empty strings")
    void opaqueNormalizesNull() {
        assertThat(LangfuseErrorBody.opaque(null))
                .returns("", LangfuseErrorBody::message)
                .returns(Optional.empty(), LangfuseErrorBody::code)
                .returns("", LangfuseErrorBody::rawBody);
    }

    @Test
    @DisplayName("empty answers empty strings and carries no code")
    void emptyAnswersEmptyStrings() {
        assertThat(LangfuseErrorBody.empty())
                .returns("", LangfuseErrorBody::message)
                .returns(Optional.empty(), LangfuseErrorBody::code)
                .returns("", LangfuseErrorBody::rawBody);
    }

    @Test
    @DisplayName("the error body is serializable so it can travel on an exception")
    void isSerializable() {
        assertThat(LangfuseErrorBody.of("boom", "conflict", RAW_BODY))
                .isInstanceOf(java.io.Serializable.class);
    }
}
