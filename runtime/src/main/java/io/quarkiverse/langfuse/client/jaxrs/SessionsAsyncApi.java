package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.model.PaginatedSessions;
import com.langfuse.api.model.SessionWithTraces;
import com.langfuse.api.sessions.SessionsApi.APISessionsGetRequest;
import com.langfuse.api.sessions.SessionsApi.APISessionsListRequest;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Sessions")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface SessionsAsyncApi extends com.langfuse.api.sessions.async.SessionsApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "sessionsGet", method = "GET", path = "/api/public/sessions/{sessionId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/sessions/{sessionId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("sessions_get")
    @Override
    public CompletionStage<SessionWithTraces> sessionsGet(
            APISessionsGetRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "sessionsList", method = "GET", path = "/api/public/sessions")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/sessions")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("sessions_list")
    @Override
    public CompletionStage<PaginatedSessions> sessionsList(
            APISessionsListRequest apiRequest);

}
