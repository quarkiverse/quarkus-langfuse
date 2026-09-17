package io.quarkiverse.langfuse.client.jaxrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.langfuse.api.LangfuseApiException;

import io.quarkiverse.langfuse.client.LangfuseAuthenticationException;
import io.quarkiverse.langfuse.client.LangfuseAuthorizationException;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;

class QuarkusLangfuseExceptionMapperTests {

    private static final String BODY = "{\"message\":\"something went wrong\"}";

    private final QuarkusLangfuseExceptionMapper mapper = new QuarkusLangfuseExceptionMapper();

    private static Response responseWith(int status) {
        var response = mock(Response.class);
        when(response.getStatus()).thenReturn(status);
        when(response.readEntity(String.class)).thenReturn(BODY);

        return response;
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
    void theResponseBodyIsReadExactlyOnce() {
        var response = responseWith(Status.UNAUTHORIZED.getStatusCode());

        mapper.toThrowable(response);

        verify(response, times(1)).readEntity(String.class);
    }
}
