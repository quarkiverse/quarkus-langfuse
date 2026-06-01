package io.quarkiverse.langfuse;

import java.time.Duration;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.annotationQueues.AnnotationQueuesApi;
import com.langfuse.api.blobStorageIntegrations.BlobStorageIntegrationsApi;
import com.langfuse.api.comments.CommentsApi;
import com.langfuse.api.datasetItems.DatasetItemsApi;
import com.langfuse.api.datasetRunItems.DatasetRunItemsApi;
import com.langfuse.api.datasets.DatasetsApi;
import com.langfuse.api.health.HealthApi;
import com.langfuse.api.ingestion.IngestionApi;
import com.langfuse.api.legacyMetricsV1.LegacyMetricsV1Api;
import com.langfuse.api.legacyObservationsV1.LegacyObservationsV1Api;
import com.langfuse.api.legacyScoreV1.LegacyScoreV1Api;
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
import com.langfuse.api.sessions.SessionsApi;
import com.langfuse.api.trace.TraceApi;
import com.langfuse.api.unstableEvaluationRules.UnstableEvaluationRulesApi;
import com.langfuse.api.unstableEvaluators.UnstableEvaluatorsApi;

import io.quarkiverse.langfuse.client.QuarkusLangfuseAsyncClient;
import io.quarkiverse.langfuse.client.QuarkusLangfuseClient;
import io.quarkiverse.langfuse.config.LangfuseConfig;

public class QuarkusLangfuseApi implements LangfuseApi {
    private final QuarkusLangfuseClient client;
    private final QuarkusLangfuseAsyncClient asyncClient;
    private final LangfuseConfig config;

    private QuarkusLangfuseApi(QuarkusLangfuseApiBuilder builder) {
        this.client = builder.client;
        this.asyncClient = builder.asyncClient;
        this.config = builder.config;
    }

    public static QuarkusLangfuseApiBuilder builder() {
        return new QuarkusLangfuseApiBuilder();
    }

    @Override
    public HealthApi health() {
        return this.client;
    }

    @Override
    public com.langfuse.api.health.async.HealthApi asyncHealth() {
        return this.asyncClient;
    }

    @Override
    public AnnotationQueuesApi annotationQueues() {
        return this.client;
    }

    @Override
    public com.langfuse.api.annotationQueues.async.AnnotationQueuesApi asyncAnnotationQueues() {
        return this.asyncClient;
    }

    @Override
    public BlobStorageIntegrationsApi blobStorageIntegrations() {
        return this.client;
    }

    @Override
    public com.langfuse.api.blobStorageIntegrations.async.BlobStorageIntegrationsApi asyncBlobStorageIntegrations() {
        return this.asyncClient;
    }

    @Override
    public CommentsApi comments() {
        return this.client;
    }

    @Override
    public com.langfuse.api.comments.async.CommentsApi asyncComments() {
        return this.asyncClient;
    }

    @Override
    public DatasetItemsApi datasetItems() {
        return this.client;
    }

    @Override
    public com.langfuse.api.datasetItems.async.DatasetItemsApi asyncDatasetItems() {
        return this.asyncClient;
    }

    @Override
    public DatasetRunItemsApi datasetRunItems() {
        return this.client;
    }

    @Override
    public com.langfuse.api.datasetRunItems.async.DatasetRunItemsApi asyncDatasetRunItems() {
        return this.asyncClient;
    }

    @Override
    public DatasetsApi datasets() {
        return this.client;
    }

    @Override
    public com.langfuse.api.datasets.async.DatasetsApi asyncDatasets() {
        return this.asyncClient;
    }

    @Override
    public IngestionApi ingestion() {
        return this.client;
    }

    @Override
    public com.langfuse.api.ingestion.async.IngestionApi asyncIngestion() {
        return this.asyncClient;
    }

    @Override
    public LegacyMetricsV1Api legacyMetricsV1() {
        return this.client;
    }

    @Override
    public com.langfuse.api.legacyMetricsV1.async.LegacyMetricsV1Api asyncLegacyMetricsV1() {
        return this.asyncClient;
    }

    @Override
    public LegacyObservationsV1Api legacyObservationsV1() {
        return this.client;
    }

    @Override
    public com.langfuse.api.legacyObservationsV1.async.LegacyObservationsV1Api asyncLegacyObservationsV1() {
        return this.asyncClient;
    }

    @Override
    public LegacyScoreV1Api legacyScoreV1() {
        return this.client;
    }

    @Override
    public com.langfuse.api.legacyScoreV1.async.LegacyScoreV1Api asyncLegacyScoreV1() {
        return this.asyncClient;
    }

    @Override
    public LlmConnectionsApi llmConnections() {
        return this.client;
    }

    @Override
    public com.langfuse.api.llmConnections.async.LlmConnectionsApi asyncLlmConnections() {
        return this.asyncClient;
    }

    @Override
    public MediaApi media() {
        return this.client;
    }

    @Override
    public com.langfuse.api.media.async.MediaApi asyncMedia() {
        return this.asyncClient;
    }

    @Override
    public MetricsApi metrics() {
        return this.client;
    }

    @Override
    public com.langfuse.api.metrics.async.MetricsApi asyncMetrics() {
        return this.asyncClient;
    }

    @Override
    public ModelsApi models() {
        return this.client;
    }

    @Override
    public com.langfuse.api.models.async.ModelsApi asyncModels() {
        return this.asyncClient;
    }

    @Override
    public ObservationsApi observations() {
        return this.client;
    }

    @Override
    public com.langfuse.api.observations.async.ObservationsApi asyncObservations() {
        return this.asyncClient;
    }

    @Override
    public OpentelemetryApi opentelemetry() {
        return this.client;
    }

    @Override
    public com.langfuse.api.opentelemetry.async.OpentelemetryApi asyncOpentelemetry() {
        return this.asyncClient;
    }

    @Override
    public OrganizationsApi organizations() {
        return this.client;
    }

    @Override
    public com.langfuse.api.organizations.async.OrganizationsApi asyncOrganizations() {
        return this.asyncClient;
    }

    @Override
    public ProjectsApi projects() {
        return this.client;
    }

    @Override
    public com.langfuse.api.projects.async.ProjectsApi asyncProjects() {
        return this.asyncClient;
    }

    @Override
    public PromptVersionApi promptVersion() {
        return this.client;
    }

    @Override
    public com.langfuse.api.promptVersion.async.PromptVersionApi asyncPromptVersion() {
        return this.asyncClient;
    }

    @Override
    public PromptsApi prompts() {
        return this.client;
    }

    @Override
    public com.langfuse.api.prompts.async.PromptsApi asyncPrompts() {
        return this.asyncClient;
    }

    @Override
    public ScimApi scim() {
        return this.client;
    }

    @Override
    public com.langfuse.api.scim.async.ScimApi asyncScim() {
        return this.asyncClient;
    }

    @Override
    public ScoreConfigsApi scoreConfigs() {
        return this.client;
    }

    @Override
    public com.langfuse.api.scoreConfigs.async.ScoreConfigsApi asyncScoreConfigs() {
        return this.asyncClient;
    }

    @Override
    public ScoresApi scores() {
        return this.client;
    }

    @Override
    public com.langfuse.api.scores.async.ScoresApi asyncScores() {
        return this.asyncClient;
    }

    @Override
    public SessionsApi sessions() {
        return this.client;
    }

    @Override
    public com.langfuse.api.sessions.async.SessionsApi asyncSessions() {
        return this.asyncClient;
    }

    @Override
    public TraceApi trace() {
        return this.client;
    }

    @Override
    public com.langfuse.api.trace.async.TraceApi asyncTrace() {
        return this.asyncClient;
    }

    @Override
    public UnstableEvaluationRulesApi unstableEvaluationRules() {
        return this.client;
    }

    @Override
    public com.langfuse.api.unstableEvaluationRules.async.UnstableEvaluationRulesApi asyncUnstableEvaluationRules() {
        return this.asyncClient;
    }

    @Override
    public UnstableEvaluatorsApi unstableEvaluators() {
        return this.client;
    }

    @Override
    public com.langfuse.api.unstableEvaluators.async.UnstableEvaluatorsApi asyncUnstableEvaluators() {
        return this.asyncClient;
    }

    public static class QuarkusLangfuseApiBuilder implements LangfuseApiBuilder<QuarkusLangfuseApi, QuarkusLangfuseApiBuilder> {
        private QuarkusLangfuseClient client;
        private QuarkusLangfuseAsyncClient asyncClient;
        private LangfuseConfig config;

        private QuarkusLangfuseApiBuilder() {
        }

        public QuarkusLangfuseApiBuilder client(QuarkusLangfuseClient client) {
            this.client = client;
            return this;
        }

        public QuarkusLangfuseApiBuilder asyncClient(QuarkusLangfuseAsyncClient asyncClient) {
            this.asyncClient = asyncClient;
            return this;
        }

        public QuarkusLangfuseApiBuilder config(LangfuseConfig config) {
            this.config = config;
            return this;
        }

        @Override
        public QuarkusLangfuseApiBuilder username(String username) {
            throw new UnsupportedOperationException("This operation is not supported by the QuarkusLangfuseApi");
        }

        @Override
        public QuarkusLangfuseApiBuilder password(String password) {
            throw new UnsupportedOperationException("This operation is not supported by the QuarkusLangfuseApi");
        }

        @Override
        public QuarkusLangfuseApiBuilder url(String url) {
            throw new UnsupportedOperationException("This operation is not supported by the QuarkusLangfuseApi");
        }

        @Override
        public QuarkusLangfuseApiBuilder readTimeout(Duration readTimeout) {
            throw new UnsupportedOperationException("This operation is not supported by the QuarkusLangfuseApi");
        }

        @Override
        public QuarkusLangfuseApiBuilder addHeader(String name, String value) {
            throw new UnsupportedOperationException("This operation is not supported by the QuarkusLangfuseApi");
        }

        @Override
        public QuarkusLangfuseApiBuilder logRequests(boolean logRequests) {
            throw new UnsupportedOperationException("This operation is not supported by the QuarkusLangfuseApi");
        }

        @Override
        public QuarkusLangfuseApiBuilder logResponses(boolean logResponses) {
            throw new UnsupportedOperationException("This operation is not supported by the QuarkusLangfuseApi");
        }

        @Override
        public QuarkusLangfuseApiBuilder prettyPrint(boolean prettyPrint) {
            throw new UnsupportedOperationException("This operation is not supported by the QuarkusLangfuseApi");
        }

        public QuarkusLangfuseApi build() {
            return new QuarkusLangfuseApi(this);
        }
    }
}
