package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import jakarta.inject.Inject;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.LangfuseApiException;
import com.langfuse.api.experiments.ExperimentsApi.APIExperimentsListItemsRequest;
import com.langfuse.api.experiments.ExperimentsApi.APIExperimentsListRequest;
import com.langfuse.api.model.AnnotationQueueObjectType;
import com.langfuse.api.model.AnnotationQueueStatus;
import com.langfuse.api.model.CommentObjectType;
import com.langfuse.api.model.CreateAnnotationQueueItemRequest;
import com.langfuse.api.model.CreateAnnotationQueueRequest;
import com.langfuse.api.model.CreateCommentRequest;
import com.langfuse.api.model.CreateDatasetItemRequest;
import com.langfuse.api.model.CreateDatasetRequest;
import com.langfuse.api.model.CreatePromptRequest;
import com.langfuse.api.model.CreateScoreConfigRequest;
import com.langfuse.api.model.CreateTextPromptRequest;
import com.langfuse.api.model.CreateTextPromptType;
import com.langfuse.api.model.DatasetItem;
import com.langfuse.api.model.PromptMeta;
import com.langfuse.api.model.ScoreConfigDataType;
import com.langfuse.api.model.UpdateAnnotationQueueItemRequest;
import com.langfuse.api.prompts.PromptsApi.APIPromptsCreateRequest;

import io.quarkiverse.langfuse.api.AsyncLangfuseOperations;
import io.quarkiverse.langfuse.api.CommentFilter;
import io.quarkiverse.langfuse.api.DatasetItemFilter;
import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.api.deletion.DeletionOutcome;
import io.quarkiverse.langfuse.api.paging.PageSelection;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;

/**
 * Integration coverage for the domains the operations layer gained in the eleven-domain expansion,
 * exercising the behaviours a WireMock stub structurally cannot reach.
 *
 * <p>
 * Three of those behaviours motivate this class:
 *
 * <ul>
 * <li><strong>Required query parameters.</strong> Langfuse documents {@code fromStartTime} as
 * required on both experiment listings and answers {@code 400} without it. A stub matches on path
 * and method and serves the request anyway, so only a real server proves the
 * {@code since}/{@code between} gateway makes the call satisfiable - and that an unbounded call
 * really would have failed.</li>
 * <li><strong>Filters having an effect.</strong> A stub proves a filter parameter is <em>sent</em>.
 * Only a real server proves Langfuse acts on it, which is asserted here by showing the filtered
 * result set is genuinely narrower than the unfiltered one.</li>
 * <li><strong>Delete semantics.</strong> Absence, success and the prompt domain's all-versions scope
 * are all server behaviours.</li>
 * </ul>
 *
 * <p>
 * Every fixture is scoped to {@link #RUN_ID}, and the destructive scenarios create their own under a
 * distinct {@code it-del-} prefix so that nothing a later {@code @Order} asserts on can be removed by
 * an earlier one. This module has no teardown.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class ExpandedOperationsTests {
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final Duration INGESTION_TIMEOUT = Duration.ofSeconds(60);
    private static final String RUN_ID = UUID.randomUUID().toString().substring(0, 8);
    private static final OffsetDateTime WINDOW_START = OffsetDateTime.now().minusHours(1);

    // Fixtures later @Order-ed methods assert on. Nothing destructive may name these.
    private static final String PROMPT_NAME = "it-prompt-" + RUN_ID;
    private static final String QUEUE_NAME = "it-queue-" + RUN_ID;
    private static final String DATASET_NAME = "it-ds-" + RUN_ID;
    private static final String OTHER_DATASET_NAME = "it-ds-other-" + RUN_ID;
    private static final String TRACE_ID = UUID.randomUUID().toString().replace("-", "");
    private static final String OTHER_TRACE_ID = UUID.randomUUID().toString().replace("-", "");

    // Fixtures the destructive scenarios create and destroy themselves, under prefixes disjoint from
    // every shared name above.
    private static final String DELETE_PROMPT_PREFIX = "it-del-prompt-" + RUN_ID + "-";
    private static final String DELETE_DATASET_NAME = "it-del-ds-" + RUN_ID;

    private static String projectId;
    private static String queueId;
    private static String queueItemId;
    private static final List<String> datasetItemIds = new java.util.ArrayList<>();

    @Inject
    LangfuseApi client;

    @Inject
    LangfuseOperations langfuse;

    @Inject
    AsyncLangfuseOperations asyncLangfuse;

    // --- Scenario 1: seed the fixtures the later scenarios read -----------------------------

    @Test
    @Order(1)
    void seedTraces() {
        OtelTestHelper.ingestTrace(this.client, TRACE_ID, "expanded-ops-trace");
        OtelTestHelper.ingestTrace(this.client, OTHER_TRACE_ID, "expanded-ops-other-trace");

        projectId = this.client.projects().projectsGet().getData().get(0).getId();

        assertThat(projectId).isNotBlank();
    }

    // --- Scenario 2: prompts, whose lookup and delete are both server-side by name -----------

    @Test
    @Order(2)
    void promptCreateIfAbsentIsIdempotentAgainstARealServer() {
        var request = CreateTextPromptRequest.builder()
                .name(PROMPT_NAME)
                .prompt("Hello {{name}}")
                .type(CreateTextPromptType.TEXT)
                .labels(List.of("production"))
                .build();

        var first = this.langfuse.prompts().createIfAbsent(request);
        var second = this.langfuse.prompts().createIfAbsent(request);

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();

        // A second createIfAbsent must not add a version: Langfuse would have versioned a blind create.
        assertThat(promptMeta(PROMPT_NAME).getVersions()).hasSize(1);

        assertThat(this.langfuse.prompts().exists(PROMPT_NAME)).isTrue();
        assertThat(this.langfuse.prompts().findByName(PROMPT_NAME)).isPresent();
        assertThat(this.langfuse.prompts().exists("it-missing-prompt-" + RUN_ID)).isFalse();
        assertThat(this.langfuse.prompts().findByName("it-missing-prompt-" + RUN_ID)).isEmpty();

        assertThat(await(this.asyncLangfuse.prompts().exists(PROMPT_NAME))).isTrue();
        assertThat(await(this.asyncLangfuse.prompts().findByName("it-missing-prompt-" + RUN_ID))).isNull();
    }

    @Test
    @Order(3)
    void deletingAPromptRemovesEveryVersionOfIt() {
        // Its own prefix: this is the one delete in the layer whose scope is the whole name, and it must
        // not be able to reach PROMPT_NAME, which @Order(2) and the listing assertions depend on.
        var name = DELETE_PROMPT_PREFIX + "all-versions";

        // Both versions are TEXT: Langfuse rejects a chat version on a name whose earlier versions are
        // text ("Previous versions have different prompt type"), so a mixed pair cannot be seeded.
        createTextPromptVersion(name, "version one");
        createTextPromptVersion(name, "version two");

        assertThat(promptMeta(name).getVersions())
                .as("both versions must exist before the delete, or the scope assertion is vacuous")
                .hasSize(2);

        var result = this.langfuse.prompts().deleteByName(name);

        assertThat(result.hasFailures()).isFalse();
        assertThat(result.outcome(name)).get().isInstanceOf(DeletionOutcome.Deleted.class);

        // Neither version survives, and the name disappears from the listing entirely.
        assertThat(this.langfuse.prompts().findByName(name)).isEmpty();
        assertThat(this.langfuse.prompts().stream(PageSelection.all()).map(PromptMeta::getName))
                .doesNotContain(name)
                .contains(PROMPT_NAME);
    }

    @Test
    @Order(4)
    void deletingAnAbsentPromptNameIsNotAnError() {
        // Prompts key their delete on the name server-side, so absence arrives as a 404 from the delete
        // itself rather than from a client-side scan finding nothing.
        var absent = "it-absent-prompt-" + UUID.randomUUID();

        assertThat(this.langfuse.prompts().deleteByName(absent).outcome(absent))
                .get()
                .isInstanceOf(DeletionOutcome.NotFound.class);

        assertThat(await(this.asyncLangfuse.prompts().deleteByName(absent)).outcome(absent))
                .get()
                .isInstanceOf(DeletionOutcome.NotFound.class);
    }

    // --- Scenario 3: annotation queues and their parent-scoped items -------------------------

    @Test
    @Order(5)
    void annotationQueueLookupAndItemLifecycle() {
        var scoreConfig = this.langfuse.scoreConfigs().createIfAbsent(CreateScoreConfigRequest.builder()
                .name("it-queue-cfg-" + RUN_ID)
                .dataType(ScoreConfigDataType.NUMERIC)
                .minValue(0.0)
                .maxValue(1.0)
                .build());

        var queue = this.langfuse.annotationQueues().createIfAbsent(CreateAnnotationQueueRequest.builder()
                .name(QUEUE_NAME)
                .description("Expanded operations IT queue")
                .scoreConfigIds(List.of(scoreConfig.getId()))
                .build());

        queueId = queue.getId();

        assertThat(queue.getName()).isEqualTo(QUEUE_NAME);

        // createIfAbsent resolves the name by scanning the listing, so a second call must return the
        // same queue rather than a duplicate.
        assertThat(this.langfuse.annotationQueues().createIfAbsent(CreateAnnotationQueueRequest.builder()
                .name(QUEUE_NAME)
                .scoreConfigIds(List.of(scoreConfig.getId()))
                .build()).getId())
                .isEqualTo(queueId);

        assertThat(this.langfuse.annotationQueues().findById(queueId))
                .isPresent()
                .get()
                .satisfies(found -> assertThat(found.getName()).isEqualTo(QUEUE_NAME));
        assertThat(this.langfuse.annotationQueues().findByName(QUEUE_NAME))
                .isPresent()
                .get()
                .satisfies(found -> assertThat(found.getId()).isEqualTo(queueId));
        assertThat(this.langfuse.annotationQueues().findById("it-absent-" + UUID.randomUUID())).isEmpty();

        assertThat(await(this.asyncLangfuse.annotationQueues().findById(queueId)))
                .isNotNull()
                .satisfies(found -> assertThat(found.getId()).isEqualTo(queueId));
    }

    @Test
    @Order(6)
    void annotationQueueItemsAreCreatedUpdatedAndDeletedThroughTheParentScope() {
        var items = this.langfuse.annotationQueues().items(queueId);

        var created = awaitQueueItem(queueId);
        queueItemId = created.getId();

        assertThat(created.getQueueId()).isEqualTo(queueId);

        assertThat(items.findById(queueItemId))
                .isPresent()
                .get()
                .satisfies(found -> assertThat(found.getObjectId()).isEqualTo(TRACE_ID));

        assertThat(items.update(queueItemId, UpdateAnnotationQueueItemRequest.builder()
                .status(AnnotationQueueStatus.COMPLETED)
                .build()))
                .satisfies(updated -> assertThat(updated.getStatus()).isEqualTo(AnnotationQueueStatus.COMPLETED));

        // The parent scope is genuinely a scope: the item belongs to this queue's listing.
        assertThat(items.stream(PageSelection.all()).map(item -> item.getId()))
                .contains(queueItemId);

        var absent = "it-absent-item-" + UUID.randomUUID();
        var result = items.deleteById(queueItemId, absent);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.hasFailures()).isFalse();
        assertThat(result.deleted()).containsExactly(queueItemId);
        assertThat(result.notFound()).containsExactly(absent);
        assertThat(items.findById(queueItemId)).isEmpty();
    }

    // --- Scenario 4: filters genuinely narrowing a real result set ---------------------------

    @Test
    @Order(7)
    void datasetItemFilterNarrowsTheRealResultSet() {
        this.langfuse.datasets().createIfAbsent(CreateDatasetRequest.builder().name(DATASET_NAME).build());
        this.langfuse.datasets().createIfAbsent(CreateDatasetRequest.builder().name(OTHER_DATASET_NAME).build());

        IntStream.rangeClosed(1, 3)
                .mapToObj(i -> CreateDatasetItemRequest.builder()
                        .datasetName(DATASET_NAME)
                        .input(Map.of("question", "q" + i))
                        .build())
                .map(request -> this.langfuse.datasetItems().create(request).getId())
                .forEach(datasetItemIds::add);

        var otherId = this.langfuse.datasetItems().create(CreateDatasetItemRequest.builder()
                .datasetName(OTHER_DATASET_NAME)
                .input(Map.of("question", "other"))
                .build())
                .getId();

        var filtered = this.langfuse.datasetItems()
                .matching(DatasetItemFilter.builder().datasetName(DATASET_NAME).build())
                .stream(PageSelection.all())
                .map(DatasetItem::getId)
                .toList();

        // The filter must have an effect server-side: the other dataset's item is excluded even though
        // it is visible in the unfiltered collection.
        assertThat(filtered)
                .containsExactlyInAnyOrderElementsOf(datasetItemIds)
                .doesNotContain(otherId);

        var unfiltered = this.langfuse.datasetItems().stream(PageSelection.all())
                .map(DatasetItem::getId)
                .toList();

        assertThat(unfiltered)
                .hasSizeGreaterThan(filtered.size())
                .containsAll(filtered)
                .contains(otherId);

        assertThat(this.langfuse.datasetItems().findById(datasetItemIds.get(0)))
                .isPresent()
                .get()
                .satisfies(item -> assertThat(item.getDatasetName()).isEqualTo(DATASET_NAME));
        assertThat(this.langfuse.datasetItems().findById("it-absent-" + UUID.randomUUID())).isEmpty();

        var asyncFiltered = await(this.asyncLangfuse.datasetItems()
                .matching(DatasetItemFilter.builder().datasetName(DATASET_NAME).build())
                .find(PageSelection.all()))
                .stream()
                .map(DatasetItem::getId)
                .toList();

        assertThat(asyncFiltered).containsExactlyElementsOf(filtered);
    }

    @Test
    @Order(8)
    void commentFilterNarrowsTheRealResultSet() {
        var onTrace = awaitComment(TRACE_ID, "comment on the first trace");
        var onOtherTrace = awaitComment(OTHER_TRACE_ID, "comment on the second trace");

        assertThat(onTrace).isNotBlank().isNotEqualTo(onOtherTrace);

        var filtered = this.langfuse.comments()
                .matching(CommentFilter.builder()
                        .objectType(CommentObjectType.TRACE)
                        .objectId(TRACE_ID)
                        .build())
                .stream(PageSelection.all())
                .map(comment -> comment.getId())
                .toList();

        assertThat(filtered)
                .containsExactly(onTrace)
                .doesNotContain(onOtherTrace);

        var unfiltered = this.langfuse.comments().stream(PageSelection.all())
                .map(comment -> comment.getId())
                .toList();

        assertThat(unfiltered)
                .hasSizeGreaterThan(filtered.size())
                .contains(onTrace, onOtherTrace);

        // create returns the id alone, so findById is the only way to see the content back.
        assertThat(this.langfuse.comments().findById(onTrace))
                .isPresent()
                .get()
                .satisfies(comment -> assertThat(comment.getContent()).isEqualTo("comment on the first trace"));
        assertThat(this.langfuse.comments().findById("it-absent-" + UUID.randomUUID())).isEmpty();
    }

    // --- Scenario 5: the required-bound gateway on experiments -------------------------------

    @Test
    @Order(9)
    void theTimeWindowGatewayMakesTheRequiredBoundSatisfiable() {
        // These are the calls the gateway exists to make reachable. They cost nothing to assert beyond
        // "did not throw", which is the point: without the bound they would be a 400.
        assertThat(this.langfuse.experiments().since(WINDOW_START).findAll()).isNotNull();
        assertThat(this.langfuse.experiments().between(WINDOW_START, OffsetDateTime.now()).findAll()).isNotNull();
        assertThat(this.langfuse.experimentItems().since(WINDOW_START).findAll()).isNotNull();
        assertThat(this.langfuse.experimentItems().between(WINDOW_START, OffsetDateTime.now()).findAll()).isNotNull();

        assertThat(await(this.asyncLangfuse.experiments().since(WINDOW_START).findAll())).isNotNull();
        assertThat(await(this.asyncLangfuse.experimentItems().since(WINDOW_START).findAll())).isNotNull();
    }

    @Test
    @Order(10)
    void anUnboundedExperimentListingIsRejectedByTheRealServer() {
        // The evidence the gateway is not merely decorative. Reached through the generated client
        // directly, because the operations layer has deliberately made this call unexpressible - and a
        // WireMock stub would have served it happily.
        assertThatThrownBy(() -> this.client.experiments().experimentsList(
                APIExperimentsListRequest.newBuilder().build()))
                .isInstanceOf(LangfuseApiException.class)
                .satisfies(e -> assertThat(((LangfuseApiException) e).getStatusCode()).isEqualTo(400));

        assertThatThrownBy(() -> this.client.experiments().experimentsListItems(
                APIExperimentsListItemsRequest.newBuilder().build()))
                .isInstanceOf(LangfuseApiException.class)
                .satisfies(e -> assertThat(((LangfuseApiException) e).getStatusCode()).isEqualTo(400));
    }

    // --- Scenario 6: dataset item delete outcomes -------------------------------------------

    @Test
    @Order(11)
    void datasetItemDeleteReportsDeletedAndNotFoundInOneBatch() {
        // Own dataset, own items: @Order(7) still asserts on datasetItemIds.
        this.langfuse.datasets().createIfAbsent(CreateDatasetRequest.builder().name(DELETE_DATASET_NAME).build());

        var present = this.langfuse.datasetItems().create(CreateDatasetItemRequest.builder()
                .datasetName(DELETE_DATASET_NAME)
                .input(Map.of("question", "delete me"))
                .build())
                .getId();
        var absent = "it-absent-" + UUID.randomUUID();

        var result = this.langfuse.datasetItems().deleteById(present, absent);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.hasFailures()).isFalse();
        assertThat(result.deleted()).containsExactly(present);
        assertThat(result.notFound()).containsExactly(absent);
        assertThat(result.outcome(present)).get().isInstanceOf(DeletionOutcome.Deleted.class);
        assertThat(this.langfuse.datasetItems().findById(present)).isEmpty();

        // The shared fixtures are untouched by this scenario.
        assertThat(this.langfuse.datasetItems()
                .matching(DatasetItemFilter.builder().datasetName(DATASET_NAME).build())
                .stream(PageSelection.all())
                .map(DatasetItem::getId))
                .containsExactlyInAnyOrderElementsOf(datasetItemIds);
    }

    @Test
    @Order(12)
    void asyncDatasetItemDeleteAgreesWithTheSyncTree() {
        var present = this.langfuse.datasetItems().create(CreateDatasetItemRequest.builder()
                .datasetName(DELETE_DATASET_NAME)
                .input(Map.of("question", "delete me too"))
                .build())
                .getId();

        var result = await(this.asyncLangfuse.datasetItems().deleteById(present));

        assertThat(result.outcome(present)).get().isInstanceOf(DeletionOutcome.Deleted.class);
        assertThat(await(this.asyncLangfuse.datasetItems().findById(present))).isNull();
    }

    private com.langfuse.api.model.AnnotationQueueItem awaitQueueItem(String queue) {
        // The item references a trace, and OTel ingestion is eventually consistent, so creation is
        // retried rather than asserted read-after-write.
        var created = new java.util.concurrent.atomic.AtomicReference<com.langfuse.api.model.AnnotationQueueItem>();

        // Awaitility is qualified rather than statically imported: this class also carries the module's
        // await(Uni) helper, and the two names collide.
        Awaitility.await().atMost(INGESTION_TIMEOUT)
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptionsMatching(LangfuseApiException.class::isInstance)
                .untilAsserted(() -> {
                    created.set(this.langfuse.annotationQueues().items(queue)
                            .create(CreateAnnotationQueueItemRequest.builder()
                                    .objectId(TRACE_ID)
                                    .objectType(AnnotationQueueObjectType.TRACE)
                                    .build()));

                    assertThat(created.get().getId()).isNotBlank();
                });

        return created.get();
    }

    private String awaitComment(String traceId, String content) {
        var id = new java.util.concurrent.atomic.AtomicReference<String>();

        Awaitility.await().atMost(INGESTION_TIMEOUT)
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptionsMatching(LangfuseApiException.class::isInstance)
                .untilAsserted(() -> {
                    id.set(this.langfuse.comments().create(CreateCommentRequest.builder()
                            .projectId(projectId)
                            .objectType(CommentObjectType.TRACE.getValue())
                            .objectId(traceId)
                            .content(content)
                            .build()));

                    assertThat(id.get()).isNotBlank();
                });

        return id.get();
    }

    private PromptMeta promptMeta(String name) {
        return this.langfuse.prompts().stream(PageSelection.all())
                .filter(meta -> name.equals(meta.getName()))
                .findFirst()
                .orElseThrow();
    }

    // Prompt versions are created through the generated client: createIfAbsent deliberately refuses to
    // add one, which is precisely what @Order(2) asserts.
    private void createTextPromptVersion(String name, String body) {
        this.client.prompts().promptsCreate(APIPromptsCreateRequest.newBuilder()
                .createPromptRequest(new CreatePromptRequest(CreateTextPromptRequest.builder()
                        .name(name)
                        .prompt(body)
                        .type(CreateTextPromptType.TEXT)
                        .build()))
                .build());
    }

    private static <T> T await(Uni<T> uni) {
        return uni.await().atMost(TIMEOUT);
    }
}
