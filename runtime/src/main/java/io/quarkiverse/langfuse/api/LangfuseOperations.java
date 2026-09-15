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

    LangfuseOperations(LangfuseApi langfuseApi, LangfuseConfig config, AsyncLangfuseOperations async) {
        this.langfuseApi = langfuseApi;
        this.async = async;
        this.models = new DefaultModelOperations(langfuseApi.models(), config);
        this.datasets = new DefaultDatasetOperations(langfuseApi.datasets(), config);
        this.llmConnections = new DefaultLlmConnectionOperations(langfuseApi.llmConnections(), config);
        this.scoreConfigs = new DefaultScoreConfigOperations(langfuseApi.scoreConfigs(), config);
        this.evaluationRules = new DefaultEvaluationRuleOperations(langfuseApi.evaluationRules(), config);
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
}
