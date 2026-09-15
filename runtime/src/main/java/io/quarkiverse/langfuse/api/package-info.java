/**
 * Higher-level operations over the Langfuse API.
 *
 * <p>
 * The generated {@link com.langfuse.api.LangfuseApi} mirrors the Langfuse REST API one endpoint at a
 * time. This package sits above it and offers the operations applications actually tend to write:
 * looking things up by name, checking whether they exist, and walking paginated collections without
 * hand-rolling the page or cursor arithmetic. Anything not covered here remains one call away through
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
 * {@code Multi} - that behave identically apart from how absence is represented.
 * <ul>
 * <li>{@link io.quarkiverse.langfuse.api.ModelOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncModelOperations}</li>
 * <li>{@link io.quarkiverse.langfuse.api.DatasetOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncDatasetOperations}</li>
 * <li>{@link io.quarkiverse.langfuse.api.LlmConnectionOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncLlmConnectionOperations}</li>
 * <li>{@link io.quarkiverse.langfuse.api.ScoreConfigOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncScoreConfigOperations}</li>
 * <li>{@link io.quarkiverse.langfuse.api.EvaluationRuleOperations} /
 * {@link io.quarkiverse.langfuse.api.AsyncEvaluationRuleOperations}</li>
 * </ul>
 *
 * <h2>Page addressing</h2>
 * <p>
 * Used by every domain above except evaluation rules.
 * <ul>
 * <li>{@link io.quarkiverse.langfuse.api.Page} - a coordinate: a 1-based index and a size</li>
 * <li>{@link io.quarkiverse.langfuse.api.PageSelection} - which pages to visit: {@code all}, {@code
 * from}, {@code only}, {@code range}, {@code rangeClosed}</li>
 * <li>{@link io.quarkiverse.langfuse.api.PagedResult} - a page of items plus the totals Langfuse
 * reports</li>
 * </ul>
 *
 * <h2>Cursor addressing</h2>
 * <p>
 * Used by evaluation rules, whose collection Langfuse addresses with an opaque cursor rather than a
 * page index.
 * <ul>
 * <li>{@link io.quarkiverse.langfuse.api.Cursor} - a position: an opaque, server-issued value (or
 * none, for the start of the collection) and a limit</li>
 * <li>{@link io.quarkiverse.langfuse.api.CursorSelection} - which batches to visit: {@code all},
 * {@code from}, {@code only}, {@code first}</li>
 * <li>{@link io.quarkiverse.langfuse.api.CursorResult} - a batch of items plus the cursor needed to
 * continue</li>
 * </ul>
 */
package io.quarkiverse.langfuse.api;
