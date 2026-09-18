package io.quarkiverse.langfuse.api;

import jakarta.enterprise.context.ApplicationScoped;

import com.langfuse.api.LangfuseApi;

import io.quarkiverse.langfuse.config.LangfuseConfig;

/**
 * Higher-level operations over the Langfuse API.
 *
 * <p>
 * The generated {@link LangfuseApi} mirrors the Langfuse REST API one endpoint at a time. This bean
 * sits above it and offers the operations applications actually tend to write: looking things up by
 * name, checking whether they exist, and walking paginated collections without hand-rolling the page
 * arithmetic.
 *
 * <pre>{@code
 * @Inject
 * LangfuseOperations langfuse;
 *
 * langfuse.models().findByName("gpt-4o"); // curated, synchronous
 * langfuse.async().models().findByName("gpt-4o"); // curated, asynchronous
 * langfuse.api().prompts().promptsList(request); // the full generated API
 * }</pre>
 *
 * <p>
 * Anything not covered here remains one call away through {@link #api()}, so this layer never has to
 * be complete to be useful.
 *
 * @see AsyncLangfuseOperations
 */
@ApplicationScoped
public class LangfuseOperations {
    private final LangfuseApi langfuseApi;
    private final AsyncLangfuseOperations async;
    private final ModelOperations models;
    private final DatasetOperations datasets;
    private final LlmConnectionOperations llmConnections;
    private final ScoreConfigOperations scoreConfigs;
    private final EvaluationRuleOperations evaluationRules;
    private final EvaluatorOperations evaluators;
    private final PromptOperations prompts;
    private final AnnotationQueueOperations annotationQueues;
    private final CommentOperations comments;
    private final DatasetItemOperations datasetItems;
    private final ExperimentTimeWindow experiments;
    private final ExperimentItemTimeWindow experimentItems;
    private final ObservationOperations observations;
    private final ScoreOperations scores;
    private final BlobStorageIntegrationOperations blobStorageIntegrations;

    LangfuseOperations(LangfuseApi langfuseApi, LangfuseConfig config, AsyncLangfuseOperations async) {
        this.langfuseApi = langfuseApi;
        this.async = async;
        this.models = new DefaultModelOperations(langfuseApi.models(), config);
        this.datasets = new DefaultDatasetOperations(langfuseApi.datasets(), config);
        this.llmConnections = new DefaultLlmConnectionOperations(langfuseApi.llmConnections(), config);
        this.scoreConfigs = new DefaultScoreConfigOperations(langfuseApi.scoreConfigs(), config);
        this.evaluationRules = new DefaultEvaluationRuleOperations(langfuseApi.evaluationRules(), config);
        this.evaluators = new DefaultEvaluatorOperations(langfuseApi.evaluators(), config);
        this.prompts = new DefaultPromptOperations(langfuseApi.prompts(), config);
        this.annotationQueues = new DefaultAnnotationQueueOperations(langfuseApi.annotationQueues(), config);
        this.comments = new DefaultCommentOperations(langfuseApi.comments(), config);
        this.datasetItems = new DefaultDatasetItemOperations(langfuseApi.datasetItems(), config);
        this.experiments = new DefaultExperimentTimeWindow(langfuseApi.experiments(), config);
        this.experimentItems = new DefaultExperimentItemTimeWindow(langfuseApi.experiments(), config);
        this.observations = new DefaultObservationOperations(langfuseApi.observations(), config);
        this.scores = new DefaultScoreOperations(langfuseApi.scoresV3(), langfuseApi.scores(),
                langfuseApi.legacyScoreV1(), config);
        this.blobStorageIntegrations = new DefaultBlobStorageIntegrationOperations(langfuseApi.blobStorageIntegrations(),
                config);
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
     * The same operations, returning Mutiny types.
     *
     * @return the asynchronous operations
     */
    public AsyncLangfuseOperations async() {
        return this.async;
    }

    /**
     * Operations over model definitions.
     *
     * @return the model operations
     */
    public ModelOperations models() {
        return this.models;
    }

    /**
     * Operations over datasets.
     *
     * @return the dataset operations
     */
    public DatasetOperations datasets() {
        return this.datasets;
    }

    /**
     * Operations over LLM connections.
     *
     * @return the LLM connection operations
     */
    public LlmConnectionOperations llmConnections() {
        return this.llmConnections;
    }

    /**
     * Operations over score configs.
     *
     * @return the score config operations
     */
    public ScoreConfigOperations scoreConfigs() {
        return this.scoreConfigs;
    }

    /**
     * Operations over evaluation rules.
     *
     * @return the evaluation rule operations
     */
    public EvaluationRuleOperations evaluationRules() {
        return this.evaluationRules;
    }

    /**
     * Operations over evaluators.
     *
     * @return the evaluator operations
     */
    public EvaluatorOperations evaluators() {
        return this.evaluators;
    }

    /**
     * Operations over prompts.
     *
     * @return the prompt operations
     */
    public PromptOperations prompts() {
        return this.prompts;
    }

    /**
     * Operations over annotation queues.
     *
     * @return the annotation queue operations
     */
    public AnnotationQueueOperations annotationQueues() {
        return this.annotationQueues;
    }

    /**
     * Operations over comments.
     *
     * @return the comment operations
     */
    public CommentOperations comments() {
        return this.comments;
    }

    /**
     * Operations over dataset items.
     *
     * @return the dataset item operations
     */
    public DatasetItemOperations datasetItems() {
        return this.datasetItems;
    }

    /**
     * Operations over experiments, reached by choosing a time window.
     *
     * <p>
     * Langfuse requires a lower bound on the time range, so the collection is reached through
     * {@link ExperimentTimeWindow#since(java.time.OffsetDateTime) since} or
     * {@link ExperimentTimeWindow#between(java.time.OffsetDateTime, java.time.OffsetDateTime) between}
     * rather than directly.
     *
     * @return the entry point to the experiment operations
     */
    public ExperimentTimeWindow experiments() {
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
     * {@link ExperimentItemTimeWindow#since(java.time.OffsetDateTime) since} or
     * {@link ExperimentItemTimeWindow#between(java.time.OffsetDateTime, java.time.OffsetDateTime)
     * between} rather than directly.
     *
     * @return the entry point to the experiment item operations
     */
    public ExperimentItemTimeWindow experimentItems() {
        return this.experimentItems;
    }

    /**
     * Operations over observations.
     *
     * @return the observation operations
     */
    public ObservationOperations observations() {
        return this.observations;
    }

    /**
     * Operations over scores.
     *
     * @return the score operations
     */
    public ScoreOperations scores() {
        return this.scores;
    }

    /**
     * Operations over blob storage integrations.
     *
     * @return the blob storage integration operations
     */
    public BlobStorageIntegrationOperations blobStorageIntegrations() {
        return this.blobStorageIntegrations;
    }
}
