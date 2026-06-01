package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.model.ResourceTypesResponse;
import com.langfuse.api.model.SchemasResponse;
import com.langfuse.api.model.ScimUser;
import com.langfuse.api.model.ScimUsersListResponse;
import com.langfuse.api.model.ServiceProviderConfig;
import com.langfuse.api.scim.ScimApi.APIScimCreateUserRequest;
import com.langfuse.api.scim.ScimApi.APIScimDeleteUserRequest;
import com.langfuse.api.scim.ScimApi.APIScimGetUserRequest;
import com.langfuse.api.scim.ScimApi.APIScimListUsersRequest;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Scim")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface ScimApi extends com.langfuse.api.scim.ScimApi {

    /**
     * Create a new user in the organization
     *
     * @param apiRequest {@link APIScimCreateUserRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scimCreateUser", method = "POST", path = "/api/public/scim/Users")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/scim/Users")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scim_createUser")
    @Override
    public ScimUser scimCreateUser(
            APIScimCreateUserRequest apiRequest);

    /**
     * Remove a user from the organization
     *
     * @param apiRequest {@link APIScimDeleteUserRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scimDeleteUser", method = "DELETE", path = "/api/public/scim/Users/{userId}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/scim/Users/{userId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scim_deleteUser")
    @Override
    public Object scimDeleteUser(
            APIScimDeleteUserRequest apiRequest);

    /**
     * Get SCIM Resource Types
     *
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scimGetResourceTypes", method = "GET", path = "/api/public/scim/ResourceTypes")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/scim/ResourceTypes")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scim_getResourceTypes")
    @Override
    public ResourceTypesResponse scimGetResourceTypes();

    /**
     * Get SCIM Schemas
     *
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scimGetSchemas", method = "GET", path = "/api/public/scim/Schemas")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/scim/Schemas")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scim_getSchemas")
    @Override
    public SchemasResponse scimGetSchemas();

    /**
     * Get SCIM Service Provider Configuration
     *
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scimGetServiceProviderConfig", method = "GET", path = "/api/public/scim/ServiceProviderConfig")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/scim/ServiceProviderConfig")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scim_getServiceProviderConfig")
    @Override
    public ServiceProviderConfig scimGetServiceProviderConfig();

    /**
     * Get a specific user by ID
     *
     * @param apiRequest {@link APIScimGetUserRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scimGetUser", method = "GET", path = "/api/public/scim/Users/{userId}")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/scim/Users/{userId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scim_getUser")
    @Override
    public ScimUser scimGetUser(
            APIScimGetUserRequest apiRequest);

    /**
     * List users in the organization
     *
     * @param apiRequest {@link APIScimListUsersRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "scimListUsers", method = "GET", path = "/api/public/scim/Users")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/scim/Users")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("scim_listUsers")
    @Override
    public ScimUsersListResponse scimListUsers(
            APIScimListUsersRequest apiRequest);

}
