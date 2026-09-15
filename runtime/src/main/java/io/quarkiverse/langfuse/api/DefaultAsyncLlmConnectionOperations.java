package io.quarkiverse.langfuse.api;

import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsListRequest;
import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsUpsertRequest;
import com.langfuse.api.llmConnections.async.LlmConnectionsApi;
import com.langfuse.api.model.LlmConnection;
import com.langfuse.api.model.UpsertLlmConnectionRequest;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.smallrye.mutiny.Uni;

final class DefaultAsyncLlmConnectionOperations extends AbstractAsyncPagedOperations<LlmConnection>
        implements AsyncLlmConnectionOperations {
    private final LlmConnectionsApi llmConnectionsApi;

    DefaultAsyncLlmConnectionOperations(LlmConnectionsApi llmConnectionsApi, LangfuseConfig config) {
        super(page -> fetch(llmConnectionsApi, page), config);
        this.llmConnectionsApi = llmConnectionsApi;
    }

    @Override
    public Uni<LlmConnection> findByProvider(String provider) {
        return scanForName(Names.require(provider, "Provider"), LlmConnection::getProvider);
    }

    @Override
    public Uni<LlmConnection> upsert(UpsertLlmConnectionRequest request) {
        return Uni.createFrom()
                .completionStage(() -> this.llmConnectionsApi.llmConnectionsUpsert(APILlmConnectionsUpsertRequest.newBuilder()
                        .upsertLlmConnectionRequest(request)
                        .build()));
    }

    private static Uni<PagedResult<LlmConnection>> fetch(LlmConnectionsApi llmConnectionsApi, Page page) {
        return Uni.createFrom()
                .completionStage(() -> llmConnectionsApi.llmConnectionsList(APILlmConnectionsListRequest.newBuilder()
                        .page(page.index())
                        .limit(page.size())
                        .build()))
                .map(response -> PagedResults.from(page, response.getData(), response.getMeta()));
    }
}
