package io.quarkiverse.langfuse.api;

import java.util.Collection;
import java.util.Optional;

import com.langfuse.api.model.CreatePromptRequest;
import com.langfuse.api.model.Prompt;
import com.langfuse.api.model.PromptMeta;
import com.langfuse.api.prompts.PromptsApi;
import com.langfuse.api.prompts.PromptsApi.APIPromptsCreateRequest;
import com.langfuse.api.prompts.PromptsApi.APIPromptsDeleteRequest;
import com.langfuse.api.prompts.PromptsApi.APIPromptsGetRequest;
import com.langfuse.api.prompts.PromptsApi.APIPromptsListRequest;

import io.quarkiverse.langfuse.api.deletion.DeletionResult;
import io.quarkiverse.langfuse.api.paging.Page;
import io.quarkiverse.langfuse.api.paging.PagedResult;
import io.quarkiverse.langfuse.client.LangfuseNotFoundException;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkiverse.langfuse.util.ValidationUtils;

final class DefaultPromptOperations extends AbstractPagedOperations<PromptMeta> implements PromptOperations {
    private final PromptsApi promptsApi;

    DefaultPromptOperations(PromptsApi promptsApi, LangfuseConfig config) {
        super(page -> fetch(promptsApi, page), config);
        this.promptsApi = promptsApi;
    }

    @Override
    public Optional<Prompt> findByName(String promptName) {
        var name = ValidationUtils.ensureNotBlank(promptName, "Prompt name");

        // Direct lookup, not scanForName: Langfuse resolves a prompt by name in one request. Only
        // LangfuseNotFoundException is caught - catching LangfuseApiException would report a 401 or a
        // 500 as "absent", which is the one mistake this layer must never make.
        try {
            return Optional.of(this.promptsApi.promptsGet(APIPromptsGetRequest.newBuilder()
                    .promptName(name)
                    .build()));
        } catch (LangfuseNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public Prompt createIfAbsent(CreatePromptRequest request) {
        // Prompt and CreatePromptRequest are generated oneOf wrappers, so the name lives on the actual
        // instance rather than on the wrapper: PromptNames absorbs that, exactly as EvaluatorNames does.
        return findByName(PromptNames.of(request))
                .orElseGet(() -> this.promptsApi.promptsCreate(APIPromptsCreateRequest.newBuilder()
                        .createPromptRequest(request)
                        .build()));
    }

    @Override
    public DeletionResult deleteByName(Collection<String> promptNames) {
        // Optional::of is the identity resolver, and that is the whole story of this domain: the delete
        // endpoint is keyed on the prompt name, so unlike every other deleteByName in this layer there
        // is no name-to-id scan to perform. An absent name surfaces as a 404 from the delete itself,
        // which Deletions turns into a NotFound outcome.
        return Deletions.deleteAll(promptNames, "Prompt name", Optional::of, this::delete, deleteConcurrency());
    }

    // Neither label nor version is supplied, so Langfuse removes every version of the prompt.
    private void delete(String promptName) {
        this.promptsApi.promptsDelete(APIPromptsDeleteRequest.newBuilder()
                .promptName(promptName)
                .build());
    }

    private static PagedResult<PromptMeta> fetch(PromptsApi promptsApi, Page page) {
        var response = promptsApi.promptsList(APIPromptsListRequest.newBuilder()
                .page(page.index())
                .limit(page.size())
                .build());

        return PagedResults.from(page, response.getData(), response.getMeta());
    }
}
