package com.langfuse.api;

import com.langfuse.api.annotationQueues.AnnotationQueuesApi;
import com.langfuse.api.blobStorageIntegrations.BlobStorageIntegrationsApi;
import com.langfuse.api.comments.CommentsApi;
import com.langfuse.api.datasetItems.DatasetItemsApi;
import com.langfuse.api.datasets.DatasetsApi;
import com.langfuse.api.evaluationRules.EvaluationRulesApi;
import com.langfuse.api.evaluators.EvaluatorsApi;
import com.langfuse.api.experiments.ExperimentsApi;
import com.langfuse.api.feedback.FeedbackApi;
import com.langfuse.api.health.HealthApi;
import com.langfuse.api.llmConnections.LlmConnectionsApi;
import com.langfuse.api.media.MediaApi;
import com.langfuse.api.metrics.MetricsApi;
import com.langfuse.api.models.ModelsApi;
import com.langfuse.api.observations.ObservationsApi;
import com.langfuse.api.opentelemetry.OpentelemetryApi;
import com.langfuse.api.organizations.OrganizationsApi;
import com.langfuse.api.projects.ProjectsApi;
import com.langfuse.api.promptVersion.PromptVersionApi;
import com.langfuse.api.prompts.PromptsApi;
import com.langfuse.api.scim.ScimApi;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi;
import com.langfuse.api.scores.ScoresApi;
import com.langfuse.api.skills.SkillsApi;
import com.langfuse.api.trace.TraceApi;
import com.langfuse.api.unstableDashboardWidgets.UnstableDashboardWidgetsApi;
import com.langfuse.api.unstableDashboards.UnstableDashboardsApi;

public interface LangfuseApis extends
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
