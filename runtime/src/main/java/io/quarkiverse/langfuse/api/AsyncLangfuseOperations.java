package io.quarkiverse.langfuse.api;

import jakarta.enterprise.context.ApplicationScoped;

import com.langfuse.api.LangfuseApi;

import io.quarkiverse.langfuse.config.LangfuseConfig;

/**
 * Higher-level operations over the Langfuse API, returning Mutiny types.
 *
 * <p>
 * The asynchronous counterpart of {@link LangfuseOperations}, also reachable through
 * {@link LangfuseOperations#async()}.
 *
 * <p>
 * The two trees are independent implementations over the synchronous and asynchronous halves of the
 * generated client: neither blocks on the other, and neither fakes asynchrony by moving blocking calls
 * to a worker thread.
 *
 * @see LangfuseOperations
 */
@ApplicationScoped
public class AsyncLangfuseOperations {
    private final LangfuseApi langfuseApi;
    private final AsyncModelOperations models;
    private final AsyncDatasetOperations datasets;
    private final AsyncLlmConnectionOperations llmConnections;
    private final AsyncScoreConfigOperations scoreConfigs;
    private final AsyncEvaluationRuleOperations evaluationRules;
    private final AsyncEvaluatorOperations evaluators;
    private final AsyncPromptOperations prompts;
    private final AsyncAnnotationQueueOperations annotationQueues;
    private final AsyncCommentOperations comments;
    private final AsyncDatasetItemOperations datasetItems;
    private final AsyncExperimentTimeWindow experiments;
    private final AsyncExperimentItemTimeWindow experimentItems;
    private final AsyncObservationOperations observations;
    private final AsyncScoreOperations scores;
    private final AsyncBlobStorageIntegrationOperations blobStorageIntegrations;

    AsyncLangfuseOperations(LangfuseApi langfuseApi, LangfuseConfig config) {
        this.langfuseApi = langfuseApi;
        this.models = new DefaultAsyncModelOperations(langfuseApi.asyncModels(), config);
        this.datasets = new DefaultAsyncDatasetOperations(langfuseApi.asyncDatasets(), config);
        this.llmConnections = new DefaultAsyncLlmConnectionOperations(langfuseApi.asyncLlmConnections(), config);
        this.scoreConfigs = new DefaultAsyncScoreConfigOperations(langfuseApi.asyncScoreConfigs(), config);
        this.evaluationRules = new DefaultAsyncEvaluationRuleOperations(langfuseApi.asyncEvaluationRules(), config);
        this.evaluators = new DefaultAsyncEvaluatorOperations(langfuseApi.asyncEvaluators(), config);
        this.prompts = new DefaultAsyncPromptOperations(langfuseApi.asyncPrompts(), config);
        this.annotationQueues = new DefaultAsyncAnnotationQueueOperations(langfuseApi.asyncAnnotationQueues(), config);
        this.comments = new DefaultAsyncCommentOperations(langfuseApi.asyncComments(), config);
        this.datasetItems = new DefaultAsyncDatasetItemOperations(langfuseApi.asyncDatasetItems(), config);
        this.experiments = new DefaultAsyncExperimentTimeWindow(langfuseApi.asyncExperiments(), config);
        this.experimentItems = new DefaultAsyncExperimentItemTimeWindow(langfuseApi.asyncExperiments(), config);
        this.observations = new DefaultAsyncObservationOperations(langfuseApi.asyncObservations(), config);
        this.scores = new DefaultAsyncScoreOperations(langfuseApi.asyncScoresV3(), langfuseApi.asyncScores(),
                langfuseApi.asyncLegacyScoreV1(), config);
        this.blobStorageIntegrations = new DefaultAsyncBlobStorageIntegrationOperations(
                langfuseApi.asyncBlobStorageIntegrations(), config);
    }

    /**
     * The full generated Langfuse API, for anything this layer does not cover.
     *
     * @return the underlying {@link LangfuseApi}
     */
    public LangfuseApi api() {
        return this.langfuseApi;
    }

    /**
     * Operations over model definitions.
     *
     * @return the model operations
     */
    public AsyncModelOperations models() {
        return this.models;
    }

    /**
     * Operations over datasets.
     *
     * @return the dataset operations
     */
    public AsyncDatasetOperations datasets() {
        return this.datasets;
    }

    /**
     * Operations over LLM connections.
     *
     * @return the LLM connection operations
     */
    public AsyncLlmConnectionOperations llmConnections() {
        return this.llmConnections;
    }

    /**
     * Operations over score configs.
     *
     * @return the score config operations
     */
    public AsyncScoreConfigOperations scoreConfigs() {
        return this.scoreConfigs;
    }

    /**
     * Operations over evaluation rules.
     *
     * @return the evaluation rule operations
     */
    public AsyncEvaluationRuleOperations evaluationRules() {
        return this.evaluationRules;
    }

    /**
     * Operations over evaluators.
     *
     * @return the evaluator operations
     */
    public AsyncEvaluatorOperations evaluators() {
        return this.evaluators;
    }

    /**
     * Operations over prompts.
     *
     * @return the prompt operations
     */
    public AsyncPromptOperations prompts() {
        return this.prompts;
    }

    /**
     * Operations over annotation queues.
     *
     * @return the annotation queue operations
     */
    public AsyncAnnotationQueueOperations annotationQueues() {
        return this.annotationQueues;
    }

    /**
     * Operations over comments.
     *
     * @return the comment operations
     */
    public AsyncCommentOperations comments() {
        return this.comments;
    }

    /**
     * Operations over dataset items.
     *
     * @return the dataset item operations
     */
    public AsyncDatasetItemOperations datasetItems() {
        return this.datasetItems;
    }

    /**
     * Operations over experiments, reached by choosing a time window.
     *
     * <p>
     * Langfuse requires a lower bound on the time range, so the collection is reached through
     * {@link AsyncExperimentTimeWindow#since(java.time.OffsetDateTime) since} or
     * {@link AsyncExperimentTimeWindow#between(java.time.OffsetDateTime, java.time.OffsetDateTime)
     * between} rather than directly.
     *
     * @return the entry point to the experiment operations
     */
    public AsyncExperimentTimeWindow experiments() {
        return this.experiments;
    }

    /**
     * Operations over experiment items, reached by choosing a time window.
     *
     * <p>
     * A top-level collection rather than a sub-collection of {@link #experiments()}: Langfuse takes
     * the parent experiment as a query criterion, so it is a filter rather than a scoping step.
     *
     * <p>
     * Langfuse requires a lower bound on the time range, so the collection is reached through
     * {@link AsyncExperimentItemTimeWindow#since(java.time.OffsetDateTime) since} or
     * {@link AsyncExperimentItemTimeWindow#between(java.time.OffsetDateTime, java.time.OffsetDateTime)
     * between} rather than directly.
     *
     * @return the entry point to the experiment item operations
     */
    public AsyncExperimentItemTimeWindow experimentItems() {
        return this.experimentItems;
    }

    /**
     * Operations over observations.
     *
     * @return the observation operations
     */
    public AsyncObservationOperations observations() {
        return this.observations;
    }

    /**
     * Operations over scores.
     *
     * @return the score operations
     */
    public AsyncScoreOperations scores() {
        return this.scores;
    }

    /**
     * Operations over blob storage integrations.
     *
     * @return the blob storage integration operations
     */
    public AsyncBlobStorageIntegrationOperations blobStorageIntegrations() {
        return this.blobStorageIntegrations;
    }
}
