package io.quarkiverse.langfuse;

import java.time.Instant;
import java.util.List;

import jakarta.ws.rs.core.Response;

import io.quarkiverse.langfuse.client.LangfuseApis;
import io.quarkiverse.langfuse.client.QuarkusLangfuseClient;
import io.quarkiverse.langfuse.client.model.AnnotationQueue;
import io.quarkiverse.langfuse.client.model.AnnotationQueueAssignmentRequest;
import io.quarkiverse.langfuse.client.model.AnnotationQueueItem;
import io.quarkiverse.langfuse.client.model.AnnotationQueueStatus;
import io.quarkiverse.langfuse.client.model.ApiKeyDeletionResponse;
import io.quarkiverse.langfuse.client.model.ApiKeyList;
import io.quarkiverse.langfuse.client.model.ApiKeyResponse;
import io.quarkiverse.langfuse.client.model.BlobStorageIntegrationDeletionResponse;
import io.quarkiverse.langfuse.client.model.BlobStorageIntegrationResponse;
import io.quarkiverse.langfuse.client.model.BlobStorageIntegrationStatusResponse;
import io.quarkiverse.langfuse.client.model.BlobStorageIntegrationsResponse;
import io.quarkiverse.langfuse.client.model.Comment;
import io.quarkiverse.langfuse.client.model.CreateAnnotationQueueAssignmentResponse;
import io.quarkiverse.langfuse.client.model.CreateAnnotationQueueItemRequest;
import io.quarkiverse.langfuse.client.model.CreateAnnotationQueueRequest;
import io.quarkiverse.langfuse.client.model.CreateBlobStorageIntegrationRequest;
import io.quarkiverse.langfuse.client.model.CreateCommentRequest;
import io.quarkiverse.langfuse.client.model.CreateCommentResponse;
import io.quarkiverse.langfuse.client.model.CreateDatasetItemRequest;
import io.quarkiverse.langfuse.client.model.CreateDatasetRequest;
import io.quarkiverse.langfuse.client.model.CreateDatasetRunItemRequest;
import io.quarkiverse.langfuse.client.model.CreateModelRequest;
import io.quarkiverse.langfuse.client.model.CreatePromptRequest;
import io.quarkiverse.langfuse.client.model.CreateScoreConfigRequest;
import io.quarkiverse.langfuse.client.model.Dataset;
import io.quarkiverse.langfuse.client.model.DatasetItem;
import io.quarkiverse.langfuse.client.model.DatasetRunItem;
import io.quarkiverse.langfuse.client.model.DatasetRunWithItems;
import io.quarkiverse.langfuse.client.model.DeleteAnnotationQueueAssignmentResponse;
import io.quarkiverse.langfuse.client.model.DeleteAnnotationQueueItemResponse;
import io.quarkiverse.langfuse.client.model.DeleteDatasetItemResponse;
import io.quarkiverse.langfuse.client.model.DeleteDatasetRunResponse;
import io.quarkiverse.langfuse.client.model.DeleteLlmConnectionResponse;
import io.quarkiverse.langfuse.client.model.DeleteMembershipRequest;
import io.quarkiverse.langfuse.client.model.DeleteTraceResponse;
import io.quarkiverse.langfuse.client.model.GetCommentsResponse;
import io.quarkiverse.langfuse.client.model.GetMediaResponse;
import io.quarkiverse.langfuse.client.model.GetMediaUploadUrlRequest;
import io.quarkiverse.langfuse.client.model.GetMediaUploadUrlResponse;
import io.quarkiverse.langfuse.client.model.GetScoresResponse;
import io.quarkiverse.langfuse.client.model.HealthResponse;
import io.quarkiverse.langfuse.client.model.IngestionBatchRequest;
import io.quarkiverse.langfuse.client.model.IngestionResponse;
import io.quarkiverse.langfuse.client.model.LegacyCreateScoreRequest;
import io.quarkiverse.langfuse.client.model.LegacyCreateScoreResponse;
import io.quarkiverse.langfuse.client.model.LegacyMetricsResponse;
import io.quarkiverse.langfuse.client.model.LegacyObservationsViews;
import io.quarkiverse.langfuse.client.model.LlmConnection;
import io.quarkiverse.langfuse.client.model.MembershipDeletionResponse;
import io.quarkiverse.langfuse.client.model.MembershipRequest;
import io.quarkiverse.langfuse.client.model.MembershipResponse;
import io.quarkiverse.langfuse.client.model.MembershipsResponse;
import io.quarkiverse.langfuse.client.model.MetricsV2Response;
import io.quarkiverse.langfuse.client.model.Model;
import io.quarkiverse.langfuse.client.model.ObservationLevel;
import io.quarkiverse.langfuse.client.model.ObservationsV2Response;
import io.quarkiverse.langfuse.client.model.ObservationsView;
import io.quarkiverse.langfuse.client.model.OpentelemetryExportTracesRequest;
import io.quarkiverse.langfuse.client.model.OrganizationApiKeysResponse;
import io.quarkiverse.langfuse.client.model.OrganizationProjectsResponse;
import io.quarkiverse.langfuse.client.model.PaginatedAnnotationQueueItems;
import io.quarkiverse.langfuse.client.model.PaginatedAnnotationQueues;
import io.quarkiverse.langfuse.client.model.PaginatedDatasetItems;
import io.quarkiverse.langfuse.client.model.PaginatedDatasetRunItems;
import io.quarkiverse.langfuse.client.model.PaginatedDatasetRuns;
import io.quarkiverse.langfuse.client.model.PaginatedDatasets;
import io.quarkiverse.langfuse.client.model.PaginatedLlmConnections;
import io.quarkiverse.langfuse.client.model.PaginatedModels;
import io.quarkiverse.langfuse.client.model.PaginatedSessions;
import io.quarkiverse.langfuse.client.model.PatchMediaBody;
import io.quarkiverse.langfuse.client.model.Project;
import io.quarkiverse.langfuse.client.model.ProjectDeletionResponse;
import io.quarkiverse.langfuse.client.model.Projects;
import io.quarkiverse.langfuse.client.model.ProjectsCreateApiKeyRequest;
import io.quarkiverse.langfuse.client.model.ProjectsCreateRequest;
import io.quarkiverse.langfuse.client.model.ProjectsUpdateRequest;
import io.quarkiverse.langfuse.client.model.Prompt;
import io.quarkiverse.langfuse.client.model.PromptMetaListResponse;
import io.quarkiverse.langfuse.client.model.PromptVersionUpdateRequest;
import io.quarkiverse.langfuse.client.model.ResourceTypesResponse;
import io.quarkiverse.langfuse.client.model.SchemasResponse;
import io.quarkiverse.langfuse.client.model.ScimCreateUserRequest;
import io.quarkiverse.langfuse.client.model.ScimUser;
import io.quarkiverse.langfuse.client.model.ScimUsersListResponse;
import io.quarkiverse.langfuse.client.model.Score;
import io.quarkiverse.langfuse.client.model.ScoreConfig;
import io.quarkiverse.langfuse.client.model.ScoreConfigs;
import io.quarkiverse.langfuse.client.model.ScoreDataType;
import io.quarkiverse.langfuse.client.model.ScoreSource;
import io.quarkiverse.langfuse.client.model.ServiceProviderConfig;
import io.quarkiverse.langfuse.client.model.SessionWithTraces;
import io.quarkiverse.langfuse.client.model.TraceDeleteMultipleRequest;
import io.quarkiverse.langfuse.client.model.TraceWithFullDetails;
import io.quarkiverse.langfuse.client.model.Traces;
import io.quarkiverse.langfuse.client.model.UnstableCreateEvaluationRuleRequest;
import io.quarkiverse.langfuse.client.model.UnstableCreateEvaluatorRequest;
import io.quarkiverse.langfuse.client.model.UnstableDeleteEvaluationRuleResponse;
import io.quarkiverse.langfuse.client.model.UnstableEvaluationRule;
import io.quarkiverse.langfuse.client.model.UnstableEvaluationRules;
import io.quarkiverse.langfuse.client.model.UnstableEvaluator;
import io.quarkiverse.langfuse.client.model.UnstableEvaluators;
import io.quarkiverse.langfuse.client.model.UnstableUpdateEvaluationRuleRequest;
import io.quarkiverse.langfuse.client.model.UpdateAnnotationQueueItemRequest;
import io.quarkiverse.langfuse.client.model.UpdateScoreConfigRequest;
import io.quarkiverse.langfuse.client.model.UpsertLlmConnectionRequest;
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
