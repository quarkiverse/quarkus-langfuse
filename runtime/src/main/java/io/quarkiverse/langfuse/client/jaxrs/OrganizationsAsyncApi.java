package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

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

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Organizations")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface OrganizationsAsyncApi extends com.langfuse.api.organizations.async.OrganizationsApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "organizationsDeleteOrganizationMembership", method = "DELETE", path = "/api/public/organizations/memberships")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/organizations/memberships")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("organizations_deleteOrganizationMembership")
    @Override
    public CompletionStage<MembershipDeletionResponse> organizationsDeleteOrganizationMembership(
            APIOrganizationsDeleteOrganizationMembershipRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "organizationsDeleteProjectMembership", method = "DELETE", path = "/api/public/projects/{projectId}/memberships")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/projects/{projectId}/memberships")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("organizations_deleteProjectMembership")
    @Override
    public CompletionStage<MembershipDeletionResponse> organizationsDeleteProjectMembership(
            APIOrganizationsDeleteProjectMembershipRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "organizationsGetOrganizationApiKeys", method = "GET", path = "/api/public/organizations/apiKeys")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/organizations/apiKeys")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("organizations_getOrganizationApiKeys")
    @Override
    public CompletionStage<OrganizationApiKeysResponse> organizationsGetOrganizationApiKeys();

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "organizationsGetOrganizationMemberships", method = "GET", path = "/api/public/organizations/memberships")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/organizations/memberships")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("organizations_getOrganizationMemberships")
    @Override
    public CompletionStage<MembershipsResponse> organizationsGetOrganizationMemberships();

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "organizationsGetOrganizationProjects", method = "GET", path = "/api/public/organizations/projects")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/organizations/projects")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("organizations_getOrganizationProjects")
    @Override
    public CompletionStage<OrganizationProjectsResponse> organizationsGetOrganizationProjects();

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "organizationsGetProjectMemberships", method = "GET", path = "/api/public/projects/{projectId}/memberships")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/projects/{projectId}/memberships")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("organizations_getProjectMemberships")
    @Override
    public CompletionStage<MembershipsResponse> organizationsGetProjectMemberships(
            APIOrganizationsGetProjectMembershipsRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "organizationsUpdateOrganizationMembership", method = "PUT", path = "/api/public/organizations/memberships")
    @jakarta.ws.rs.PUT
    @jakarta.ws.rs.Path("/api/public/organizations/memberships")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("organizations_updateOrganizationMembership")
    @Override
    public CompletionStage<MembershipResponse> organizationsUpdateOrganizationMembership(
            APIOrganizationsUpdateOrganizationMembershipRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "organizationsUpdateProjectMembership", method = "PUT", path = "/api/public/projects/{projectId}/memberships")
    @jakarta.ws.rs.PUT
    @jakarta.ws.rs.Path("/api/public/projects/{projectId}/memberships")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("organizations_updateProjectMembership")
    @Override
    public CompletionStage<MembershipResponse> organizationsUpdateProjectMembership(
            APIOrganizationsUpdateProjectMembershipRequest apiRequest);

}
