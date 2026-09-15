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

    AsyncLangfuseOperations(LangfuseApi langfuseApi, LangfuseConfig config) {
        this.langfuseApi = langfuseApi;
        this.models = new DefaultAsyncModelOperations(langfuseApi.asyncModels(), config);
        this.datasets = new DefaultAsyncDatasetOperations(langfuseApi.asyncDatasets(), config);
        this.llmConnections = new DefaultAsyncLlmConnectionOperations(langfuseApi.asyncLlmConnections(), config);
        this.scoreConfigs = new DefaultAsyncScoreConfigOperations(langfuseApi.asyncScoreConfigs(), config);
        this.evaluationRules = new DefaultAsyncEvaluationRuleOperations(langfuseApi.asyncEvaluationRules(), config);
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
}
