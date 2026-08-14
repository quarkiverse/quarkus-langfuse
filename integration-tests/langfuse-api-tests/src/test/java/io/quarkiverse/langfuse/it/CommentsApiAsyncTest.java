package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.LangfuseApiException;
import com.langfuse.api.comments.CommentsApi.APICommentsCreateRequest;
import com.langfuse.api.comments.CommentsApi.APICommentsGetByIdRequest;
import com.langfuse.api.comments.CommentsApi.APICommentsGetRequest;
import com.langfuse.api.model.Comment;
import com.langfuse.api.model.CreateCommentRequest;

import io.quarkus.test.junit.QuarkusTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class CommentsApiAsyncTest {

    private static final String TRACE_ID = UUID.randomUUID().toString().replace("-", "");
    private static String commentId;

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void ingestTrace() {
        // Ingest via OTel — legacy ingestion rejects trace-create in v4 events_only mode
        OtelTestHelper.ingestTrace(client, TRACE_ID, "async-comments-test-trace");
    }

    @Test
    @Order(2)
    void createComment() {
        // Poll until the trace is available for comment attachment — OTel ingestion is eventually consistent
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptionsMatching(LangfuseApiException.class::isInstance)
                .untilAsserted(() -> {
                    var projectId = client.projects().projectsGet().getData().get(0).getId();

                    assertThat(client.asyncComments().commentsCreate(
                            APICommentsCreateRequest.newBuilder()
                                    .createCommentRequest(CreateCommentRequest.builder()
                                            .projectId(projectId)
                                            .objectType("TRACE")
                                            .objectId(TRACE_ID)
                                            .content("Async test comment")
                                            .build())
                                    .build()))
                            .succeedsWithin(Duration.ofSeconds(5))
                            .satisfies(response -> {
                                assertThat(response.getId()).isNotBlank();
                                commentId = response.getId();
                            });
                });
    }

    @Test
    @Order(3)
    void getCommentById() {
        assertThat(client.asyncComments().commentsGetById(
                APICommentsGetByIdRequest.newBuilder()
                        .commentId(commentId)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .extracting(Comment::getContent, Comment::getObjectId)
                .containsExactly("Async test comment", TRACE_ID);
    }

    @Test
    @Order(3)
    void listComments() {
        assertThat(client.asyncComments().commentsGet(
                APICommentsGetRequest.newBuilder()
                        .objectType("TRACE")
                        .objectId(TRACE_ID)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(comments -> assertThat(comments.getData())
                        .isNotEmpty()
                        .anyMatch(c -> commentId.equals(c.getId())));
    }
}
