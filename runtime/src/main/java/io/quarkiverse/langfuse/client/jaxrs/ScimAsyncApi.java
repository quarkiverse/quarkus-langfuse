package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import com.langfuse.api.model.ResourceTypesResponse;
import com.langfuse.api.model.SchemasResponse;
import com.langfuse.api.model.ScimUser;
import com.langfuse.api.model.ScimUsersListResponse;
import com.langfuse.api.model.ServiceProviderConfig;
import com.langfuse.api.scim.ScimApi.APIScimCreateUserRequest;
import com.langfuse.api.scim.ScimApi.APIScimDeleteUserRequest;
import com.langfuse.api.scim.ScimApi.APIScimGetUserRequest;
import com.langfuse.api.scim.ScimApi.APIScimListUsersRequest;

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Scim")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface ScimAsyncApi extends com.langfuse.api.scim.async.ScimApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scimCreateUser", method = "POST", path = "/api/public/scim/Users")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/scim/Users")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scim_createUser")
    @Override
    public CompletionStage<ScimUser> scimCreateUser(
            APIScimCreateUserRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scimDeleteUser", method = "DELETE", path = "/api/public/scim/Users/{userId}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/scim/Users/{userId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scim_deleteUser")
    @Override
    public CompletionStage<Object> scimDeleteUser(
            APIScimDeleteUserRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scimGetResourceTypes", method = "GET", path = "/api/public/scim/ResourceTypes")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/scim/ResourceTypes")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scim_getResourceTypes")
    @Override
    public CompletionStage<ResourceTypesResponse> scimGetResourceTypes();

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scimGetSchemas", method = "GET", path = "/api/public/scim/Schemas")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/scim/Schemas")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scim_getSchemas")
    @Override
    public CompletionStage<SchemasResponse> scimGetSchemas();

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scimGetServiceProviderConfig", method = "GET", path = "/api/public/scim/ServiceProviderConfig")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/scim/ServiceProviderConfig")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scim_getServiceProviderConfig")
    @Override
    public CompletionStage<ServiceProviderConfig> scimGetServiceProviderConfig();

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scimGetUser", method = "GET", path = "/api/public/scim/Users/{userId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/scim/Users/{userId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scim_getUser")
    @Override
    public CompletionStage<ScimUser> scimGetUser(
            APIScimGetUserRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scimListUsers", method = "GET", path = "/api/public/scim/Users")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/scim/Users")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scim_listUsers")
    @Override
    public CompletionStage<ScimUsersListResponse> scimListUsers(
            APIScimListUsersRequest apiRequest);

}
