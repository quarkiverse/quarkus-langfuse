package io.quarkiverse.langfuse.client.jaxrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.langfuse.api.LangfuseApiException;
import com.langfuse.api.LangfuseErrorBody;

import io.quarkiverse.langfuse.client.LangfuseAuthenticationException;
import io.quarkiverse.langfuse.client.LangfuseAuthorizationException;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;

class QuarkusLangfuseExceptionMapperTests {

    private static final String BODY = "{\"message\":\"something went wrong\"}";

    private final QuarkusLangfuseExceptionMapper mapper = new QuarkusLangfuseExceptionMapper();

    private static Response responseWith(int status) {
        return responseWith(status, MediaType.APPLICATION_JSON_TYPE, BODY);
    }

    private static Response responseWith(int status, MediaType mediaType, String body) {
        var response = mock(Response.class);
        when(response.getStatus()).thenReturn(status);
        when(response.getMediaType()).thenReturn(mediaType);
        when(response.readEntity(String.class)).thenReturn(body);

        return response;
    }

    private static Response notFoundWithJson(String body) {
        return responseWith(Status.NOT_FOUND.getStatusCode(), MediaType.APPLICATION_JSON_TYPE, body);
    }

    @Test
    void unauthorizedBecomesAnAuthenticationFailure() {
        var status = Status.UNAUTHORIZED.getStatusCode();

        assertThat(mapper.toThrowable(responseWith(status)))
                .isInstanceOf(LangfuseAuthenticationException.class)
                .isInstanceOf(LangfuseApiException.class)
                .hasMessageContaining(String.valueOf(status))
                .hasMessageContaining(BODY)
                .extracting(LangfuseApiException::getStatusCode)
                .isEqualTo(status);
    }

    @Test
    void forbiddenBecomesAnAuthorizationFailure() {
        var status = Status.FORBIDDEN.getStatusCode();

        assertThat(mapper.toThrowable(responseWith(status)))
                .isInstanceOf(LangfuseAuthorizationException.class)
                .isInstanceOf(LangfuseApiException.class)
                .hasMessageContaining(String.valueOf(status))
                .hasMessageContaining(BODY)
                .extracting(LangfuseApiException::getStatusCode)
                .isEqualTo(status);
    }

    @Test
    void notFoundKeepsItsDedicatedType() {
        var status = Status.NOT_FOUND.getStatusCode();

        assertThat(mapper.toThrowable(responseWith(status)))
                .isInstanceOf(LangfuseNotFoundException.class)
                .hasMessageContaining(String.valueOf(status))
                .extracting(LangfuseApiException::getStatusCode)
                .isEqualTo(status);
    }

    /**
     * {@code Status.fromStatusCode} answers {@code null} for codes outside the Jakarta RS enum, such
     * as nginx's 499 or Cloudflare's 520, so those must still map rather than failing the mapper.
     */
    @ParameterizedTest
    @ValueSource(ints = { 499, 520 })
    void statusesOutsideTheJakartaEnumStillMap(int status) {
        assertThat(mapper.toThrowable(responseWith(status)))
                .isExactlyInstanceOf(LangfuseApiException.class)
                .extracting(LangfuseApiException::getStatusCode)
                .isEqualTo(status);
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 409, 429 })
    void otherClientErrorsBecomeAGenericApiFailureCarryingTheStatus(int status) {
        assertThat(mapper.toThrowable(responseWith(status)))
                .isExactlyInstanceOf(LangfuseApiException.class)
                .hasMessageContaining(String.valueOf(status))
                .hasMessageContaining(BODY)
                .extracting(LangfuseApiException::getStatusCode)
                .isEqualTo(status);
    }

    @ParameterizedTest
    @ValueSource(ints = { 500, 503 })
    void serverErrorsCarryTheStatusInsteadOfBeingBareRuntimeExceptions(int status) {
        assertThat(mapper.toThrowable(responseWith(status)))
                .isExactlyInstanceOf(LangfuseApiException.class)
                .hasMessageContaining(String.valueOf(status))
                .extracting(LangfuseApiException::getStatusCode)
                .isEqualTo(status);
    }

    /**
     * A {@code null} return tells the REST Client the response was not an error, so it deserializes
     * the error body into the declared return type - the defect behind issue #96.
     */
    @ParameterizedTest
    @ValueSource(ints = { 400, 401, 403, 404, 409, 429, 499, 500, 503, 520 })
    void noFailureStatusIsEverTreatedAsASuccess(int status) {
        assertThat(mapper.toThrowable(responseWith(status))).isNotNull();
    }

    @Test
    void aMessageOnlyBodyExposesTheServersOwnWords() {
        assertThat(mapper.toThrowable(notFoundWithJson("{\"message\":\"Dataset X not found\"}")))
                .isInstanceOf(LangfuseNotFoundException.class)
                .extracting(LangfuseApiException::getServerMessage, LangfuseApiException::getErrorBody)
                .containsExactly("Dataset X not found",
                        LangfuseErrorBody.of("Dataset X not found", null, "{\"message\":\"Dataset X not found\"}"));
    }

    @Test
    void aBodyDeclaringACodeExposesIt() {
        assertThat(mapper.toThrowable(notFoundWithJson("{\"message\":\"nope\",\"code\":\"resource_not_found\"}")))
                .extracting(LangfuseApiException::getServerMessage, e -> e.getErrorBody().code())
                .containsExactly("nope", Optional.of("resource_not_found"));
    }

    /**
     * Langfuse names the machine-readable field {@code error} outside the endpoints that declare a
     * {@code PublicApiError}, so that name has to be honoured too.
     */
    @Test
    void anErrorFieldIsAcceptedAsTheCode() {
        var body = "{\"message\":\"No model with this id found\",\"error\":\"LangfuseNotFoundError\"}";

        assertThat(mapper.toThrowable(notFoundWithJson(body)))
                .extracting(LangfuseApiException::getServerMessage, e -> e.getErrorBody().code())
                .containsExactly("No model with this id found", Optional.of("LangfuseNotFoundError"));
    }

    /**
     * A proxy's HTML error page must be carried verbatim rather than parsed - the media type is what
     * keeps it away from the JSON parser entirely.
     */
    @Test
    void anHtmlBodyIsCarriedOpaquely() {
        var body = "<html><body>502 Bad Gateway</body></html>";

        assertThat(mapper.toThrowable(responseWith(502, MediaType.TEXT_HTML_TYPE, body)))
                .isExactlyInstanceOf(LangfuseApiException.class)
                .extracting(LangfuseApiException::getServerMessage, e -> e.getErrorBody().code(),
                        e -> e.getErrorBody().rawBody())
                .containsExactly(body, Optional.empty(), body);
    }

    @Test
    void anEmptyBodyFallsBackToTheFullMessage() {
        var thrown = mapper.toThrowable(responseWith(Status.NOT_FOUND.getStatusCode(), null, ""));

        assertThat(thrown)
                .isInstanceOf(LangfuseNotFoundException.class)
                .extracting(LangfuseApiException::getServerMessage, e -> e.getErrorBody().rawBody())
                .containsExactly(thrown.getMessage(), "");
    }

    /**
     * A JSON document is not necessarily an object; a bare scalar has no fields to read, so it is
     * carried opaquely instead of failing the mapper.
     */
    @ParameterizedTest
    @ValueSource(strings = { "\"just a string\"", "null", "[1,2,3]", "not json at all" })
    void aBodyThatIsNotAJsonObjectIsCarriedOpaquely(String body) {
        assertThat(mapper.toThrowable(notFoundWithJson(body)))
                .isInstanceOf(LangfuseNotFoundException.class)
                .extracting(LangfuseApiException::getServerMessage, e -> e.getErrorBody().code())
                .containsExactly(body, Optional.empty());
    }

    @Test
    void theResponseBodyIsReadExactlyOnce() {
        var response = responseWith(Status.UNAUTHORIZED.getStatusCode());

        mapper.toThrowable(response);

        verify(response, times(1)).readEntity(String.class);
    }
}
