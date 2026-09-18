package io.quarkiverse.langfuse.api;

import com.langfuse.api.model.CreateScoreConfigRequest;
import com.langfuse.api.model.ScoreConfig;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi.APIScoreConfigsCreateRequest;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi.APIScoreConfigsGetByIdRequest;
import com.langfuse.api.scoreConfigs.ScoreConfigsApi.APIScoreConfigsGetRequest;
import com.langfuse.api.scoreConfigs.async.ScoreConfigsApi;

import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;
import io.smallrye.mutiny.Uni;

final class DefaultAsyncScoreConfigOperations extends AbstractAsyncPagedOperations<ScoreConfig>
        implements AsyncScoreConfigOperations {
    private final ScoreConfigsApi scoreConfigsApi;

    DefaultAsyncScoreConfigOperations(ScoreConfigsApi scoreConfigsApi, LangfuseConfig config) {
        super(page -> fetch(scoreConfigsApi, page), config);
        this.scoreConfigsApi = scoreConfigsApi;
    }

    @Override
    public Uni<ScoreConfig> findById(String id) {
        // Validated here rather than inside the completionStage supplier: a throw in there becomes a
        // failure event, and blank input must surface as a thrown IllegalArgumentException.
        var configId = ValidationUtils.ensureNotBlank(id, "Score config id");

        // Direct GET, so no scan. recoverWithNull is scoped to LangfuseNotFoundException alone: every
        // other failure, a 401 included, must still fail the Uni rather than read as absence.
        return Uni.createFrom()
                .completionStage(() -> this.scoreConfigsApi.scoreConfigsGetById(APIScoreConfigsGetByIdRequest.newBuilder()
                        .configId(configId)
                        .build()))
                .onFailure(LangfuseNotFoundException.class)
                .recoverWithNull();
    }

    @Override
    public Uni<ScoreConfig> findByName(String configName) {
        return scanForName(ValidationUtils.ensureNotBlank(configName, "Score config name"), ScoreConfig::getName);
    }

    @Override
    public Uni<ScoreConfig> createIfAbsent(CreateScoreConfigRequest request) {
        // See DefaultAsyncModelOperations.createIfAbsent for why flatMap + ternary.
        return findByName(request.getName())
                .flatMap(existing -> (existing != null)
                        ? Uni.createFrom().item(existing)
                        : Uni.createFrom().completionStage(() -> this.scoreConfigsApi.scoreConfigsCreate(
                                APIScoreConfigsCreateRequest.newBuilder()
                                        .createScoreConfigRequest(request)
                                        .build())));
    }

    // See DefaultScoreConfigOperations.fetch: this is the one domain in the layer whose list endpoint
    // is named scoreConfigsGet rather than *List.
    private static Uni<PagedResult<ScoreConfig>> fetch(ScoreConfigsApi scoreConfigsApi, Page page) {
        return Uni.createFrom()
                .completionStage(() -> scoreConfigsApi.scoreConfigsGet(APIScoreConfigsGetRequest.newBuilder()
                        .page(page.index())
                        .limit(page.size())
                        .build()))
                .map(response -> PagedResults.from(page, response.getData(), response.getMeta()));
    }
}
