/**
 * Cursor addressing for the higher-level Langfuse operations layer.
 *
 * <p>
 * Used by evaluation rules, evaluators, evaluator versions, scores, observations, experiments and
 * experiment items, whose collections Langfuse addresses with an opaque cursor rather than a page
 * index. The other domains in {@link io.quarkiverse.langfuse.api} are addressed by page instead -
 * see {@link io.quarkiverse.langfuse.api.paging} - except blob storage integrations, which Langfuse
 * returns whole and does not paginate.
 * <ul>
 * <li>{@link io.quarkiverse.langfuse.api.cursor.Cursor} - a position: an opaque, server-issued value
 * (or none, for the start of the collection) and a limit</li>
 * <li>{@link io.quarkiverse.langfuse.api.cursor.CursorSelection} - which batches to visit:
 * {@code all}, {@code from}, {@code only}, {@code first}</li>
 * <li>{@link io.quarkiverse.langfuse.api.cursor.CursorResult} - a batch of items plus the cursor
 * needed to continue</li>
 * </ul>
 */
package io.quarkiverse.langfuse.api.cursor;
