package io.quarkiverse.langfuse.api;

import java.util.Optional;

import com.langfuse.api.model.CreateScoreConfigRequest;
import com.langfuse.api.model.ScoreConfig;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi.APIScoreConfigsCreateRequest;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi.APIScoreConfigsGetByIdRequest;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi.APIScoreConfigsGetRequest;

import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;

final class DefaultScoreConfigOperations extends AbstractPagedOperations<ScoreConfig> implements ScoreConfigOperations {
    private final ScoreConfigsApi scoreConfigsApi;

    DefaultScoreConfigOperations(ScoreConfigsApi scoreConfigsApi, LangfuseConfig config) {
        super(page -> fetch(scoreConfigsApi, page), config);
        this.scoreConfigsApi = scoreConfigsApi;
    }

    @Override
    public Optional<ScoreConfig> findById(String id) {
        var configId = ValidationUtils.ensureNotBlank(id, "Score config id");

        // Direct GET, so no scan: the server resolves the id. Only LangfuseNotFoundException is caught -
        // catching LangfuseApiException would report a 401 or a 500 as "absent", which is the one
        // mistake this layer must never make.
        try {
            return Optional.of(this.scoreConfigsApi.scoreConfigsGetById(APIScoreConfigsGetByIdRequest.newBuilder()
                    .configId(configId)
                    .build()));
        } catch (LangfuseNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ScoreConfig> findByName(String configName) {
        return scanForName(ValidationUtils.ensureNotBlank(configName, "Score config name"), ScoreConfig::getName);
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
