/**
 * Delete outcomes for the higher-level Langfuse operations layer.
 *
 * <p>
 * No Langfuse endpoint deletes more than one resource at a time, so a batch delete in
 * {@link io.quarkiverse.langfuse.api} is client-side iteration and can partially apply. These types
 * are the vocabulary for describing what happened to each identifier, rather than a contract any
 * domain must conform to - each domain chooses for itself what it keys its deletes on.
 * <ul>
 * <li>{@link DeletionOutcome} - the terminal state of deleting
 * one identifier: {@code Deleted}, {@code NotFound} or {@code Failed}</li>
 * <li>{@link DeletionResult} - the outcomes of a whole delete
 * operation, keyed by identifier</li>
 * </ul>
 */
package io.quarkiverse.langfuse.api.deletion;
