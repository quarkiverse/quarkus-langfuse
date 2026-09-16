package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import jakarta.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.model.CreateDatasetRequest;
import com.langfuse.api.model.CreateModelRequest;
import com.langfuse.api.model.CreateScoreConfigRequest;
import com.langfuse.api.model.Dataset;
import com.langfuse.api.model.LlmAdapter;
import com.langfuse.api.model.LlmConnection;
import com.langfuse.api.model.Model;
import com.langfuse.api.model.ModelTokenizerId;
import com.langfuse.api.model.ModelUsageUnit;
import com.langfuse.api.model.ScoreConfig;
import com.langfuse.api.model.ScoreConfigDataType;
import com.langfuse.api.model.UpsertLlmConnectionRequest;

import io.quarkiverse.langfuse.api.AsyncLangfuseOperations;
import io.quarkiverse.langfuse.api.DeletionOutcome;
import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.Page;
import io.quarkiverse.langfuse.api.PageSelection;
import io.quarkiverse.langfuse.api.PagedResult;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

/**
 * Integration tests exercising {@link LangfuseOperations} and {@link AsyncLangfuseOperations}
 * against a real Langfuse instance via Dev Services.
 *
 * <p>
 * These validate what WireMock cannot: that Langfuse's real 1-based page semantics match ours with
 * no off-by-one errors, that cursor tokens round-trip properly against real server responses, and
 * that the end-to-end initializer scenario is genuinely idempotent.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class OperationsIntegrationTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final String RUN_ID = UUID.randomUUID().toString().substring(0, 8);

    // Unique names per test run to prevent interference between runs
    private static final String MODEL_NAME = "it-model-" + RUN_ID;
    private static final String SCORE_CONFIG_NAME = "it-score-config-" + RUN_ID;
    private static final String DATASET_PREFIX = "it-dataset-" + RUN_ID + "-";
    private static final String LLM_PROVIDER = "it-provider-" + RUN_ID;

    // Fixtures the delete scenarios create and destroy themselves. This module has no teardown, and
    // @Order(11) still asserts MODEL_NAME / LLM_PROVIDER / SCORE_CONFIG_NAME exist, so delete tests own
    // a disjoint prefix and never target those.
    private static final String DELETE_MODEL_PREFIX = "it-del-model-" + RUN_ID + "-";
    private static final String DELETE_PROVIDER_PREFIX = "it-del-provider-" + RUN_ID + "-";

    @Inject
    LangfuseOperations langfuse;

    @Inject
    AsyncLangfuseOperations asyncLangfuse;

    // --- Scenario 1: Page-based pagination correctness on real data ------------------------

    @Test
    @Order(1)
    void createDatasetsForPaginationTests() {
        // Create 7 datasets with known names so pagination math can be asserted exactly
        IntStream.rangeClosed(1, 7)
                .mapToObj(i -> CreateDatasetRequest.builder()
                        .name(DATASET_PREFIX + i)
                        .description("Dataset %d for pagination IT".formatted(i))
                        .build())
                .forEach(langfuse.datasets()::createIfAbsent);

        // Verify all 7 exist individually via the single-request lookup
        IntStream.rangeClosed(1, 7)
                .forEach(i -> assertThat(langfuse.datasets().exists(DATASET_PREFIX + i)).isTrue());
    }

    @Test
    @Order(2)
    void paginationWalksEveryPageWithoutGapsOrDuplicates() {
        // Page size 3 over 7 items requires exactly 3 pages (3 + 3 + 1)
        var allDatasets = langfuse.datasets().stream(PageSelection.all(3))
                .map(Dataset::getName)
                .filter(name -> name.startsWith(DATASET_PREFIX))
                .toList();

        var expectedNames = IntStream.rangeClosed(1, 7)
                .mapToObj(i -> DATASET_PREFIX + i)
                .toList();

        // Langfuse returns datasets in reverse chronological order (most recently created first);
        // assert all 7 are present with no duplicates and no gaps
        assertThat(allDatasets)
                .hasSize(7)
                .containsExactlyInAnyOrderElementsOf(expectedNames);
    }

    @Test
    @Order(3)
    void findPageExposesRealServerTotals() {
        // Page 2 at size 3 contains items 4, 5, 6
        PagedResult<Dataset> page2 = langfuse.datasets().findPage(Page.of(2, 3));

        assertThat(page2.items()).hasSize(3);
        assertThat(page2.totalItems()).isGreaterThanOrEqualTo(7);
        assertThat(page2.totalPages()).isGreaterThanOrEqualTo(3);
        assertThat(page2.hasNext()).isTrue();
        assertThat(page2.nextPage()).contains(Page.of(3, 3));
    }

    @Test
    @Order(4)
    void rangeAndRangeClosedBoundTheTraversalOnRealServer() {
        // range(1, 3, 3) = pages 1 and 2 (end-exclusive) = 6 items
        var rangeNames = langfuse.datasets().stream(PageSelection.range(1, 3, 3))
                .map(Dataset::getName)
                .filter(name -> name.startsWith(DATASET_PREFIX))
                .toList();

        assertThat(rangeNames).hasSize(6);

        // rangeClosed(1, 2, 3) = pages 1 and 2 (end-inclusive) = same 6 items
        var rangeClosedNames = langfuse.datasets().stream(PageSelection.rangeClosed(1, 2, 3))
                .map(Dataset::getName)
                .filter(name -> name.startsWith(DATASET_PREFIX))
                .toList();

        assertThat(rangeClosedNames).containsExactlyElementsOf(rangeNames);
    }

    // --- Scenario 2: findByName across a page boundary -------------------------------------

    @Test
    @Order(5)
    void findByNameFindsItemOnLastPageAtSmallPageSize() {
        assertThat(langfuse.models().findByName("non-existent-model-" + RUN_ID)).isEmpty();
        assertThat(langfuse.models().exists("non-existent-model-" + RUN_ID)).isFalse();
    }

    // --- Scenario 3: Real absence through the 404 path -------------------------------------

    @Test
    @Order(6)
    void absenceReturnsEmptyAndFalseOnRealServer() {
        var missingName = "definitely-does-not-exist-" + UUID.randomUUID();

        assertThat(langfuse.datasets().findByName(missingName)).isEmpty();
        assertThat(langfuse.datasets().exists(missingName)).isFalse();

        assertThat(await(asyncLangfuse.datasets().findByName(missingName))).isNull();
        assertThat(await(asyncLangfuse.datasets().exists(missingName))).isFalse();
    }

    // --- Scenario 4: Atomic LLM connection upsert ------------------------------------------

    @Test
    @Order(7)
    void llmConnectionUpsertIsAtomicAndUpdatesInPlace() {
        // First upsert creates the connection
        LlmConnection first = langfuse.llmConnections().upsert(UpsertLlmConnectionRequest.builder()
                .provider(LLM_PROVIDER)
                .adapter(LlmAdapter.OPENAI)
                .secretKey("sk-first-key")
                .build());

        assertThat(first.getProvider()).isEqualTo(LLM_PROVIDER);
        assertThat(first.getAdapter()).isEqualTo("openai");

        // Verify it can be found by provider
        assertThat(langfuse.llmConnections().findByProvider(LLM_PROVIDER))
                .isPresent()
                .get()
                .extracting(LlmConnection::getId)
                .isEqualTo(first.getId());

        // Second upsert replaces it with a different adapter
        LlmConnection second = langfuse.llmConnections().upsert(UpsertLlmConnectionRequest.builder()
                .provider(LLM_PROVIDER)
                .adapter(LlmAdapter.AZURE)
                .secretKey("sk-second-key")
                .build());

        assertThat(second.getProvider()).isEqualTo(LLM_PROVIDER);
        assertThat(second.getAdapter()).isEqualTo("azure");

        // Still only one connection for this provider
        var allForProvider = langfuse.llmConnections().findAll().stream()
                .filter(c -> LLM_PROVIDER.equals(c.getProvider()))
                .toList();

        assertThat(allForProvider).hasSize(1);
    }

    // --- Scenario 5: createIfAbsent idempotency --------------------------------------------

    @Test
    @Order(8)
    void createIfAbsentIsIdempotentForModels() {
        var request = CreateModelRequest.builder()
                .modelName(MODEL_NAME)
                .matchPattern("(?i)^(%s)$".formatted(MODEL_NAME))
                .unit(ModelUsageUnit.TOKENS)
                .inputPrice(0.001)
                .outputPrice(0.002)
                .tokenizerId(ModelTokenizerId.OPENAI)
                .build();

        // First call creates
        Model first = langfuse.models().createIfAbsent(request);
        assertThat(first.getId()).isNotBlank();
        assertThat(first.getModelName()).isEqualTo(MODEL_NAME);

        // Second call returns the existing one without creating a duplicate
        Model second = langfuse.models().createIfAbsent(request);
        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(second.getModelName()).isEqualTo(MODEL_NAME);
    }

    @Test
    @Order(9)
    void createIfAbsentIsIdempotentForScoreConfigs() {
        var request = CreateScoreConfigRequest.builder()
                .name(SCORE_CONFIG_NAME)
                .dataType(ScoreConfigDataType.NUMERIC)
                .build();

        ScoreConfig first = langfuse.scoreConfigs().createIfAbsent(request);
        assertThat(first.getId()).isNotBlank();
        assertThat(first.getName()).isEqualTo(SCORE_CONFIG_NAME);

        ScoreConfig second = langfuse.scoreConfigs().createIfAbsent(request);
        assertThat(second.getId()).isEqualTo(first.getId());
    }

    // --- Scenario 6: Async parity ----------------------------------------------------------

    @Test
    @Order(10)
    void asyncTreeReturnsIdenticalResultsOnRealServer() {
        // findByName parity
        var syncDataset = langfuse.datasets().findByName(DATASET_PREFIX + "1");
        var asyncDataset = await(asyncLangfuse.datasets().findByName(DATASET_PREFIX + "1"));

        assertThat(syncDataset).isPresent();
        assertThat(asyncDataset).isNotNull();
        assertThat(asyncDataset.getId()).isEqualTo(syncDataset.get().getId());

        // exists parity
        assertThat(await(asyncLangfuse.datasets().exists(DATASET_PREFIX + "1"))).isTrue();
        assertThat(await(asyncLangfuse.datasets().exists("definitely-missing-" + RUN_ID))).isFalse();

        // LLM connection lookup parity
        var asyncConn = await(asyncLangfuse.llmConnections().findByProvider(LLM_PROVIDER));
        assertThat(asyncConn).isNotNull();
        assertThat(asyncConn.getProvider()).isEqualTo(LLM_PROVIDER);
    }

    // --- Scenario 7: The motivating LangfuseInitializer scenario end-to-end ----------------

    @Test
    @Order(11)
    void initializerScenarioRunsIdempotently() {
        // Run the complete initialization routine twice - exactly what a Quarkus app does across
        // restarts - and assert that all resources exist, are reachable, and have no duplicates.
        runInitializerRoutine();
        runInitializerRoutine();

        assertThat(langfuse.models().findByName(MODEL_NAME)).isPresent();
        assertThat(langfuse.llmConnections().findByProvider(LLM_PROVIDER)).isPresent();
        assertThat(langfuse.scoreConfigs().findByName(SCORE_CONFIG_NAME)).isPresent();
    }

    // --- Scenario 8: Cursor-addressed evaluation rules operations -------------------------

    @Test
    @Order(12)
    void cursorTraversalsWorkAgainstRealServer() {
        // Evaluation rules cannot be created on community edition (requires enterprise plan),
        // but the cursor traversal engine must run cleanly against the real server collection
        assertThat(langfuse.evaluationRules().findAll()).isNotNull();
        assertThat(langfuse.evaluationRules().findByName("non-existent-rule-" + RUN_ID)).isEmpty();
        assertThat(langfuse.evaluationRules().exists("non-existent-rule-" + RUN_ID)).isFalse();

        assertThat(await(asyncLangfuse.evaluationRules().findAll())).isNotNull();
        assertThat(await(asyncLangfuse.evaluationRules().findByName("non-existent-rule-" + RUN_ID))).isNull();
        assertThat(await(asyncLangfuse.evaluationRules().exists("non-existent-rule-" + RUN_ID))).isFalse();
    }

    // --- Scenario 9: Batch delete against a real server ------------------------------------

    @Test
    @Order(13)
    void deleteByIdReportsDeletedAndNotFoundInOneBatch() {
        var present = createDeletableModel("a");
        var absent = "does-not-exist-" + UUID.randomUUID();

        var result = langfuse.models().deleteById(present, absent);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.outcome(present)).get().isInstanceOf(DeletionOutcome.Deleted.class);
        assertThat(result.outcome(absent)).get().isInstanceOf(DeletionOutcome.NotFound.class);
        assertThat(result.hasFailures()).isFalse();
        assertThat(result.deleted()).containsExactlyInAnyOrder(present);
        assertThat(result.notFound()).containsExactlyInAnyOrder(absent);
    }

    @Test
    @Order(14)
    void deleteByNameCollapsesDuplicatesAndRemovesTheModel() {
        var name = DELETE_MODEL_PREFIX + "dup";
        createModelNamed(name);

        var result = langfuse.models().deleteByName(name, name, name);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.outcome(name)).get().isInstanceOf(DeletionOutcome.Deleted.class);
        assertThat(langfuse.models().findByName(name)).isEmpty();
    }

    @Test
    @Order(15)
    void deletingAnAbsentIdentifierIsNotAnError() {
        var absent = "absent-" + UUID.randomUUID();

        assertThat(langfuse.models().deleteById(absent).outcome(absent))
                .get()
                .isInstanceOf(DeletionOutcome.NotFound.class);

        assertThat(langfuse.models().deleteByName(absent).outcome(absent))
                .get()
                .isInstanceOf(DeletionOutcome.NotFound.class);

        assertThat(langfuse.models().deleteById(List.of()).size()).isZero();
    }

    @Test
    @Order(16)
    void asyncDeleteAgreesWithTheSyncTree() {
        var present = createDeletableModel("async");
        var absent = "does-not-exist-" + UUID.randomUUID();

        var result = await(asyncLangfuse.models().deleteById(present, absent));

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.outcome(present)).get().isInstanceOf(DeletionOutcome.Deleted.class);
        assertThat(result.outcome(absent)).get().isInstanceOf(DeletionOutcome.NotFound.class);

        var byName = DELETE_MODEL_PREFIX + "async-name";
        createModelNamed(byName);

        assertThat(await(asyncLangfuse.models().deleteByName(byName)).outcome(byName))
                .get()
                .isInstanceOf(DeletionOutcome.Deleted.class);
        assertThat(await(asyncLangfuse.models().findByName(byName))).isNull();
    }

    @Test
    @Order(17)
    void deleteByProviderRemovesAnLlmConnection() {
        var provider = DELETE_PROVIDER_PREFIX + "a";

        langfuse.llmConnections().upsert(UpsertLlmConnectionRequest.builder()
                .provider(provider)
                .adapter(LlmAdapter.OPENAI)
                .secretKey("sk-delete-me")
                .build());

        var result = langfuse.llmConnections().deleteByProvider(provider);

        assertThat(result.outcome(provider)).get().isInstanceOf(DeletionOutcome.Deleted.class);
        assertThat(langfuse.llmConnections().findByProvider(provider)).isEmpty();
        assertThat(langfuse.llmConnections().findByProvider(LLM_PROVIDER)).isPresent();
    }

    @Test
    @Order(18)
    void cursorAddressedDomainsReportAbsentIdentifiersAsNotFound() {
        var absent = "absent-" + UUID.randomUUID();

        assertThat(langfuse.evaluationRules().deleteByName(absent).outcome(absent))
                .get()
                .isInstanceOf(DeletionOutcome.NotFound.class);

        assertThat(langfuse.evaluators().deleteByName(absent).outcome(absent))
                .get()
                .isInstanceOf(DeletionOutcome.NotFound.class);

        assertThat(await(asyncLangfuse.evaluationRules().deleteByName(absent)).outcome(absent))
                .get()
                .isInstanceOf(DeletionOutcome.NotFound.class);

        assertThat(await(asyncLangfuse.evaluators().deleteByName(absent)).outcome(absent))
                .get()
                .isInstanceOf(DeletionOutcome.NotFound.class);
    }

    @Test
    @Order(19)
    void deletingALangfuseManagedModelIsReportedAsNotFound() {
        // Langfuse refuses to delete its built-in model definitions, but answers 404 rather than 4xx:
        // "No model with this id found. Note: You cannot delete built-in models". Our mapping turns that
        // into NotFound, and the model survives - so refusal is observable as absence plus survival, not
        // as a failure. Guarded because seeded built-ins are a property of the image, not of this code.
        var managed = langfuse.models().stream(PageSelection.all())
                .filter(model -> Boolean.TRUE.equals(model.getIsLangfuseManaged()))
                .map(Model::getId)
                .findFirst();

        assumeTrue(managed.isPresent(), "no Langfuse-managed model is seeded in this instance");

        var result = langfuse.models().deleteById(managed.get());

        assertThat(result.hasFailures()).isFalse();
        assertThat(result.outcome(managed.get())).get().isInstanceOf(DeletionOutcome.NotFound.class);
        assertThat(langfuse.models().stream(PageSelection.all()).map(Model::getId)).contains(managed.get());
    }

    @Test
    @Order(20)
    void syncBatchDeleteInvokedFromAWorkerThreadCompletesRatherThanDeadlocking() throws Exception {
        var ids = IntStream.rangeClosed(1, 6)
                .mapToObj(i -> createDeletableModel("worker-" + i))
                .toList();

        // The fan-out submits to the same pool this call runs on. A timeout here is the starvation that
        // running one unit on the calling thread exists to prevent.
        var result = CompletableFuture
                .supplyAsync(() -> langfuse.models().deleteById(ids), Infrastructure.getDefaultExecutor())
                .get(30, TimeUnit.SECONDS);

        assertThat(result.size()).isEqualTo(6);
        assertThat(result.deleted()).containsExactlyInAnyOrderElementsOf(ids);
    }

    private String createDeletableModel(String suffix) {
        return createModelNamed(DELETE_MODEL_PREFIX + suffix);
    }

    private String createModelNamed(String name) {
        return langfuse.models().createIfAbsent(CreateModelRequest.builder()
                .modelName(name)
                .matchPattern("(?i)^(%s)$".formatted(name))
                .unit(ModelUsageUnit.TOKENS)
                .inputPrice(0.001)
                .outputPrice(0.002)
                .tokenizerId(ModelTokenizerId.OPENAI)
                .build())
                .getId();
    }

    private void runInitializerRoutine() {
        langfuse.models().createIfAbsent(CreateModelRequest.builder()
                .modelName(MODEL_NAME)
                .matchPattern("(?i)^(%s)$".formatted(MODEL_NAME))
                .unit(ModelUsageUnit.TOKENS)
                .inputPrice(0.001)
                .outputPrice(0.002)
                .tokenizerId(ModelTokenizerId.OPENAI)
                .build());

        langfuse.llmConnections().upsert(UpsertLlmConnectionRequest.builder()
                .provider(LLM_PROVIDER)
                .adapter(LlmAdapter.AZURE)
                .secretKey("sk-init-test")
                .build());

        langfuse.scoreConfigs().createIfAbsent(CreateScoreConfigRequest.builder()
                .name(SCORE_CONFIG_NAME)
                .dataType(ScoreConfigDataType.NUMERIC)
                .build());
    }

    private static <T> T await(Uni<T> uni) {
        return uni.await().atMost(TIMEOUT);
    }
}
