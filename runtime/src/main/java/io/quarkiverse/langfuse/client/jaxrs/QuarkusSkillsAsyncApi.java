package io.quarkiverse.langfuse.client.jaxrs;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.model.CreateSkillVersionRequest;
import com.langfuse.api.model.DeleteSkillVersionResponse;
import com.langfuse.api.model.SkillFileContentsResponse;
import com.langfuse.api.model.SkillMetaListResponse;
import com.langfuse.api.model.SkillVersion;
import com.langfuse.api.model.UpdateSkillLabelsRequest;
import com.langfuse.api.model.UpdateSkillRequest;
import com.langfuse.api.skills.SkillsApi.APISkillsCreateVersionRequest;
import com.langfuse.api.skills.SkillsApi.APISkillsDeleteVersionRequest;
import com.langfuse.api.skills.SkillsApi.APISkillsGetFileContentsRequest;
import com.langfuse.api.skills.SkillsApi.APISkillsGetRequest;
import com.langfuse.api.skills.SkillsApi.APISkillsListRequest;
import com.langfuse.api.skills.SkillsApi.APISkillsSetLabelsRequest;
import com.langfuse.api.skills.SkillsApi.APISkillsUpdateRequest;

/**
 * Langfuse Skills Async API
 */
public interface QuarkusSkillsAsyncApi extends com.langfuse.api.skills.async.SkillsApi {

    /**
     * List skills with metadata and timestamps from their latest version
     */
    @GET
    @Path("/api/public/skills")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<SkillMetaListResponse> skillsList(
            @QueryParam("name") String name,
            @QueryParam("search") String search,
            @QueryParam("tag") String tag,
            @QueryParam("page") Integer page,
            @QueryParam("limit") Integer limit,
            @QueryParam("fromUpdatedAt") OffsetDateTime fromUpdatedAt,
            @QueryParam("toUpdatedAt") OffsetDateTime toUpdatedAt);

    /**
     * List skills with metadata and timestamps from their latest version
     */
    @Override
    default CompletionStage<SkillMetaListResponse> skillsList(APISkillsListRequest apiRequest) {
        return skillsList(apiRequest.name(), apiRequest.search(), apiRequest.tag(), apiRequest.page(), apiRequest.limit(),
                apiRequest.fromUpdatedAt(), apiRequest.toUpdatedAt());
    }

    /**
     * Create skill version
     */
    @POST
    @Path("/api/public/skills")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<SkillVersion> skillsCreateVersion(
            CreateSkillVersionRequest createSkillVersionRequest);

    /**
     * Create skill version
     */
    @Override
    default CompletionStage<SkillVersion> skillsCreateVersion(APISkillsCreateVersionRequest apiRequest) {
        return skillsCreateVersion(apiRequest.createSkillVersionRequest());
    }

    /**
     * Resolve a skill's metadata and file manifest by version or label
     */
    @GET
    @Path("/api/public/skills/{skillName}")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<SkillVersion> skillsGet(
            @PathParam("skillName") String skillName,
            @QueryParam("version") Integer version,
            @QueryParam("label") String label,
            @QueryParam("includeContent") Boolean includeContent);

    /**
     * Resolve a skill's metadata and file manifest by version or label
     */
    @Override
    default CompletionStage<SkillVersion> skillsGet(APISkillsGetRequest apiRequest) {
        return skillsGet(apiRequest.skillName(), apiRequest.version(), apiRequest.label(), apiRequest.includeContent());
    }

    /**
     * Replace the shared tags across all versions of a skill
     */
    @PATCH
    @Path("/api/public/skills/{skillName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<SkillVersion> skillsUpdate(
            @PathParam("skillName") String skillName,
            UpdateSkillRequest updateSkillRequest);

    /**
     * Replace the shared tags across all versions of a skill
     */
    @Override
    default CompletionStage<SkillVersion> skillsUpdate(APISkillsUpdateRequest apiRequest) {
        return skillsUpdate(apiRequest.skillName(), apiRequest.updateSkillRequest());
    }

    /**
     * Read a batch of text contents by base64-encoded SHA-256 hashes
     */
    @GET
    @Path("/api/public/skills/files/content")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<SkillFileContentsResponse> skillsGetFileContents(
            @QueryParam("sha256Hashes") String sha256Hashes);

    /**
     * Read a batch of text contents by base64-encoded SHA-256 hashes
     */
    @Override
    default CompletionStage<SkillFileContentsResponse> skillsGetFileContents(APISkillsGetFileContentsRequest apiRequest) {
        return skillsGetFileContents(apiRequest.sha256Hashes());
    }

    /**
     * Replace the labels on a skill version
     */
    @PATCH
    @Path("/api/public/skills/{skillName}/versions/{skillVersion}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<SkillVersion> skillsSetLabels(
            @PathParam("skillName") String skillName,
            @PathParam("skillVersion") Integer skillVersion,
            UpdateSkillLabelsRequest updateSkillLabelsRequest);

    /**
     * Replace the labels on a skill version
     */
    @Override
    default CompletionStage<SkillVersion> skillsSetLabels(APISkillsSetLabelsRequest apiRequest) {
        return skillsSetLabels(apiRequest.skillName(), apiRequest.skillVersion(), apiRequest.updateSkillLabelsRequest());
    }

    /**
     * Delete one immutable skill version
     */
    @DELETE
    @Path("/api/public/skills/{skillName}/versions/{skillVersion}")
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<DeleteSkillVersionResponse> skillsDeleteVersion(
            @PathParam("skillName") String skillName,
            @PathParam("skillVersion") Integer skillVersion);

    /**
     * Delete one immutable skill version
     */
    @Override
    default CompletionStage<DeleteSkillVersionResponse> skillsDeleteVersion(APISkillsDeleteVersionRequest apiRequest) {
        return skillsDeleteVersion(apiRequest.skillName(), apiRequest.skillVersion());
    }

}
