package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.function.Function;

import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsDeleteRequest;
import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsListRequest;
import com.langfuse.api.llmConnections.LlmConnectionsApi.APILlmConnectionsUpsertRequest;
import com.langfuse.api.llmConnections.async.LlmConnectionsApi;
import com.langfuse.api.model.LlmConnection;
import com.langfuse.api.model.UpsertLlmConnectionRequest;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;
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
        return scanForName(ValidationUtils.ensureNotBlank(provider, "Provider"), LlmConnection::getProvider);
    }

    @Override
    public Uni<LlmConnection> upsert(UpsertLlmConnectionRequest request) {
        return Uni.createFrom()
                .completionStage(() -> this.llmConnectionsApi.llmConnectionsUpsert(APILlmConnectionsUpsertRequest.newBuilder()
                        .upsertLlmConnectionRequest(request)
                        .build()));
    }

    @Override
    public Uni<DeletionResult> deleteById(Collection<String> ids) {
        return deferDeleteAll(ids, "LLM connection id", Uni.createFrom()::item);
    }

    @Override
    public Uni<DeletionResult> deleteByProvider(Collection<String> providers) {
        return deferDeleteAll(providers, "Provider", this::resolveByProvider);
    }

    // Validation runs before the deferred wrapper so malformed input throws from the call, as the rest
    // of this tree does. Inside the supplier it would surface as a failed Uni at subscription instead.
    // deleteConcurrency() stays inside, so the config is read per subscription rather than once here.
    private Uni<DeletionResult> deferDeleteAll(Collection<String> identifiers, String label,
            Function<String, Uni<String>> resolve) {
        DeletionIdentifiers.validated(identifiers, label);

        return Uni.createFrom()
                .deferred(() -> AsyncDeletions.deleteAll(identifiers, label, resolve, this::delete,
                        deleteConcurrency()));
    }

    private Uni<String> resolveByProvider(String provider) {
        return findByProvider(provider)
                .map(connection -> (connection == null) ? null : connection.getId());
    }

    private Uni<?> delete(String id) {
        return Uni.createFrom()
                .completionStage(() -> this.llmConnectionsApi.llmConnectionsDelete(APILlmConnectionsDeleteRequest
                        .newBuilder()
                        .id(id)
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
