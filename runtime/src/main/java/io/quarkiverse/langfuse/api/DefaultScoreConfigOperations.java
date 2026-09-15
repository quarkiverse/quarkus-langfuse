package io.quarkiverse.langfuse.api;

import java.util.Optional;

import com.langfuse.api.model.CreateScoreConfigRequest;
import com.langfuse.api.model.ScoreConfig;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi.APIScoreConfigsCreateRequest;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi.APIScoreConfigsGetRequest;

import io.quarkiverse.langfuse.config.LangfuseConfig;

final class DefaultScoreConfigOperations extends AbstractPagedOperations<ScoreConfig> implements ScoreConfigOperations {
    private final ScoreConfigsApi scoreConfigsApi;

    DefaultScoreConfigOperations(ScoreConfigsApi scoreConfigsApi, LangfuseConfig config) {
        super(page -> fetch(scoreConfigsApi, page), config);
        this.scoreConfigsApi = scoreConfigsApi;
    }

    @Override
    public Optional<ScoreConfig> findByName(String configName) {
        return scanForName(Names.require(configName, "Score config name"), ScoreConfig::getName);
    }

    @Override
    public ScoreConfig createIfAbsent(CreateScoreConfigRequest request) {
        return findByName(request.getName())
                .orElseGet(() -> this.scoreConfigsApi.scoreConfigsCreate(APIScoreConfigsCreateRequest.newBuilder()
                        .createScoreConfigRequest(request)
                        .build()));
    }

    /**
     * Score configs are listed via {@code scoreConfigsGet}, not {@code scoreConfigsList} - the only
     * domain in this layer where the generated method name does not follow the {@code *List} pattern.
     */
    private static PagedResult<ScoreConfig> fetch(ScoreConfigsApi scoreConfigsApi, Page page) {
        var response = scoreConfigsApi.scoreConfigsGet(APIScoreConfigsGetRequest.newBuilder()
                .page(page.index())
                .limit(page.size())
                .build());

        return PagedResults.from(page, response.getData(), response.getMeta());
    }
}
