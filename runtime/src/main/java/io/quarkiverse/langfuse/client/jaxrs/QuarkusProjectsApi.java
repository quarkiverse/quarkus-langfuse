package io.quarkiverse.langfuse.client.jaxrs;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.model.ApiKeyDeletionResponse;
import com.langfuse.api.model.ApiKeyList;
import com.langfuse.api.model.ApiKeyResponse;
import com.langfuse.api.model.Project;
import com.langfuse.api.model.ProjectDeletionResponse;
import com.langfuse.api.model.Projects;
import com.langfuse.api.model.ProjectsCreateApiKeyRequest;
import com.langfuse.api.model.ProjectsCreateRequest;
import com.langfuse.api.model.ProjectsUpdateRequest;
import com.langfuse.api.projects.ProjectsApi.APIProjectsCreateApiKeyRequest;
import com.langfuse.api.projects.ProjectsApi.APIProjectsCreateRequest;
import com.langfuse.api.projects.ProjectsApi.APIProjectsDeleteApiKeyRequest;
import com.langfuse.api.projects.ProjectsApi.APIProjectsDeleteRequest;
import com.langfuse.api.projects.ProjectsApi.APIProjectsGetApiKeysRequest;
import com.langfuse.api.projects.ProjectsApi.APIProjectsUpdateRequest;

/**
 * langfuse
 * <p>
 * ## Authentication Authenticate with the API using [Basic Auth](https://en.wikipedia.org/wiki/Basic_access_authentication),
 * get API keys in the project settings: - publicKey: Langfuse Public Key - secretKey: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
public interface QuarkusProjectsApi extends com.langfuse.api.projects.ProjectsApi {

    /**
     * Create a new project
     */
    @POST
    @Path("/api/public/projects")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Project projectsCreate(
            ProjectsCreateRequest projectsCreateRequest);

    /**
     * Create a new project
     */
    @Override
    default Project projectsCreate(APIProjectsCreateRequest apiRequest) {
        return projectsCreate(apiRequest.projectsCreateRequest());
    }

    /**
     * Create a new API key for a project
     */
    @POST
    @Path("/api/public/projects/{projectId}/apiKeys")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ApiKeyResponse projectsCreateApiKey(
            @PathParam("projectId") String projectId,
            ProjectsCreateApiKeyRequest projectsCreateApiKeyRequest);

    /**
     * Create a new API key for a project
     */
    @Override
    default ApiKeyResponse projectsCreateApiKey(APIProjectsCreateApiKeyRequest apiRequest) {
        return projectsCreateApiKey(apiRequest.projectId(), apiRequest.projectsCreateApiKeyRequest());
    }

    /**
     * Delete a project by ID
     */
    @DELETE
    @Path("/api/public/projects/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    ProjectDeletionResponse projectsDelete(
            @PathParam("projectId") String projectId);

    /**
     * Delete a project by ID
     */
    @Override
    default ProjectDeletionResponse projectsDelete(APIProjectsDeleteRequest apiRequest) {
        return projectsDelete(apiRequest.projectId());
    }

    /**
     * Delete an API key for a project
     */
    @DELETE
    @Path("/api/public/projects/{projectId}/apiKeys/{apiKeyId}")
    @Produces(MediaType.APPLICATION_JSON)
    ApiKeyDeletionResponse projectsDeleteApiKey(
            @PathParam("projectId") String projectId,
            @PathParam("apiKeyId") String apiKeyId);

    /**
     * Delete an API key for a project
     */
    @Override
    default ApiKeyDeletionResponse projectsDeleteApiKey(APIProjectsDeleteApiKeyRequest apiRequest) {
        return projectsDeleteApiKey(apiRequest.projectId(), apiRequest.apiKeyId());
    }

    /**
     * Get Project associated with API key
     */
    @GET
    @Path("/api/public/projects")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    Projects projectsGet();

    /**
     * Get all API keys for a project
     */
    @GET
    @Path("/api/public/projects/{projectId}/apiKeys")
    @Produces(MediaType.APPLICATION_JSON)
    ApiKeyList projectsGetApiKeys(
            @PathParam("projectId") String projectId);

    /**
     * Get all API keys for a project
     */
    @Override
    default ApiKeyList projectsGetApiKeys(APIProjectsGetApiKeysRequest apiRequest) {
        return projectsGetApiKeys(apiRequest.projectId());
    }

    /**
     * Update a project by ID
     */
    @PUT
    @Path("/api/public/projects/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Project projectsUpdate(
            @PathParam("projectId") String projectId,
            ProjectsUpdateRequest projectsUpdateRequest);

    /**
     * Update a project by ID
     */
    @Override
    default Project projectsUpdate(APIProjectsUpdateRequest apiRequest) {
        return projectsUpdate(apiRequest.projectId(), apiRequest.projectsUpdateRequest());
    }

}
