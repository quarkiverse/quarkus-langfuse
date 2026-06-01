package io.quarkiverse.langfuse.client.jaxrs;

import com.langfuse.api.model.ApiKeyDeletionResponse;
import com.langfuse.api.model.ApiKeyList;
import com.langfuse.api.model.ApiKeyResponse;
import com.langfuse.api.model.Project;
import com.langfuse.api.model.ProjectDeletionResponse;
import com.langfuse.api.model.Projects;
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
 * get API keys in the project settings: - username: Langfuse Public Key - password: Langfuse Secret Key ## Exports - OpenAPI
 * spec: https://cloud.langfuse.com/generated/api/openapi.yml
 * </p>
 */
@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Projects")

@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface ProjectsApi extends com.langfuse.api.projects.ProjectsApi {

    /**
     * Create a new project
     *
     * @param apiRequest {@link APIProjectsCreateRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "projectsCreate", method = "POST", path = "/api/public/projects")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/projects")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("projects_create")
    @Override
    public Project projectsCreate(
            APIProjectsCreateRequest apiRequest);

    /**
     * Create a new API key for a project
     *
     * @param apiRequest {@link APIProjectsCreateApiKeyRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "projectsCreateApiKey", method = "POST", path = "/api/public/projects/{projectId}/apiKeys")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/projects/{projectId}/apiKeys")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("projects_createApiKey")
    @Override
    public ApiKeyResponse projectsCreateApiKey(
            APIProjectsCreateApiKeyRequest apiRequest);

    /**
     * Delete a project by ID
     *
     * @param apiRequest {@link APIProjectsDeleteRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "projectsDelete", method = "DELETE", path = "/api/public/projects/{projectId}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/projects/{projectId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("projects_delete")
    @Override
    public ProjectDeletionResponse projectsDelete(
            APIProjectsDeleteRequest apiRequest);

    /**
     * Delete an API key for a project
     *
     * @param apiRequest {@link APIProjectsDeleteApiKeyRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "projectsDeleteApiKey", method = "DELETE", path = "/api/public/projects/{projectId}/apiKeys/{apiKeyId}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/projects/{projectId}/apiKeys/{apiKeyId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("projects_deleteApiKey")
    @Override
    public ApiKeyDeletionResponse projectsDeleteApiKey(
            APIProjectsDeleteApiKeyRequest apiRequest);

    /**
     * Get Project associated with API key
     *
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "projectsGet", method = "GET", path = "/api/public/projects")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/projects")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("projects_get")
    @Override
    public Projects projectsGet();

    /**
     * Get all API keys for a project
     *
     * @param apiRequest {@link APIProjectsGetApiKeysRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "projectsGetApiKeys", method = "GET", path = "/api/public/projects/{projectId}/apiKeys")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/projects/{projectId}/apiKeys")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("projects_getApiKeys")
    @Override
    public ApiKeyList projectsGetApiKeys(
            APIProjectsGetApiKeysRequest apiRequest);

    /**
     * Update a project by ID
     *
     * @param apiRequest {@link APIProjectsUpdateRequest}
     */
    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "projectsUpdate", method = "PUT", path = "/api/public/projects/{projectId}")
    @jakarta.ws.rs.PUT
    @jakarta.ws.rs.Path("/api/public/projects/{projectId}")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("projects_update")
    @Override
    public Project projectsUpdate(
            APIProjectsUpdateRequest apiRequest);

}
