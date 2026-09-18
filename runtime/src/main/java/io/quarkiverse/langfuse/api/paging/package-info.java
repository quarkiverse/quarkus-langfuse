/**
 * Page addressing for the higher-level Langfuse operations layer.
 *
 * <p>
 * Used by the domains in {@link io.quarkiverse.langfuse.api} whose collections Langfuse addresses
 * with a page index: models, datasets, dataset items, LLM connections, score configs, prompts,
 * annotation queues, annotation queue items and comments. The remaining collections are addressed
 * with an opaque cursor instead - see {@link io.quarkiverse.langfuse.api.cursor} - except blob
 * storage integrations, which Langfuse does not paginate at all.
 * <ul>
 * <li>{@link io.quarkiverse.langfuse.api.paging.Page} - a coordinate: a 1-based index and a size</li>
 * <li>{@link io.quarkiverse.langfuse.api.paging.PageSelection} - which pages to visit: {@code all},
 * {@code from}, {@code only}, {@code range}, {@code rangeClosed}</li>
 * <li>{@link io.quarkiverse.langfuse.api.paging.PagedResult} - a page of items plus the totals
 * Langfuse reports</li>
 * </ul>
 */
package io.quarkiverse.langfuse.api.paging;
