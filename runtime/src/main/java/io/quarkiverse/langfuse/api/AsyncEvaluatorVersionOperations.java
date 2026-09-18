package io.quarkiverse.langfuse.api;

import com.langfuse.api.model.EvaluatorVersion;

import io.quarkiverse.langfuse.api.cursor.Cursor;
import io.quarkiverse.langfuse.api.cursor.CursorSelection;

/**
 * Higher-level operations over the stored version history of a single Langfuse evaluator, returning
 * Mutiny types.
 *
 * <p>
 * Obtained from {@link AsyncEvaluatorOperations#versions(String)}. The asynchronous counterpart of
 * {@link EvaluatorVersionOperations}; the two behave identically. Versions are
 * <strong>parent-scoped</strong>, so no operation takes an evaluator id, and
 * <strong>cursor-addressed</strong>, so {@link #stream}, {@link #streamBatches} and
 * {@link #findBatch} - inherited from {@link AsyncCursorOperations} - accept a
 * {@link CursorSelection} or a {@link Cursor}.
 *
 * <p>
 * <strong>Versions are returned newest-first.</strong> Langfuse orders the history by descending
 * version, so the first batch holds the most recent versions and a partial walk - a bounded
 * {@link CursorSelection}, or a {@link #streamAll()} the subscriber cancels - yields the newest
 * versions rather than an arbitrary slice.
 *
 * <p>
 * <strong>Read-only.</strong> Langfuse offers no write operation over the version history: versions
 * appear as a side effect of updating the evaluator, and they are removed only when the evaluator
 * itself is deleted through {@link AsyncEvaluatorOperations#deleteById(String)}. There is therefore
 * no create, update or delete here, and no {@code findById}: the history endpoint is the only way
 * in. That is the API's shape, not an omission.
 *
 * @see EvaluatorVersionOperations
 */
public sealed interface AsyncEvaluatorVersionOperations extends AsyncCursorOperations<EvaluatorVersion>
        permits DefaultAsyncEvaluatorVersionOperations {
}
