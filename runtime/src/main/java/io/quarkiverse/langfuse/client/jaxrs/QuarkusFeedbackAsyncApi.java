package io.quarkiverse.langfuse.client.jaxrs;

import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.feedback.FeedbackApi.APIFeedbackSubmitRequest;
import com.langfuse.api.model.SubmitFeedbackRequest;
import com.langfuse.api.model.SubmitFeedbackResponse;

public interface QuarkusFeedbackAsyncApi extends com.langfuse.api.feedback.async.FeedbackApi {

    /**
     * Submit feedback.
     */
    @POST
    @Path("/api/public/feedback")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    CompletionStage<SubmitFeedbackResponse> feedbackSubmit(
            SubmitFeedbackRequest submitFeedbackRequest);

    /**
     * Submit feedback.
     */
    @Override
    default CompletionStage<SubmitFeedbackResponse> feedbackSubmit(APIFeedbackSubmitRequest apiRequest) {
        return feedbackSubmit(apiRequest.submitFeedbackRequest());
    }

}
