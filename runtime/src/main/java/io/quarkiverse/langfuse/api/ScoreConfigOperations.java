package io.quarkiverse.langfuse.api;

import java.util.Optional;

import com.langfuse.api.model.CreateScoreConfigRequest;
import com.langfuse.api.model.ScoreConfig;

/**
 * Higher-level operations over Langfuse score configs.
 *
 * <p>
 * Obtained from {@link LangfuseOperations#scoreConfigs()}. Score configs are a page-addressed
 * collection, so {@link #stream}, {@link #streamPages} and {@link #findPage} - inherited from
 * {@link PagedOperations} - accept a {@link PageSelection} or a {@link Page} directly.
 *
 * @see AsyncScoreConfigOperations
 */
public sealed interface ScoreConfigOperations extends PagedOperations<ScoreConfig> permits DefaultScoreConfigOperations {

    /**
     * Finds a score config by its exact name.
     *
     * <p>
     * Langfuse offers no name filter for score configs, so this walks the collection and stops at the
     * first match: a config found in the first page costs a single request.
     *
     * @param configName the score config name to look for, must not be {@code null} or blank
     * @return the matching score config, or empty if no config has that name
     * @throws IllegalArgumentException if {@code configName} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         config not existing
     */
    Optional<ScoreConfig> findByName(String configName);

    /**
     * Whether a score config with the given exact name exists.
     *
     * @param configName the score config name to look for, must not be {@code null} or blank
     * @return {@code true} if a config with that name exists
     * @throws IllegalArgumentException if {@code configName} is {@code null} or blank
     * @throws com.langfuse.api.LangfuseApiException if the request fails for any reason other than the
     *         config not existing
     */
    default boolean exists(String configName) {
        return findByName(configName).isPresent();
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
     * @return the existing or newly created score config
     */
    ScoreConfig createIfAbsent(CreateScoreConfigRequest request);
}
