package io.quarkiverse.langfuse.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import com.langfuse.api.model.CreateChatPromptRequest;
import com.langfuse.api.model.CreatePromptRequest;
import com.langfuse.api.model.CreateTextPromptRequest;
import com.langfuse.api.model.Prompt;
import com.langfuse.api.model.PromptMeta;

import io.quarkiverse.langfuse.api.deletion.DeletionOutcome;
import io.quarkiverse.langfuse.api.deletion.DeletionResult;
import io.quarkiverse.langfuse.util.ValidationUtils;
import io.smallrye.mutiny.Uni;

/**
 * Higher-level operations over Langfuse prompts, returning Mutiny types.
 *
 * <p>
 * Obtained from {@link AsyncLangfuseOperations#prompts()}. The asynchronous counterpart of
 * {@link PromptOperations}; the two behave identically apart from how absence is represented, which
 * follows each style's own convention: {@link java.util.Optional} for the synchronous tree, a
 * {@code null} item for the asynchronous one.
 *
 * <p>
 * <strong>Listing and lookup return different types.</strong> Langfuse lists prompts as
 * {@link PromptMeta} - a name with its versions, labels and tags, but without any prompt content -
 * while {@link #findByName(String)} fetches a single {@link Prompt}, which is the polymorphic
 * chat-or-text model.
 *
 * @see PromptOperations
 */
public sealed interface AsyncPromptOperations extends AsyncPagedOperations<PromptMeta> permits DefaultAsyncPromptOperations {

    /**
     * Finds a prompt by its exact name, returning the version Langfuse serves by default.
     *
     * <p>
     * <strong>Emits {@code null} if no prompt has that name.</strong>
     *
     * <p>
     * This is a <strong>direct lookup</strong>: Langfuse resolves the name server-side, so it costs a
     * single request whatever the size of the collection.
     *
     * @param promptName the prompt name to look for, must not be {@code null} or blank
     * @return the matching prompt, or {@code null} if no prompt has that name
     * @throws IllegalArgumentException if {@code promptName} is {@code null} or blank
     */
    Uni<Prompt> findByName(String promptName);

    /**
     * Whether a prompt with the given exact name exists.
     *
     * @param promptName the prompt name to look for, must not be {@code null} or blank
     * @return {@code true} if a prompt with that name exists. Never {@code null}
     * @throws IllegalArgumentException if {@code promptName} is {@code null} or blank
     */
    default Uni<Boolean> exists(String promptName) {
        return findByName(promptName)
                .map(Objects::nonNull);
    }

    /**
     * Returns the prompt with the requested name, creating it if no prompt has that name.
     *
     * <p>
     * <strong>Not atomic.</strong> Langfuse offers no create-or-update-by-name operation for prompts,
     * so this performs a lookup followed by a create. Concurrent callers may therefore both observe the
     * prompt as absent and both create it.
     *
     * <p>
     * Note that Langfuse's create endpoint always adds a <em>new version</em> to the named prompt. This
     * method deliberately does not do so when the name already exists: use
     * {@link AsyncLangfuseOperations#api()} directly to version an existing prompt.
     *
     * @param request the prompt to create if it is missing
     * @return the existing or newly created prompt. Never {@code null}
     * @throws IllegalArgumentException if {@code request} is {@code null}
     */
    Uni<Prompt> createIfAbsent(CreatePromptRequest request);

    /**
     * Convenience overload of {@link #createIfAbsent(CreatePromptRequest)} for a chat prompt.
     *
     * @param request the chat prompt to create if it is missing
     * @return the existing or newly created prompt. Never {@code null}
     * @throws IllegalArgumentException if {@code request} is {@code null}
     */
    default Uni<Prompt> createIfAbsent(CreateChatPromptRequest request) {
        ValidationUtils.ensureNotNull(request, "request");

        return createIfAbsent(new CreatePromptRequest(request));
    }

    /**
     * Convenience overload of {@link #createIfAbsent(CreatePromptRequest)} for a text prompt.
     *
     * @param request the text prompt to create if it is missing
     * @return the existing or newly created prompt. Never {@code null}
     * @throws IllegalArgumentException if {@code request} is {@code null}
     */
    default Uni<Prompt> createIfAbsent(CreateTextPromptRequest request) {
        ValidationUtils.ensureNotNull(request, "request");

        return createIfAbsent(new CreatePromptRequest(request));
    }

    /**
     * Deletes the prompts with the given exact names.
     *
     * <p>
     * <strong>Deletes every version.</strong> Langfuse scopes a prompt deletion by an optional label or
     * version; this layer supplies neither, so <strong>all versions</strong> of each named prompt are
     * removed. Use {@link AsyncLangfuseOperations#api()} directly to delete a single label or version.
     *
     * <p>
     * <strong>No collection scan.</strong> Unlike the name-based deletes on other domains, the Langfuse
     * delete endpoint is keyed on the prompt name itself, so each name is deleted in a single request
     * and an absent name costs no more than a present one.
     *
     * <p>
     * <strong>Absence is not an error.</strong> A name matching no prompt yields a
     * {@link DeletionOutcome.NotFound} outcome rather than failing the {@link Uni}, so deleting
     * something that is already gone is a normal result rather than a failure to handle.
     *
     * <p>
     * <strong>Never fails fast.</strong> Every name is attempted regardless of what happened to the
     * others, and each is reported separately: one failure neither hides the successes nor prevents
     * the remaining work.
     *
     * <p>
     * <strong>Not atomic.</strong> Langfuse offers no bulk delete, so this iterates client-side and
     * can partially apply. Inspect the returned {@link DeletionResult} rather than assuming
     * all-or-nothing.
     *
     * @param promptNames the prompt names to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct name. Never {@code null}
     * @throws IllegalArgumentException if {@code promptNames} is {@code null}, or if any element is
     *         {@code null} or blank. Thrown from this call rather than emitted as a failure
     */
    Uni<DeletionResult> deleteByName(Collection<String> promptNames);

    /**
     * Deletes the prompt with the given exact name, and every one of its versions.
     *
     * @param promptName the prompt name to delete, must not be {@code null} or blank
     * @return the outcome for that name. Never {@code null}
     * @throws IllegalArgumentException if {@code promptName} is {@code null} or blank
     * @see #deleteByName(Collection)
     */
    default Uni<DeletionResult> deleteByName(String promptName) {
        return deleteByName(Collections.singletonList(promptName));
    }

    /**
     * Deletes the prompts with the given exact names, and every one of their versions.
     *
     * @param promptNames the prompt names to delete, must not be {@code null} and must not contain
     *        {@code null} or blank elements; may be empty, in which case no request is issued
     * @return one outcome per distinct name. Never {@code null}
     * @throws IllegalArgumentException if {@code promptNames} is {@code null}, or if any element is
     *         {@code null} or blank
     * @see #deleteByName(Collection)
     */
    default Uni<DeletionResult> deleteByName(String... promptNames) {
        return deleteByName((promptNames == null) ? null : Arrays.asList(promptNames));
    }
}
