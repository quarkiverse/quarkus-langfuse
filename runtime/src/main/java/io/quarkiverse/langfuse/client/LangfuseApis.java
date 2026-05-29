package io.quarkiverse.langfuse.client;

import io.quarkiverse.langfuse.client.api.AnnotationQueuesApi;
import io.quarkiverse.langfuse.client.api.BlobStorageIntegrationsApi;
import io.quarkiverse.langfuse.client.api.CommentsApi;
import io.quarkiverse.langfuse.client.api.DatasetItemsApi;
import io.quarkiverse.langfuse.client.api.DatasetRunItemsApi;
import io.quarkiverse.langfuse.client.api.DatasetsApi;
import io.quarkiverse.langfuse.client.api.HealthApi;
import io.quarkiverse.langfuse.client.api.IngestionApi;
import io.quarkiverse.langfuse.client.api.LegacyMetricsV1Api;
import io.quarkiverse.langfuse.client.api.LegacyObservationsV1Api;
import io.quarkiverse.langfuse.client.api.LegacyScoreV1Api;
import io.quarkiverse.langfuse.client.api.LlmConnectionsApi;
import io.quarkiverse.langfuse.client.api.MediaApi;
import io.quarkiverse.langfuse.client.api.MetricsApi;
import io.quarkiverse.langfuse.client.api.ModelsApi;
import io.quarkiverse.langfuse.client.api.ObservationsApi;
import io.quarkiverse.langfuse.client.api.OpentelemetryApi;
import io.quarkiverse.langfuse.client.api.OrganizationsApi;
import io.quarkiverse.langfuse.client.api.ProjectsApi;
import io.quarkiverse.langfuse.client.api.PromptVersionApi;
import io.quarkiverse.langfuse.client.api.PromptsApi;
import io.quarkiverse.langfuse.client.api.ScimApi;
import io.quarkiverse.langfuse.client.api.ScoreConfigsApi;
import io.quarkiverse.langfuse.client.api.ScoresApi;
import io.quarkiverse.langfuse.client.api.SessionsApi;
import io.quarkiverse.langfuse.client.api.TraceApi;
import io.quarkiverse.langfuse.client.api.UnstableEvaluationRulesApi;
import io.quarkiverse.langfuse.client.api.UnstableEvaluatorsApi;

public interface LangfuseApis extends AnnotationQueuesApi, BlobStorageIntegrationsApi, CommentsApi, DatasetItemsApi,
        DatasetRunItemsApi, DatasetsApi, HealthApi,
        IngestionApi, LegacyMetricsV1Api, LegacyObservationsV1Api, LegacyScoreV1Api, LlmConnectionsApi, MediaApi, MetricsApi,
        ModelsApi, ObservationsApi,
        OpentelemetryApi, OrganizationsApi, ProjectsApi, PromptVersionApi, PromptsApi, ScimApi, ScoreConfigsApi, ScoresApi,
        SessionsApi, TraceApi,
        UnstableEvaluationRulesApi, UnstableEvaluatorsApi {
}
