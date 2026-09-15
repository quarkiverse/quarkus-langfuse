package io.quarkiverse.langfuse.api;

import java.util.Objects;

import com.langfuse.api.model.CreateScoreConfigRequest;
import com.langfuse.api.model.ScoreConfig;

import io.smallrye.mutiny.Uni;

/**
 * Higher-level operations over Langfuse score configs, returning Mutiny types.
 *
 * <p>
 * Obtained from {@link AsyncLangfuseOperations#scoreConfigs()}. The asynchronous counterpart of
 * {@link ScoreConfigOperations}; the two behave identically apart from how absence is represented,
 * which follows each style's own convention: {@link java.util.Optional} for the synchronous tree, a
 * {@code null} item for the asynchronous one.
 *
 * @see ScoreConfigOperations
 */
public sealed interface AsyncScoreConfigOperations extends AsyncPagedOperations<ScoreConfig>
        permits DefaultAsyncScoreConfigOperations {

    /**
     * Finds a score config by its exact name.
     *
     * <p>
     * <strong>Emits {@code null} if no config has that name.</strong>
     *
     * @param configName the score config name to look for, must not be {@code null} or blank
     * @return the matching score config, or {@code null} if no config has that name
     * @throws IllegalArgumentException if {@code configName} is {@code null} or blank
     */
    Uni<ScoreConfig> findByName(String configName);

    /**
     * Whether a score config with the given exact name exists.
     *
     * @param configName the score config name to look for, must not be {@code null} or blank
     * @return {@code true} if a config with that name exists. Never {@code null}
     * @throws IllegalArgumentException if {@code configName} is {@code null} or blank
     */
    default Uni<Boolean> exists(String configName) {
        return findByName(configName)
                .map(Objects::nonNull);
    }

    /**
     * Returns the score config with the requested name, creating it if no config has that name.
     *
     * <p>
     * <strong>Not atomic.</strong> Langfuse offers no create-or-update-by-name operation for score
     * configs, so this performs a lookup followed by a create. Concurrent callers may therefore both
     * observe the config as absent and both create it.
     *
     * @param request the score config to create if it is missing
     * @return the existing or newly created score config. Never {@code null}
     */
    Uni<ScoreConfig> createIfAbsent(CreateScoreConfigRequest request);
}
