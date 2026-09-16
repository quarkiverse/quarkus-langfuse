package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.Optional;

import com.langfuse.api.llmConnections.LlmConnectionsApi;
import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsDeleteRequest;
import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsListRequest;
import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsUpsertRequest;
import com.langfuse.api.model.LlmConnection;
import com.langfuse.api.model.UpsertLlmConnectionRequest;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;

final class DefaultLlmConnectionOperations extends AbstractPagedOperations<LlmConnection> implements LlmConnectionOperations {
    private final LlmConnectionsApi llmConnectionsApi;

    DefaultLlmConnectionOperations(LlmConnectionsApi llmConnectionsApi, LangfuseConfig config) {
        super(page -> fetch(llmConnectionsApi, page), config);
        this.llmConnectionsApi = llmConnectionsApi;
    }

    @Override
    public Optional<LlmConnection> findByProvider(String provider) {
        return scanForName(ValidationUtils.ensureNotBlank(provider, "Provider"), LlmConnection::getProvider);
    }

    @Override
    public LlmConnection upsert(UpsertLlmConnectionRequest request) {
        return this.llmConnectionsApi.llmConnectionsUpsert(APILlmConnectionsUpsertRequest.newBuilder()
                .upsertLlmConnectionRequest(request)
                .build());
    }

    @Override
    public DeletionResult deleteById(Collection<String> ids) {
        return Deletions.deleteAll(ids, "LLM connection id", Optional::of, this::delete, deleteConcurrency());
    }

    @Override
    public DeletionResult deleteByProvider(Collection<String> providers) {
        return Deletions.deleteAll(providers, "Provider", this::resolveByProvider, this::delete, deleteConcurrency());
    }

    private Optional<String> resolveByProvider(String provider) {
        return findByProvider(provider)
                .map(LlmConnection::getId);
    }

    private void delete(String id) {
        this.llmConnectionsApi.llmConnectionsDelete(APILlmConnectionsDeleteRequest.newBuilder()
                .id(id)
                .build());
    }

    private static PagedResult<LlmConnection> fetch(LlmConnectionsApi llmConnectionsApi, Page page) {
        var response = llmConnectionsApi.llmConnectionsList(APILlmConnectionsListRequest.newBuilder()
                .page(page.index())
                .limit(page.size())
                .build());

        return PagedResults.from(page, response.getData(), response.getMeta());
    }
}
