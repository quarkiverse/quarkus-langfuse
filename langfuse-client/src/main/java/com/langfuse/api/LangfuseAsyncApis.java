package com.langfuse.api;

import com.langfuse.api.annotationQueues.async.AnnotationQueuesApi;
import com.langfuse.api.blobStorageIntegrations.async.BlobStorageIntegrationsApi;
import com.langfuse.api.comments.async.CommentsApi;
import com.langfuse.api.datasetItems.async.DatasetItemsApi;
import com.langfuse.api.datasets.async.DatasetsApi;
import com.langfuse.api.evaluationRules.async.EvaluationRulesApi;
import com.langfuse.api.evaluators.async.EvaluatorsApi;
import com.langfuse.api.experiments.async.ExperimentsApi;
import com.langfuse.api.feedback.async.FeedbackApi;
import com.langfuse.api.health.async.HealthApi;
import com.langfuse.api.llmConnections.async.LlmConnectionsApi;
import com.langfuse.api.media.async.MediaApi;
import com.langfuse.api.metrics.async.MetricsApi;
import com.langfuse.api.models.async.ModelsApi;
import com.langfuse.api.observations.async.ObservationsApi;
import com.langfuse.api.opentelemetry.async.OpentelemetryApi;
import com.langfuse.api.organizations.async.OrganizationsApi;
import com.langfuse.api.projects.async.ProjectsApi;
import com.langfuse.api.promptVersion.async.PromptVersionApi;
import com.langfuse.api.prompts.async.PromptsApi;
import com.langfuse.api.scim.async.ScimApi;
import com.langfuse.api.scoreConfigs.async.ScoreConfigsApi;
import com.langfuse.api.scores.async.ScoresApi;
import com.langfuse.api.skills.async.SkillsApi;
import com.langfuse.api.trace.async.TraceApi;
import com.langfuse.api.unstableDashboardWidgets.async.UnstableDashboardWidgetsApi;
import com.langfuse.api.unstableDashboards.async.UnstableDashboardsApi;

public interface LangfuseAsyncApis extends
        AnnotationQueuesApi,
        BlobStorageIntegrationsApi,
        CommentsApi,
        DatasetItemsApi,
        DatasetsApi,
        EvaluationRulesApi,
        EvaluatorsApi,
        ExperimentsApi,
        FeedbackApi,
        HealthApi,
        LlmConnectionsApi,
        MediaApi,
        MetricsApi,
        ModelsApi,
        ObservationsApi,
        OpentelemetryApi,
        OrganizationsApi,
        ProjectsApi,
        PromptVersionApi,
        PromptsApi,
        ScimApi,
        ScoreConfigsApi,
        ScoresApi,
        SkillsApi,
        TraceApi,
        UnstableDashboardWidgetsApi,
        UnstableDashboardsApi {
}
