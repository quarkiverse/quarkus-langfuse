package io.quarkiverse.langfuse;

import java.time.Instant;
import java.util.List;

import jakarta.ws.rs.core.Response;

import com.langfuse.api.model.AnnotationQueue;
import com.langfuse.api.model.AnnotationQueueAssignmentRequest;
import com.langfuse.api.model.AnnotationQueueItem;
import com.langfuse.api.model.AnnotationQueueStatus;
import com.langfuse.api.model.ApiKeyDeletionResponse;
import com.langfuse.api.model.ApiKeyList;
import com.langfuse.api.model.ApiKeyResponse;
import com.langfuse.api.model.BlobStorageIntegrationDeletionResponse;
import com.langfuse.api.model.BlobStorageIntegrationResponse;
import com.langfuse.api.model.BlobStorageIntegrationStatusResponse;
import com.langfuse.api.model.BlobStorageIntegrationsResponse;
import com.langfuse.api.model.Comment;
import com.langfuse.api.model.CreateAnnotationQueueAssignmentResponse;
import com.langfuse.api.model.CreateAnnotationQueueItemRequest;
import com.langfuse.api.model.CreateAnnotationQueueRequest;
import com.langfuse.api.model.CreateBlobStorageIntegrationRequest;
import com.langfuse.api.model.CreateCommentRequest;
import com.langfuse.api.model.CreateCommentResponse;
import com.langfuse.api.model.CreateDatasetItemRequest;
import com.langfuse.api.model.CreateDatasetRequest;
import com.langfuse.api.model.CreateDatasetRunItemRequest;
import com.langfuse.api.model.CreateModelRequest;
import com.langfuse.api.model.CreatePromptRequest;
import com.langfuse.api.model.CreateScoreConfigRequest;
import com.langfuse.api.model.Dataset;
import com.langfuse.api.model.DatasetItem;
import com.langfuse.api.model.DatasetRunItem;
import com.langfuse.api.model.DatasetRunWithItems;
import com.langfuse.api.model.DeleteAnnotationQueueAssignmentResponse;
import com.langfuse.api.model.DeleteAnnotationQueueItemResponse;
import com.langfuse.api.model.DeleteDatasetItemResponse;
import com.langfuse.api.model.DeleteDatasetRunResponse;
import com.langfuse.api.model.DeleteLlmConnectionResponse;
import com.langfuse.api.model.DeleteMembershipRequest;
import com.langfuse.api.model.DeleteTraceResponse;
import com.langfuse.api.model.GetCommentsResponse;
import com.langfuse.api.model.GetMediaResponse;
import com.langfuse.api.model.GetMediaUploadUrlRequest;
import com.langfuse.api.model.GetMediaUploadUrlResponse;
import com.langfuse.api.model.GetScoresResponse;
import com.langfuse.api.model.HealthResponse;
import com.langfuse.api.model.IngestionBatchRequest;
import com.langfuse.api.model.IngestionResponse;
import com.langfuse.api.model.LegacyCreateScoreRequest;
import com.langfuse.api.model.LegacyCreateScoreResponse;
import com.langfuse.api.model.LegacyMetricsResponse;
import com.langfuse.api.model.LegacyObservationsViews;
import com.langfuse.api.model.LlmConnection;
import com.langfuse.api.model.MembershipDeletionResponse;
import com.langfuse.api.model.MembershipRequest;
import com.langfuse.api.model.MembershipResponse;
import com.langfuse.api.model.MembershipsResponse;
import com.langfuse.api.model.MetricsV2Response;
import com.langfuse.api.model.Model;
import com.langfuse.api.model.ObservationLevel;
import com.langfuse.api.model.ObservationsV2Response;
import com.langfuse.api.model.ObservationsView;
import com.langfuse.api.model.OpentelemetryExportTracesRequest;
import com.langfuse.api.model.OrganizationApiKeysResponse;
import com.langfuse.api.model.OrganizationProjectsResponse;
import com.langfuse.api.model.PaginatedAnnotationQueueItems;
import com.langfuse.api.model.PaginatedAnnotationQueues;
import com.langfuse.api.model.PaginatedDatasetItems;
import com.langfuse.api.model.PaginatedDatasetRunItems;
import com.langfuse.api.model.PaginatedDatasetRuns;
import com.langfuse.api.model.PaginatedDatasets;
import com.langfuse.api.model.PaginatedLlmConnections;
import com.langfuse.api.model.PaginatedModels;
import com.langfuse.api.model.PaginatedSessions;
import com.langfuse.api.model.PatchMediaBody;
import com.langfuse.api.model.Project;
import com.langfuse.api.model.ProjectDeletionResponse;
import com.langfuse.api.model.Projects;
import com.langfuse.api.model.ProjectsCreateApiKeyRequest;
import com.langfuse.api.model.ProjectsCreateRequest;
import com.langfuse.api.model.ProjectsUpdateRequest;
import com.langfuse.api.model.Prompt;
import com.langfuse.api.model.PromptMetaListResponse;
import com.langfuse.api.model.PromptVersionUpdateRequest;
import com.langfuse.api.model.ResourceTypesResponse;
import com.langfuse.api.model.SchemasResponse;
import com.langfuse.api.model.ScimCreateUserRequest;
import com.langfuse.api.model.ScimUser;
import com.langfuse.api.model.ScimUsersListResponse;
import com.langfuse.api.model.Score;
import com.langfuse.api.model.ScoreConfig;
import com.langfuse.api.model.ScoreConfigs;
import com.langfuse.api.model.ScoreDataType;
import com.langfuse.api.model.ScoreSource;
import com.langfuse.api.model.ServiceProviderConfig;
import com.langfuse.api.model.SessionWithTraces;
import com.langfuse.api.model.TraceDeleteMultipleRequest;
import com.langfuse.api.model.TraceWithFullDetails;
import com.langfuse.api.model.Traces;
import com.langfuse.api.model.UnstableCreateEvaluationRuleRequest;
import com.langfuse.api.model.UnstableCreateEvaluatorRequest;
import com.langfuse.api.model.UnstableDeleteEvaluationRuleResponse;
import com.langfuse.api.model.UnstableEvaluationRule;
import com.langfuse.api.model.UnstableEvaluationRules;
import com.langfuse.api.model.UnstableEvaluator;
import com.langfuse.api.model.UnstableEvaluators;
import com.langfuse.api.model.UnstableUpdateEvaluationRuleRequest;
import com.langfuse.api.model.UpdateAnnotationQueueItemRequest;
import com.langfuse.api.model.UpdateScoreConfigRequest;
import com.langfuse.api.model.UpsertLlmConnectionRequest;

import io.quarkiverse.langfuse.client.LangfuseApis;
import io.quarkiverse.langfuse.client.QuarkusLangfuseClient;
import io.quarkiverse.langfuse.config.LangfuseConfig;

public class LangfuseClient implements LangfuseApis {
    private final QuarkusLangfuseClient client;
    private final LangfuseConfig config;

    private LangfuseClient(LangfuseClientBuilder builder) {
        this.client = builder.client;
        this.config = builder.config;
    }

    public static LangfuseClientBuilder builder() {
        return new LangfuseClientBuilder();
    }

    // AnnotationQueuesApi
    @Override
    public AnnotationQueue annotationQueuesCreateQueue(CreateAnnotationQueueRequest createAnnotationQueueRequest) {
        return client.annotationQueuesCreateQueue(createAnnotationQueueRequest);
    }

    @Override
    public CreateAnnotationQueueAssignmentResponse annotationQueuesCreateQueueAssignment(String queueId,
            AnnotationQueueAssignmentRequest annotationQueueAssignmentRequest) {
        return client.annotationQueuesCreateQueueAssignment(queueId, annotationQueueAssignmentRequest);
    }

    @Override
    public AnnotationQueueItem annotationQueuesCreateQueueItem(String queueId,
            CreateAnnotationQueueItemRequest createAnnotationQueueItemRequest) {
        return client.annotationQueuesCreateQueueItem(queueId, createAnnotationQueueItemRequest);
    }

    @Override
    public DeleteAnnotationQueueAssignmentResponse annotationQueuesDeleteQueueAssignment(String queueId,
            AnnotationQueueAssignmentRequest annotationQueueAssignmentRequest) {
        return client.annotationQueuesDeleteQueueAssignment(queueId, annotationQueueAssignmentRequest);
    }

    @Override
    public DeleteAnnotationQueueItemResponse annotationQueuesDeleteQueueItem(String queueId, String itemId) {
        return client.annotationQueuesDeleteQueueItem(queueId, itemId);
    }

    @Override
    public AnnotationQueue annotationQueuesGetQueue(String queueId) {
        return client.annotationQueuesGetQueue(queueId);
    }

    @Override
    public AnnotationQueueItem annotationQueuesGetQueueItem(String queueId, String itemId) {
        return client.annotationQueuesGetQueueItem(queueId, itemId);
    }

    @Override
    public PaginatedAnnotationQueueItems annotationQueuesListQueueItems(String queueId,
            AnnotationQueueStatus status, Integer page, Integer limit) {
        return client.annotationQueuesListQueueItems(queueId, status, page, limit);
    }

    @Override
    public PaginatedAnnotationQueues annotationQueuesListQueues(Integer page, Integer limit) {
        return client.annotationQueuesListQueues(page, limit);
    }

    @Override
    public AnnotationQueueItem annotationQueuesUpdateQueueItem(String queueId, String itemId,
            UpdateAnnotationQueueItemRequest updateAnnotationQueueItemRequest) {
        return client.annotationQueuesUpdateQueueItem(queueId, itemId, updateAnnotationQueueItemRequest);
    }

    // BlobStorageIntegrationsApi
    @Override
    public BlobStorageIntegrationDeletionResponse blobStorageIntegrationsDeleteBlobStorageIntegration(String id) {
        return client.blobStorageIntegrationsDeleteBlobStorageIntegration(id);
    }

    @Override
    public BlobStorageIntegrationStatusResponse blobStorageIntegrationsGetBlobStorageIntegrationStatus(String id) {
        return client.blobStorageIntegrationsGetBlobStorageIntegrationStatus(id);
    }

    @Override
    public BlobStorageIntegrationsResponse blobStorageIntegrationsGetBlobStorageIntegrations() {
        return client.blobStorageIntegrationsGetBlobStorageIntegrations();
    }

    @Override
    public BlobStorageIntegrationResponse blobStorageIntegrationsUpsertBlobStorageIntegration(
            CreateBlobStorageIntegrationRequest createBlobStorageIntegrationRequest) {
        return client.blobStorageIntegrationsUpsertBlobStorageIntegration(createBlobStorageIntegrationRequest);
    }

    // CommentsApi
    @Override
    public CreateCommentResponse commentsCreate(CreateCommentRequest createCommentRequest) {
        return client.commentsCreate(createCommentRequest);
    }

    @Override
    public GetCommentsResponse commentsGet(Integer page, Integer limit, String objectType, String objectId,
            String authorUserId) {
        return client.commentsGet(page, limit, objectType, objectId, authorUserId);
    }

    @Override
    public Comment commentsGetById(String commentId) {
        return client.commentsGetById(commentId);
    }

    // DatasetItemsApi
    @Override
    public DatasetItem datasetItemsCreate(CreateDatasetItemRequest createDatasetItemRequest) {
        return client.datasetItemsCreate(createDatasetItemRequest);
    }

    @Override
    public DeleteDatasetItemResponse datasetItemsDelete(String id) {
        return client.datasetItemsDelete(id);
    }

    @Override
    public DatasetItem datasetItemsGet(String id) {
        return client.datasetItemsGet(id);
    }

    @Override
    public PaginatedDatasetItems datasetItemsList(String datasetName, String sourceTraceId,
            String sourceObservationId, Instant version, Integer page, Integer limit) {
        return client.datasetItemsList(datasetName, sourceTraceId, sourceObservationId, version, page, limit);
    }

    // DatasetRunItemsApi
    @Override
    public DatasetRunItem datasetRunItemsCreate(CreateDatasetRunItemRequest createDatasetRunItemRequest) {
        return client.datasetRunItemsCreate(createDatasetRunItemRequest);
    }

    @Override
    public PaginatedDatasetRunItems datasetRunItemsList(String datasetId, String runName, Integer page,
            Integer limit) {
        return client.datasetRunItemsList(datasetId, runName, page, limit);
    }

    // DatasetsApi
    @Override
    public Dataset datasetsCreate(CreateDatasetRequest createDatasetRequest) {
        return client.datasetsCreate(createDatasetRequest);
    }

    @Override
    public DeleteDatasetRunResponse datasetsDeleteRun(String datasetName, String runName) {
        return client.datasetsDeleteRun(datasetName, runName);
    }

    @Override
    public Dataset datasetsGet(String datasetName) {
        return client.datasetsGet(datasetName);
    }

    @Override
    public DatasetRunWithItems datasetsGetRun(String datasetName, String runName) {
        return client.datasetsGetRun(datasetName, runName);
    }

    @Override
    public PaginatedDatasetRuns datasetsGetRuns(String datasetName, Integer page, Integer limit) {
        return client.datasetsGetRuns(datasetName, page, limit);
    }

    @Override
    public PaginatedDatasets datasetsList(Integer page, Integer limit) {
        return client.datasetsList(page, limit);
    }

    // HealthApi
    @Override
    public HealthResponse healthHealth() {
        return client.healthHealth();
    }

    // IngestionApi
    @Override
    public IngestionResponse ingestionBatch(IngestionBatchRequest ingestionBatchRequest) {
        return client.ingestionBatch(ingestionBatchRequest);
    }

    // LegacyMetricsV1Api
    @Override
    public LegacyMetricsResponse legacyMetricsV1Metrics(String query) {
        return client.legacyMetricsV1Metrics(query);
    }

    // LegacyObservationsV1Api
    @Override
    public ObservationsView legacyObservationsV1Get(String observationId) {
        return client.legacyObservationsV1Get(observationId);
    }

    @Override
    public LegacyObservationsViews legacyObservationsV1GetMany(Integer page, Integer limit, String name,
            String userId, String type, String traceId, ObservationLevel level, String parentObservationId,
            List<String> environment, Instant fromStartTime, Instant toStartTime, String version, String filter) {
        return client.legacyObservationsV1GetMany(page, limit, name, userId, type, traceId, level,
                parentObservationId, environment, fromStartTime, toStartTime, version, filter);
    }

    // LegacyScoreV1Api
    @Override
    public LegacyCreateScoreResponse legacyScoreV1Create(LegacyCreateScoreRequest legacyCreateScoreRequest) {
        return client.legacyScoreV1Create(legacyCreateScoreRequest);
    }

    @Override
    public Response legacyScoreV1Delete(String scoreId) {
        return client.legacyScoreV1Delete(scoreId);
    }

    // LlmConnectionsApi
    @Override
    public DeleteLlmConnectionResponse llmConnectionsDelete(String id) {
        return client.llmConnectionsDelete(id);
    }

    @Override
    public PaginatedLlmConnections llmConnectionsList(Integer page, Integer limit) {
        return client.llmConnectionsList(page, limit);
    }

    @Override
    public LlmConnection llmConnectionsUpsert(UpsertLlmConnectionRequest upsertLlmConnectionRequest) {
        return client.llmConnectionsUpsert(upsertLlmConnectionRequest);
    }

    // MediaApi
    @Override
    public GetMediaResponse mediaGet(String mediaId) {
        return client.mediaGet(mediaId);
    }

    @Override
    public GetMediaUploadUrlResponse mediaGetUploadUrl(GetMediaUploadUrlRequest getMediaUploadUrlRequest) {
        return client.mediaGetUploadUrl(getMediaUploadUrlRequest);
    }

    @Override
    public Response mediaPatch(String mediaId, PatchMediaBody patchMediaBody) {
        return client.mediaPatch(mediaId, patchMediaBody);
    }

    // MetricsApi
    @Override
    public MetricsV2Response metricsMetrics(String query) {
        return client.metricsMetrics(query);
    }

    // ModelsApi
    @Override
    public Model modelsCreate(CreateModelRequest createModelRequest) {
        return client.modelsCreate(createModelRequest);
    }

    @Override
    public Response modelsDelete(String id) {
        return client.modelsDelete(id);
    }

    @Override
    public Model modelsGet(String id) {
        return client.modelsGet(id);
    }

    @Override
    public PaginatedModels modelsList(Integer page, Integer limit) {
        return client.modelsList(page, limit);
    }

    // ObservationsApi
    @Override
    public ObservationsV2Response observationsGetMany(String fields, String expandMetadata, Integer limit,
            String cursor, Boolean parseIoAsJson, String name, String userId, String type, String traceId,
            ObservationLevel level, String parentObservationId, List<String> environment, Instant fromStartTime,
            Instant toStartTime, String version, String filter) {
        return client.observationsGetMany(fields, expandMetadata, limit, cursor, parseIoAsJson, name, userId, type,
                traceId, level, parentObservationId, environment, fromStartTime, toStartTime, version, filter);
    }

    // OpentelemetryApi
    @Override
    public Object opentelemetryExportTraces(OpentelemetryExportTracesRequest opentelemetryExportTracesRequest) {
        return client.opentelemetryExportTraces(opentelemetryExportTracesRequest);
    }

    // OrganizationsApi
    @Override
    public MembershipDeletionResponse organizationsDeleteOrganizationMembership(
            DeleteMembershipRequest deleteMembershipRequest) {
        return client.organizationsDeleteOrganizationMembership(deleteMembershipRequest);
    }

    @Override
    public MembershipDeletionResponse organizationsDeleteProjectMembership(String projectId,
            DeleteMembershipRequest deleteMembershipRequest) {
        return client.organizationsDeleteProjectMembership(projectId, deleteMembershipRequest);
    }

    @Override
    public OrganizationApiKeysResponse organizationsGetOrganizationApiKeys() {
        return client.organizationsGetOrganizationApiKeys();
    }

    @Override
    public MembershipsResponse organizationsGetOrganizationMemberships() {
        return client.organizationsGetOrganizationMemberships();
    }

    @Override
    public OrganizationProjectsResponse organizationsGetOrganizationProjects() {
        return client.organizationsGetOrganizationProjects();
    }

    @Override
    public MembershipsResponse organizationsGetProjectMemberships(String projectId) {
        return client.organizationsGetProjectMemberships(projectId);
    }

    @Override
    public MembershipResponse organizationsUpdateOrganizationMembership(MembershipRequest membershipRequest) {
        return client.organizationsUpdateOrganizationMembership(membershipRequest);
    }

    @Override
    public MembershipResponse organizationsUpdateProjectMembership(String projectId,
            MembershipRequest membershipRequest) {
        return client.organizationsUpdateProjectMembership(projectId, membershipRequest);
    }

    // ProjectsApi
    @Override
    public Project projectsCreate(ProjectsCreateRequest projectsCreateRequest) {
        return client.projectsCreate(projectsCreateRequest);
    }

    @Override
    public ApiKeyResponse projectsCreateApiKey(String projectId,
            ProjectsCreateApiKeyRequest projectsCreateApiKeyRequest) {
        return client.projectsCreateApiKey(projectId, projectsCreateApiKeyRequest);
    }

    @Override
    public ProjectDeletionResponse projectsDelete(String projectId) {
        return client.projectsDelete(projectId);
    }

    @Override
    public ApiKeyDeletionResponse projectsDeleteApiKey(String projectId, String apiKeyId) {
        return client.projectsDeleteApiKey(projectId, apiKeyId);
    }

    @Override
    public Projects projectsGet() {
        return client.projectsGet();
    }

    @Override
    public ApiKeyList projectsGetApiKeys(String projectId) {
        return client.projectsGetApiKeys(projectId);
    }

    @Override
    public Project projectsUpdate(String projectId, ProjectsUpdateRequest projectsUpdateRequest) {
        return client.projectsUpdate(projectId, projectsUpdateRequest);
    }

    // PromptVersionApi
    @Override
    public Prompt promptVersionUpdate(String name, Integer version,
            PromptVersionUpdateRequest promptVersionUpdateRequest) {
        return client.promptVersionUpdate(name, version, promptVersionUpdateRequest);
    }

    // PromptsApi
    @Override
    public Prompt promptsCreate(CreatePromptRequest createPromptRequest) {
        return client.promptsCreate(createPromptRequest);
    }

    @Override
    public Response promptsDelete(String promptName, String label, Integer version) {
        return client.promptsDelete(promptName, label, version);
    }

    @Override
    public Prompt promptsGet(String promptName, Integer version, String label, Boolean resolve) {
        return client.promptsGet(promptName, version, label, resolve);
    }

    @Override
    public PromptMetaListResponse promptsList(String name, String label, String tag, Integer page, Integer limit,
            Instant fromUpdatedAt, Instant toUpdatedAt) {
        return client.promptsList(name, label, tag, page, limit, fromUpdatedAt, toUpdatedAt);
    }

    // ScimApi
    @Override
    public ScimUser scimCreateUser(ScimCreateUserRequest scimCreateUserRequest) {
        return client.scimCreateUser(scimCreateUserRequest);
    }

    @Override
    public Object scimDeleteUser(String userId) {
        return client.scimDeleteUser(userId);
    }

    @Override
    public ResourceTypesResponse scimGetResourceTypes() {
        return client.scimGetResourceTypes();
    }

    @Override
    public SchemasResponse scimGetSchemas() {
        return client.scimGetSchemas();
    }

    @Override
    public ServiceProviderConfig scimGetServiceProviderConfig() {
        return client.scimGetServiceProviderConfig();
    }

    @Override
    public ScimUser scimGetUser(String userId) {
        return client.scimGetUser(userId);
    }

    @Override
    public ScimUsersListResponse scimListUsers(String filter, Integer startIndex, Integer count) {
        return client.scimListUsers(filter, startIndex, count);
    }

    // ScoreConfigsApi
    @Override
    public ScoreConfig scoreConfigsCreate(CreateScoreConfigRequest createScoreConfigRequest) {
        return client.scoreConfigsCreate(createScoreConfigRequest);
    }

    @Override
    public ScoreConfigs scoreConfigsGet(Integer page, Integer limit) {
        return client.scoreConfigsGet(page, limit);
    }

    @Override
    public ScoreConfig scoreConfigsGetById(String configId) {
        return client.scoreConfigsGetById(configId);
    }

    @Override
    public ScoreConfig scoreConfigsUpdate(String configId, UpdateScoreConfigRequest updateScoreConfigRequest) {
        return client.scoreConfigsUpdate(configId, updateScoreConfigRequest);
    }

    // ScoresApi
    @Override
    public Score scoresGetById(String scoreId) {
        return client.scoresGetById(scoreId);
    }

    @Override
    public GetScoresResponse scoresGetMany(Integer page, Integer limit, String userId, String name,
            Instant fromTimestamp, Instant toTimestamp, List<String> environment, ScoreSource source, String operator,
            Double value, String scoreIds, String configId, String sessionId, String datasetRunId, String traceId,
            String observationId, String queueId, ScoreDataType dataType, List<String> traceTags, String fields,
            String filter) {
        return client.scoresGetMany(page, limit, userId, name, fromTimestamp, toTimestamp, environment, source,
                operator, value, scoreIds, configId, sessionId, datasetRunId, traceId, observationId, queueId,
                dataType, traceTags, fields, filter);
    }

    // SessionsApi
    @Override
    public SessionWithTraces sessionsGet(String sessionId) {
        return client.sessionsGet(sessionId);
    }

    @Override
    public PaginatedSessions sessionsList(Integer page, Integer limit, Instant fromTimestamp, Instant toTimestamp,
            List<String> environment) {
        return client.sessionsList(page, limit, fromTimestamp, toTimestamp, environment);
    }

    // TraceApi
    @Override
    public DeleteTraceResponse traceDelete(String traceId) {
        return client.traceDelete(traceId);
    }

    @Override
    public DeleteTraceResponse traceDeleteMultiple(TraceDeleteMultipleRequest traceDeleteMultipleRequest) {
        return client.traceDeleteMultiple(traceDeleteMultipleRequest);
    }

    @Override
    public TraceWithFullDetails traceGet(String traceId, String fields) {
        return client.traceGet(traceId, fields);
    }

    @Override
    public Traces traceList(Integer page, Integer limit, String userId, String name, String sessionId,
            Instant fromTimestamp, Instant toTimestamp, String orderBy, List<String> tags, String version,
            String release, List<String> environment, String fields, String filter) {
        return client.traceList(page, limit, userId, name, sessionId, fromTimestamp, toTimestamp, orderBy, tags,
                version, release, environment, fields, filter);
    }

    // UnstableEvaluationRulesApi
    @Override
    public UnstableEvaluationRule unstableEvaluationRulesCreate(
            UnstableCreateEvaluationRuleRequest unstableCreateEvaluationRuleRequest) {
        return client.unstableEvaluationRulesCreate(unstableCreateEvaluationRuleRequest);
    }

    @Override
    public UnstableDeleteEvaluationRuleResponse unstableEvaluationRulesDelete(String evaluationRuleId) {
        return client.unstableEvaluationRulesDelete(evaluationRuleId);
    }

    @Override
    public UnstableEvaluationRule unstableEvaluationRulesGet(String evaluationRuleId) {
        return client.unstableEvaluationRulesGet(evaluationRuleId);
    }

    @Override
    public UnstableEvaluationRules unstableEvaluationRulesList(Integer page, Integer limit) {
        return client.unstableEvaluationRulesList(page, limit);
    }

    @Override
    public UnstableEvaluationRule unstableEvaluationRulesUpdate(String evaluationRuleId,
            UnstableUpdateEvaluationRuleRequest unstableUpdateEvaluationRuleRequest) {
        return client.unstableEvaluationRulesUpdate(evaluationRuleId, unstableUpdateEvaluationRuleRequest);
    }

    // UnstableEvaluatorsApi
    @Override
    public UnstableEvaluator unstableEvaluatorsCreate(
            UnstableCreateEvaluatorRequest unstableCreateEvaluatorRequest) {
        return client.unstableEvaluatorsCreate(unstableCreateEvaluatorRequest);
    }

    @Override
    public UnstableEvaluator unstableEvaluatorsGet(String evaluatorId) {
        return client.unstableEvaluatorsGet(evaluatorId);
    }

    @Override
    public UnstableEvaluators unstableEvaluatorsList(Integer page, Integer limit) {
        return client.unstableEvaluatorsList(page, limit);
    }

    public static class LangfuseClientBuilder {
        private QuarkusLangfuseClient client;
        private LangfuseConfig config;

        private LangfuseClientBuilder() {
        }

        public LangfuseClientBuilder client(QuarkusLangfuseClient client) {
            this.client = client;
            return this;
        }

        public LangfuseClientBuilder config(LangfuseConfig config) {
            this.config = config;
            return this;
        }

        public LangfuseClient build() {
            return new LangfuseClient(this);
        }
    }
}
