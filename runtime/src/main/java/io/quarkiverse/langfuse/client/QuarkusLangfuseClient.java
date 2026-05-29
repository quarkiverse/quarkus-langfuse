package io.quarkiverse.langfuse.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status.Family;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.logging.Log;
import io.quarkus.rest.client.reactive.ClientBasicAuth;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;

@Path("/")
@ClientBasicAuth(username = "${" + LangfuseConfig.USERNAME_KEY + "}", password = "${" + LangfuseConfig.PASSWORD_KEY + "}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface QuarkusLangfuseClient extends LangfuseApis {
    @ClientExceptionMapper
    static RuntimeException toException(Response response) {
        var message = "Langfuse API error (%d): %s".formatted(response.getStatus(), response.readEntity(String.class));

        if (response.getStatus() == 404) {
            return new LangfuseNotFoundException(message);
        }

        if (response.getStatusInfo().getFamily() == Family.CLIENT_ERROR) {
            Log.warn(message);
            return null;
        }

        return new RuntimeException(message);
    }
}
