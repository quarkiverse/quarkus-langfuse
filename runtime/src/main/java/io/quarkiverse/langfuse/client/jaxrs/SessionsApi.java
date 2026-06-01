package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.model.PaginatedSessions;
import com.langfuse.api.model.SessionWithTraces;
import com.langfuse.api.sessions.SessionsApi.APISessionsGetRequest;
import com.langfuse.api.sessions.SessionsApi.APISessionsListRequest;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Sessions")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface SessionsApi extends com.langfuse.api.sessions.SessionsApi {

    /**
     * Get a session.
     *
     * @param apiRequest {@link APISessionsGetRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "sessionsGet", method = "GET", path = "/api/public/sessions/{sessionId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/sessions/{sessionId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("sessions_get")
    @Override
    public SessionWithTraces sessionsGet(
            APISessionsGetRequest apiRequest);

    /**
     * Get sessions
     *
     * @param apiRequest {@link APISessionsListRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "sessionsList", method = "GET", path = "/api/public/sessions")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/sessions")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("sessions_list")
    @Override
    public PaginatedSessions sessionsList(
            APISessionsListRequest apiRequest);

}
