package io.quarkiverse.langfuse.client;

import com.langfuse.api.api.AnnotationQueuesApi;
import com.langfuse.api.api.BlobStorageIntegrationsApi;
import com.langfuse.api.api.CommentsApi;
import com.langfuse.api.api.DatasetItemsApi;
import com.langfuse.api.api.DatasetRunItemsApi;
import com.langfuse.api.api.DatasetsApi;
import com.langfuse.api.api.HealthApi;
import com.langfuse.api.api.IngestionApi;
import com.langfuse.api.api.LegacyMetricsV1Api;
import com.langfuse.api.api.LegacyObservationsV1Api;
import com.langfuse.api.api.LegacyScoreV1Api;
import com.langfuse.api.api.LlmConnectionsApi;
import com.langfuse.api.api.MediaApi;
import com.langfuse.api.api.MetricsApi;
import com.langfuse.api.api.ModelsApi;
import com.langfuse.api.api.ObservationsApi;
import com.langfuse.api.api.OpentelemetryApi;
import com.langfuse.api.api.OrganizationsApi;
import com.langfuse.api.api.ProjectsApi;
import com.langfuse.api.api.PromptVersionApi;
import com.langfuse.api.api.PromptsApi;
import com.langfuse.api.api.ScimApi;
import com.langfuse.api.api.ScoreConfigsApi;
import com.langfuse.api.api.ScoresApi;
import com.langfuse.api.api.SessionsApi;
import com.langfuse.api.api.TraceApi;
import com.langfuse.api.api.UnstableEvaluationRulesApi;
import com.langfuse.api.api.UnstableEvaluatorsApi;

public interface LangfuseApis extends AnnotationQueuesApi, BlobStorageIntegrationsApi, CommentsApi, DatasetItemsApi,
        DatasetRunItemsApi, DatasetsApi, HealthApi,
        IngestionApi, LegacyMetricsV1Api, LegacyObservationsV1Api, LegacyScoreV1Api, LlmConnectionsApi, MediaApi, MetricsApi,
        ModelsApi, ObservationsApi,
        OpentelemetryApi, OrganizationsApi, ProjectsApi, PromptVersionApi, PromptsApi, ScimApi, ScoreConfigsApi, ScoresApi,
        SessionsApi, TraceApi,
        UnstableEvaluationRulesApi, UnstableEvaluatorsApi {
}
