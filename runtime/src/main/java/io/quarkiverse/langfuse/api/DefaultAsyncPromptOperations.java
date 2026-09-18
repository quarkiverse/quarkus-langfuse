package io.quarkiverse.langfuse.api;

import java.util.Collection;

import com.langfuse.api.model.CreatePromptRequest;
import com.langfuse.api.model.Prompt;
import com.langfuse.api.model.PromptMeta;
import com.langfuse.api.prompts.PromptsApi.APIPromptsCreateRequest;
import com.langfuse.api.prompts.PromptsApi.APIPromptsDeleteRequest;
import com.langfuse.api.prompts.PromptsApi.APIPromptsGetRequest;
import com.langfuse.api.prompts.PromptsApi.APIPromptsListRequest;
import com.langfuse.api.prompts.async.PromptsApi;

import io.quarkiverse.langfuse.api.deletion.DeletionResult;
import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;
import io.smallrye.mutiny.Uni;

final class DefaultAsyncPromptOperations extends AbstractAsyncPagedOperations<PromptMeta>
        implements AsyncPromptOperations {
    private final PromptsApi promptsApi;

    DefaultAsyncPromptOperations(PromptsApi promptsApi, LangfuseConfig config) {
        super(page -> fetch(promptsApi, page), config);
        this.promptsApi = promptsApi;
    }

    @Override
    public Uni<Prompt> findByName(String promptName) {
        // Validated here rather than inside the completionStage supplier: a throw in there becomes a
        // failure event, and blank input must surface as a thrown IllegalArgumentException.
        var name = ValidationUtils.ensureNotBlank(promptName, "Prompt name");

        // Direct lookup, not scanForName: Langfuse resolves a prompt by name in one request.
        // recoverWithNull is scoped to LangfuseNotFoundException alone: every other failure, a 401
        // included, must still fail the Uni rather than read as absence.
        return Uni.createFrom()
                .completionStage(() -> this.promptsApi.promptsGet(APIPromptsGetRequest.newBuilder()
                        .promptName(name)
                        .build()))
                .onFailure(LangfuseNotFoundException.class)
                .recoverWithNull();
    }

    @Override
    public Uni<Prompt> createIfAbsent(CreatePromptRequest request) {
        // See DefaultAsyncModelOperations.createIfAbsent for why flatMap + ternary. PromptNames absorbs
        // the oneOf wrapper, exactly as EvaluatorNames does for evaluators.
        return findByName(PromptNames.of(request))
                .flatMap(existing -> (existing != null)
                        ? Uni.createFrom().item(existing)
                        : Uni.createFrom()
                                .completionStage(() -> this.promptsApi.promptsCreate(APIPromptsCreateRequest.newBuilder()
                                        .createPromptRequest(request)
                                        .build())));
    }

    @Override
    public Uni<DeletionResult> deleteByName(Collection<String> promptNames) {
        // Validation runs before the deferred wrapper so malformed input throws from the call, as the
        // rest of this tree does. Inside the supplier it would surface as a failed Uni at subscription.
        // deleteConcurrency() stays inside, so the config is read per subscription rather than once here.
        DeletionIdentifiers.validated(promptNames, "Prompt name");

        // Uni.createFrom()::item is the identity resolver, and that is the whole story of this domain:
        // the delete endpoint is keyed on the prompt name, so unlike every other deleteByName in this
        // layer there is no name-to-id scan to perform.
        return Uni.createFrom()
                .deferred(() -> AsyncDeletions.deleteAll(promptNames, "Prompt name", Uni.createFrom()::item,
                        this::delete, deleteConcurrency()));
    }

    // Neither label nor version is supplied, so Langfuse removes every version of the prompt.
    private Uni<?> delete(String promptName) {
        return Uni.createFrom()
                .completionStage(() -> this.promptsApi.promptsDelete(APIPromptsDeleteRequest.newBuilder()
                        .promptName(promptName)
                        .build()));
    }

    private static Uni<PagedResult<PromptMeta>> fetch(PromptsApi promptsApi, Page page) {
        return Uni.createFrom()
                .completionStage(() -> promptsApi.promptsList(APIPromptsListRequest.newBuilder()
                        .page(page.index())
                        .limit(page.size())
                        .build()))
                .map(response -> PagedResults.from(page, response.getData(), response.getMeta()));
    }
}
