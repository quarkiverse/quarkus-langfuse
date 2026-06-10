package io.quarkiverse.langfuse.client.jaxrs;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.model.DeleteMembershipRequest;
import com.langfuse.api.model.MembershipDeletionResponse;
import com.langfuse.api.model.MembershipRequest;
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
 * get API keys in the project settings: - publicKey: Langfuse Public Key - secretKey: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
public interface QuarkusOrganizationsApi extends com.langfuse.api.organizations.OrganizationsApi {

    /**
     * Delete a membership from the organization associated with the API key
     */
    @DELETE
    @Path("/api/public/organizations/memberships")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    MembershipDeletionResponse organizationsDeleteOrganizationMembership(
            DeleteMembershipRequest deleteMembershipRequest);

    /**
     * Delete a membership from the organization associated with the API key
     */
    @Override
    default MembershipDeletionResponse organizationsDeleteOrganizationMembership(
            APIOrganizationsDeleteOrganizationMembershipRequest apiRequest) {
        return organizationsDeleteOrganizationMembership(apiRequest.deleteMembershipRequest());
    }

    /**
     * Delete a membership from a specific project
     */
    @DELETE
    @Path("/api/public/projects/{projectId}/memberships")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    MembershipDeletionResponse organizationsDeleteProjectMembership(
            @PathParam("projectId") String projectId,
            DeleteMembershipRequest deleteMembershipRequest);

    /**
     * Delete a membership from a specific project
     */
    @Override
    default MembershipDeletionResponse organizationsDeleteProjectMembership(
            APIOrganizationsDeleteProjectMembershipRequest apiRequest) {
        return organizationsDeleteProjectMembership(apiRequest.projectId(), apiRequest.deleteMembershipRequest());
    }

    /**
     * Get all API keys for the organization associated with the API key
     */
    @GET
    @Path("/api/public/organizations/apiKeys")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    OrganizationApiKeysResponse organizationsGetOrganizationApiKeys();

    /**
     * Get all memberships for the organization associated with the API key
     */
    @GET
    @Path("/api/public/organizations/memberships")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    MembershipsResponse organizationsGetOrganizationMemberships();

    /**
     * Get all projects for the organization associated with the API key
     */
    @GET
    @Path("/api/public/organizations/projects")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    OrganizationProjectsResponse organizationsGetOrganizationProjects();

    /**
     * Get all memberships for a specific project
     */
    @GET
    @Path("/api/public/projects/{projectId}/memberships")
    @Produces(MediaType.APPLICATION_JSON)
    MembershipsResponse organizationsGetProjectMemberships(
            @PathParam("projectId") String projectId);

    /**
     * Get all memberships for a specific project
     */
    @Override
    default MembershipsResponse organizationsGetProjectMemberships(
            APIOrganizationsGetProjectMembershipsRequest apiRequest) {
        return organizationsGetProjectMemberships(apiRequest.projectId());
    }

    /**
     * Create or update a membership for the organization associated with the API key
     */
    @PUT
    @Path("/api/public/organizations/memberships")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    MembershipResponse organizationsUpdateOrganizationMembership(
            MembershipRequest membershipRequest);

    /**
     * Create or update a membership for the organization associated with the API key
     */
    @Override
    default MembershipResponse organizationsUpdateOrganizationMembership(
            APIOrganizationsUpdateOrganizationMembershipRequest apiRequest) {
        return organizationsUpdateOrganizationMembership(apiRequest.membershipRequest());
    }

    /**
     * Create or update a membership for a specific project
     */
    @PUT
    @Path("/api/public/projects/{projectId}/memberships")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    MembershipResponse organizationsUpdateProjectMembership(
            @PathParam("projectId") String projectId,
            MembershipRequest membershipRequest);

    /**
     * Create or update a membership for a specific project
     */
    @Override
    default MembershipResponse organizationsUpdateProjectMembership(
            APIOrganizationsUpdateProjectMembershipRequest apiRequest) {
        return organizationsUpdateProjectMembership(apiRequest.projectId(), apiRequest.membershipRequest());
    }

}
