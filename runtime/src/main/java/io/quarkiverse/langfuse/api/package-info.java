/**
 * Higher-level operations over the Langfuse API.
 *
 * <p>
 * The generated {@link com.langfuse.api.LangfuseApi} mirrors the Langfuse REST API one endpoint at a
 * time. This package sits above it and offers the operations applications actually tend to write:
 * looking things up by id or by name, checking whether they exist, restricting a collection to the
 * items that matter, and walking paginated collections without hand-rolling the page or cursor
 * arithmetic. Anything not covered here remains one call away through
 * {@link io.quarkiverse.langfuse.api.LangfuseOperations#api()}.
 *
 * <h2>Entry points</h2>
 * <ul>
 * <li>{@link io.quarkiverse.langfuse.api.LangfuseOperations} - the synchronous facade, injectable as
 * a CDI bean</li>
 * <li>{@link io.quarkiverse.langfuse.api.AsyncLangfuseOperations} - its Mutiny counterpart, reachable
 * either through {@link io.quarkiverse.langfuse.api.LangfuseOperations#async()} or as its own
 * standalone injectable bean</li>
 * </ul>
 *
 * <h2>Domain operations</h2>
 * <p>
 * Each domain is a pair of interfaces - one returning plain values, one returning {@code Uni}/
 * {@code Multi} - that behave identically apart from how absence is represented. Each declares the
 * operations its endpoint actually supports and no more, so the domains are deliberately not
 * uniform; where one lacks an operation another has, the reason is given on the interface itself.
 * <ul>
 * <li>{@link io.quarkiverse.langfuse.api.ModelOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncModelOperations}</li>
 * <li>{@link io.quarkiverse.langfuse.api.DatasetOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncDatasetOperations}</li>
 * <li>{@link io.quarkiverse.langfuse.api.DatasetItemOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncDatasetItemOperations}</li>
 * <li>{@link io.quarkiverse.langfuse.api.LlmConnectionOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncLlmConnectionOperations}</li>
 * <li>{@link io.quarkiverse.langfuse.api.ScoreConfigOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncScoreConfigOperations}</li>
 * <li>{@link io.quarkiverse.langfuse.api.EvaluationRuleOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncEvaluationRuleOperations}</li>
 * <li>{@link io.quarkiverse.langfuse.api.EvaluatorOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncEvaluatorOperations}</li>
 * <li>{@link io.quarkiverse.langfuse.api.EvaluatorVersionOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncEvaluatorVersionOperations} - parent-scoped, reached
 * through {@link io.quarkiverse.langfuse.api.EvaluatorOperations#versions(java.lang.String)}</li>
 * <li>{@link io.quarkiverse.langfuse.api.PromptOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncPromptOperations}</li>
 * <li>{@link io.quarkiverse.langfuse.api.AnnotationQueueOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncAnnotationQueueOperations}</li>
 * <li>{@link io.quarkiverse.langfuse.api.AnnotationQueueItemOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncAnnotationQueueItemOperations} - parent-scoped, reached
 * through {@link io.quarkiverse.langfuse.api.AnnotationQueueOperations#items(java.lang.String)}</li>
 * <li>{@link io.quarkiverse.langfuse.api.CommentOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncCommentOperations}</li>
 * <li>{@link io.quarkiverse.langfuse.api.ScoreOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncScoreOperations}</li>
 * <li>{@link io.quarkiverse.langfuse.api.ObservationOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncObservationOperations}</li>
 * <li>{@link io.quarkiverse.langfuse.api.ExperimentOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncExperimentOperations} - reached by choosing a time window
 * on {@link io.quarkiverse.langfuse.api.ExperimentTimeWindow}, which Langfuse requires</li>
 * <li>{@link io.quarkiverse.langfuse.api.ExperimentItemOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncExperimentItemOperations} - reached by choosing a time
 * window on {@link io.quarkiverse.langfuse.api.ExperimentItemTimeWindow}</li>
 * <li>{@link io.quarkiverse.langfuse.api.BlobStorageIntegrationOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncBlobStorageIntegrationOperations}</li>
 * </ul>
 *
 * <h2>Addressing</h2>
 * <p>
 * How a traversal says <em>which</em> part of a collection it wants lives alongside the coordinate
 * types themselves, in one subpackage per addressing model.
 * <ul>
 * <li>{@link io.quarkiverse.langfuse.api.paging} - page addressing, used by models, datasets, dataset
 * items, LLM connections, score configs, prompts, annotation queues, annotation queue items and
 * comments</li>
 * <li>{@link io.quarkiverse.langfuse.api.cursor} - cursor addressing, used by evaluation rules,
 * evaluators, evaluator versions, scores, observations, experiments and experiment items, whose
 * collections Langfuse addresses with an opaque cursor rather than a page index</li>
 * </ul>
 * <p>
 * Blob storage integrations use neither: Langfuse returns that collection whole, in a single
 * response carrying no pagination metadata, so
 * {@link io.quarkiverse.langfuse.api.BlobStorageIntegrationOperations} offers only
 * {@link io.quarkiverse.langfuse.api.BlobStorageIntegrationOperations#findAll()}.
 *
 * <h2>Deletion</h2>
 * <p>
 * How a delete reports what happened to each identifier lives in its own subpackage alongside the
 * addressing ones.
 * <ul>
 * <li>{@link io.quarkiverse.langfuse.api.deletion} - per-identifier delete outcomes, used by every
 * domain above that exposes a delete operation</li>
 * </ul>
 *
 * <h2>Filtering</h2>
 * <p>
 * Domains whose listing endpoint accepts query criteria expose {@code matching(XFilter)}, returning
 * the domain's own type so that every operation above keeps working on the restricted view. The
 * filter types - {@link io.quarkiverse.langfuse.api.CommentFilter},
 * {@link io.quarkiverse.langfuse.api.DatasetItemFilter},
 * {@link io.quarkiverse.langfuse.api.ScoreFilter},
 * {@link io.quarkiverse.langfuse.api.ObservationFilter},
 * {@link io.quarkiverse.langfuse.api.ExperimentFilter} and
 * {@link io.quarkiverse.langfuse.api.ExperimentItemFilter} - stay in this package beside their
 * domain rather than in a subpackage of their own: each names one endpoint's vocabulary, they share
 * no supertype, and none of them means anything away from its domain.
 */
package io.quarkiverse.langfuse.api;
