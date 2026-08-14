package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.feedback.FeedbackApi.APIFeedbackSubmitRequest;
import com.langfuse.api.model.FeedbackTargetType;
import com.langfuse.api.model.SubmitFeedbackRequest;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class FeedbackApiTest {

    @Inject
    LangfuseApi client;

    @Test
    void submitFeedback() {
        assertThatThrownBy(() -> client.feedback().feedbackSubmit(
                APIFeedbackSubmitRequest.newBuilder()
                        .submitFeedbackRequest(SubmitFeedbackRequest.builder()
                                .targetType(FeedbackTargetType.PUBLIC_API)
                                .target("scores-create")
                                .feedback("Integration test feedback")
                                .build())
                        .build()))
                .isInstanceOf(RuntimeException.class);
    }
}
