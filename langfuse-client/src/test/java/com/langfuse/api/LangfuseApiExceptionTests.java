package com.langfuse.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LangfuseApiExceptionTests {

    private static final String RAW_BODY = "{\"message\":\"Dataset my-dataset not found\"}";
    private static final String MESSAGE = "Langfuse API error (404): " + RAW_BODY;

    @Test
    @DisplayName("every pre-existing constructor answers a non-null empty error body")
    void legacyConstructorsAnswerAnEmptyBody() {
        var cause = new IllegalStateException("transport blew up");

        assertThat(List.of(
                new LangfuseApiException("boom"),
                new LangfuseApiException("boom", cause),
                new LangfuseApiException("boom", 404),
                new LangfuseApiException("boom", 404, cause),
                new LangfuseApiException(cause)))
                .extracting(LangfuseApiException::getErrorBody)
                .doesNotContainNull()
                .allSatisfy(body -> assertThat(body)
                        .returns("", LangfuseErrorBody::message)
                        .returns(Optional.empty(), LangfuseErrorBody::code)
                        .returns("", LangfuseErrorBody::rawBody));
    }

    @Test
    @DisplayName("getServerMessage falls back to getMessage when no body was supplied")
    void serverMessageFallsBackToMessage() {
        assertThatObject(new LangfuseApiException(MESSAGE, 404))
                .returns(MESSAGE, LangfuseApiException::getMessage)
                .returns(MESSAGE, LangfuseApiException::getServerMessage)
                .returns(404, LangfuseApiException::getStatusCode);
    }

    @Test
    @DisplayName("a body-carrying constructor round-trips the message, the code and the raw body")
    void bodyCarryingConstructorRoundTripsTheBody() {
        var errorBody = LangfuseErrorBody.of("Dataset my-dataset not found", "resource_not_found", RAW_BODY);

        assertThatObject(new LangfuseApiException(MESSAGE, 404, errorBody))
                .returns(MESSAGE, LangfuseApiException::getMessage)
                .returns(404, LangfuseApiException::getStatusCode)
                .returns("Dataset my-dataset not found", LangfuseApiException::getServerMessage)
                .extracting(LangfuseApiException::getErrorBody)
                .returns("Dataset my-dataset not found", LangfuseErrorBody::message)
                .returns(Optional.of("resource_not_found"), LangfuseErrorBody::code)
                .returns(RAW_BODY, LangfuseErrorBody::rawBody);
    }

    @Test
    @DisplayName("a null error body is normalized rather than stored")
    void nullErrorBodyIsNormalized() {
        assertThatObject(new LangfuseApiException(MESSAGE, 404, (LangfuseErrorBody) null))
                .returns(MESSAGE, LangfuseApiException::getServerMessage)
                .extracting(LangfuseApiException::getErrorBody)
                .returns("", LangfuseErrorBody::message)
                .returns("", LangfuseErrorBody::rawBody);
    }

    @Test
    @DisplayName("the exception survives a serialization round-trip with its error body intact")
    void survivesSerializationRoundTrip() throws Exception {
        var errorBody = LangfuseErrorBody.of("Dataset my-dataset not found", "resource_not_found", RAW_BODY);
        var original = new LangfuseApiException(MESSAGE, 404, errorBody);

        assertThatObject(roundTrip(original))
                .isInstanceOf(LangfuseApiException.class)
                .returns(MESSAGE, LangfuseApiException::getMessage)
                .returns(404, LangfuseApiException::getStatusCode)
                .returns("Dataset my-dataset not found", LangfuseApiException::getServerMessage)
                .extracting(LangfuseApiException::getErrorBody)
                .returns("Dataset my-dataset not found", LangfuseErrorBody::message)
                .returns(Optional.of("resource_not_found"), LangfuseErrorBody::code)
                .returns(RAW_BODY, LangfuseErrorBody::rawBody);
    }

    @Test
    @DisplayName("an exception carrying an empty body also survives a serialization round-trip")
    void survivesSerializationRoundTripWithoutABody() throws Exception {
        assertThatObject(roundTrip(new LangfuseApiException(MESSAGE, 404)))
                .returns(MESSAGE, LangfuseApiException::getServerMessage)
                .extracting(LangfuseApiException::getErrorBody)
                .isNotNull()
                .returns("", LangfuseErrorBody::message);
    }

    private static LangfuseApiException roundTrip(LangfuseApiException exception) throws Exception {
        var bytes = new ByteArrayOutputStream();

        try (var out = new ObjectOutputStream(bytes)) {
            out.writeObject(exception);
        }

        try (var in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (LangfuseApiException) in.readObject();
        }
    }
}
