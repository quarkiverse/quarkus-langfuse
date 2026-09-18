package io.quarkiverse.langfuse.api;

import com.langfuse.api.model.EvaluatorVersion;

import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorSelection;

/**
 * Higher-level operations over the stored version history of a single Langfuse evaluator.
 *
 * <p>
 * Obtained from {@link EvaluatorOperations#versions(String)}. Like queue items, versions are
 * <strong>parent-scoped</strong>: every operation here is addressed relative to the evaluator the
 * view was opened on, so no operation takes an evaluator id. Versions are a
 * <strong>cursor-addressed</strong> collection - Langfuse reports only an opaque next cursor, not a
 * page index or totals - so {@link #stream}, {@link #streamBatches} and {@link #findBatch} -
 * inherited from {@link CursorOperations} - accept a {@link CursorSelection} or a {@link Cursor}
 * rather than their page-addressed equivalents.
 *
 * <p>
 * <strong>Versions are returned newest-first.</strong> Langfuse orders the history by descending
 * version, so the first batch holds the most recent versions and a partial walk - a bounded
 * {@link CursorSelection}, or a {@link #streamAll()} the caller stops consuming - yields the newest
 * versions rather than an arbitrary slice.
 *
 * <p>
 * <strong>Read-only.</strong> Langfuse offers no write operation over the version history: versions
 * appear as a side effect of updating the evaluator, and they are removed only when the evaluator
 * itself is deleted through {@link EvaluatorOperations#deleteById(String)}. There is therefore no
 * create, update or delete here, and no {@code findById}: the history endpoint is the only way in.
 * That is the API's shape, not an omission.
 *
 * @see AsyncEvaluatorVersionOperations
 */
public sealed interface EvaluatorVersionOperations extends CursorOperations<EvaluatorVersion>
        permits DefaultEvaluatorVersionOperations {
}
