package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

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

@io.quarkiverse.openapi.generator.annotations.GeneratedClass(value = "langfuse.yml", tag = "Projects")
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public interface ProjectsAsyncApi extends com.langfuse.api.projects.async.ProjectsApi {

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "projectsCreate", method = "POST", path = "/api/public/projects")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/projects")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("projects_create")
    @Override
    public CompletionStage<Project> projectsCreate(
            APIProjectsCreateRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "projectsCreateApiKey", method = "POST", path = "/api/public/projects/{projectId}/apiKeys")
    @jakarta.ws.rs.POST
    @jakarta.ws.rs.Path("/api/public/projects/{projectId}/apiKeys")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("projects_createApiKey")
    @Override
    public CompletionStage<ApiKeyResponse> projectsCreateApiKey(
            APIProjectsCreateApiKeyRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "projectsDelete", method = "DELETE", path = "/api/public/projects/{projectId}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/projects/{projectId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("projects_delete")
    @Override
    public CompletionStage<ProjectDeletionResponse> projectsDelete(
            APIProjectsDeleteRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "projectsDeleteApiKey", method = "DELETE", path = "/api/public/projects/{projectId}/apiKeys/{apiKeyId}")
    @jakarta.ws.rs.DELETE
    @jakarta.ws.rs.Path("/api/public/projects/{projectId}/apiKeys/{apiKeyId}")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("projects_deleteApiKey")
    @Override
    public CompletionStage<ApiKeyDeletionResponse> projectsDeleteApiKey(
            APIProjectsDeleteApiKeyRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "projectsGet", method = "GET", path = "/api/public/projects")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/projects")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("projects_get")
    @Override
    public CompletionStage<Projects> projectsGet();

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "projectsGetApiKeys", method = "GET", path = "/api/public/projects/{projectId}/apiKeys")
    @jakarta.ws.rs.GET
    @jakarta.ws.rs.Path("/api/public/projects/{projectId}/apiKeys")
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("projects_getApiKeys")
    @Override
    public CompletionStage<ApiKeyList> projectsGetApiKeys(
            APIProjectsGetApiKeysRequest apiRequest);

    @io.quarkiverse.openapi.generator.markers.OperationMarker(name = "BasicAuth", openApiSpecId = "langfuse_yml", operationId = "projectsUpdate", method = "PUT", path = "/api/public/projects/{projectId}")
    @jakarta.ws.rs.PUT
    @jakarta.ws.rs.Path("/api/public/projects/{projectId}")
    @jakarta.ws.rs.Consumes({ "application/json" })
    @jakarta.ws.rs.Produces({ "application/json" })
    @io.quarkiverse.openapi.generator.annotations.GeneratedMethod("projects_update")
    @Override
    public CompletionStage<Project> projectsUpdate(
            APIProjectsUpdateRequest apiRequest);

}
