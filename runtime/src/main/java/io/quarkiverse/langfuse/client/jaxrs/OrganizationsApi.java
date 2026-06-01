package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.model.MembershipDeletionResponse;
import com.langfuse.api.model.MembershipResponse;
import com.langfuse.api.model.MembershipsResponse;
import com.langfuse.api.model.OrganizationApiKeysResponse;
import com.langfuse.api.model.OrganizationProjectsResponse;
import com.langfuse.api.organizations.OrganizationsApi.APIOrganizationsDeleteOrganizationMembershipRequest;
import com.langfuse.api.organizations.OrganizationsApi.APIOrganizationsDeleteProjectMembershipRequest;
import com.langfuse.api.organizations.OrganizationsApi.APIOrganizationsGetProjectMembershipsRequest;
import com.langfuse.api.organizations.OrganizationsApi.APIOrganizationsUpdateOrganizationMembershipRequest;
import com.langfuse.api.organizations.OrganizationsApi.APIOrganizationsUpdateProjectMembershipRequest;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Organizations")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface OrganizationsApi extends com.langfuse.api.organizations.OrganizationsApi {

    /**
     * Delete a membership from the organization associated with the API key
     *
     * @param apiRequest {@link APIOrganizationsDeleteOrganizationMembershipRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "organizationsDeleteOrganizationMembership", method = "DELETE", path = "/api/public/organizations/memberships")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/organizations/memberships")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("organizations_deleteOrganizationMembership")
    @Override
    public MembershipDeletionResponse organizationsDeleteOrganizationMembership(
            APIOrganizationsDeleteOrganizationMembershipRequest apiRequest);

    /**
     * Delete a membership from a specific project
     *
     * @param apiRequest {@link APIOrganizationsDeleteProjectMembershipRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "organizationsDeleteProjectMembership", method = "DELETE", path = "/api/public/projects/{projectId}/memberships")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/projects/{projectId}/memberships")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("organizations_deleteProjectMembership")
    @Override
    public MembershipDeletionResponse organizationsDeleteProjectMembership(
            APIOrganizationsDeleteProjectMembershipRequest apiRequest);

    /**
     * Get all API keys for the organization associated with the API key
     *
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "organizationsGetOrganizationApiKeys", method = "GET", path = "/api/public/organizations/apiKeys")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/organizations/apiKeys")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("organizations_getOrganizationApiKeys")
    @Override
    public OrganizationApiKeysResponse organizationsGetOrganizationApiKeys();

    /**
     * Get all memberships for the organization associated with the API key
     *
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "organizationsGetOrganizationMemberships", method = "GET", path = "/api/public/organizations/memberships")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/organizations/memberships")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("organizations_getOrganizationMemberships")
    @Override
    public MembershipsResponse organizationsGetOrganizationMemberships();

    /**
     * Get all projects for the organization associated with the API key
     *
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "organizationsGetOrganizationProjects", method = "GET", path = "/api/public/organizations/projects")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/organizations/projects")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("organizations_getOrganizationProjects")
    @Override
    public OrganizationProjectsResponse organizationsGetOrganizationProjects();

    /**
     * Get all memberships for a specific project
     *
     * @param apiRequest {@link APIOrganizationsGetProjectMembershipsRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "organizationsGetProjectMemberships", method = "GET", path = "/api/public/projects/{projectId}/memberships")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/projects/{projectId}/memberships")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("organizations_getProjectMemberships")
    @Override
    public MembershipsResponse organizationsGetProjectMemberships(
            APIOrganizationsGetProjectMembershipsRequest apiRequest);

    /**
     * Create or update a membership for the organization associated with the API key
     *
     * @param apiRequest {@link APIOrganizationsUpdateOrganizationMembershipRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "organizationsUpdateOrganizationMembership", method = "PUT", path = "/api/public/organizations/memberships")
    @jakarta.ws.rs.PUT
    @jakarta.ws.rs.Path("/api/public/organizations/memberships")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("organizations_updateOrganizationMembership")
    @Override
    public MembershipResponse organizationsUpdateOrganizationMembership(
            APIOrganizationsUpdateOrganizationMembershipRequest apiRequest);

    /**
     * Create or update a membership for a specific project
     *
     * @param apiRequest {@link APIOrganizationsUpdateProjectMembershipRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "organizationsUpdateProjectMembership", method = "PUT", path = "/api/public/projects/{projectId}/memberships")
    @jakarta.ws.rs.PUT
    @jakarta.ws.rs.Path("/api/public/projects/{projectId}/memberships")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("organizations_updateProjectMembership")
    @Override
    public MembershipResponse organizationsUpdateProjectMembership(
            APIOrganizationsUpdateProjectMembershipRequest apiRequest);

}
