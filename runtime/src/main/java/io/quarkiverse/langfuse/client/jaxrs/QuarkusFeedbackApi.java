package io.quarkiverse.langfuse.client.jaxrs;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.model.SubmitFeedbackRequest;
import com.langfuse.api.model.SubmitFeedbackResponse;

/**
 * Langfuse Feedback API
 */
public interface QuarkusFeedbackApi extends com.langfuse.api.feedback.FeedbackApi {

    /**
     * Submit feedback.
     */
    @POST
    @Path("/api/public/feedback")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SubmitFeedbackResponse feedbackSubmit(
            SubmitFeedbackRequest submitFeedbackRequest);

    /**
     * Submit feedback.
     */
    @Override
    default SubmitFeedbackResponse feedbackSubmit(APIFeedbackSubmitRequest apiRequest) {
        return feedbackSubmit(apiRequest.submitFeedbackRequest());
    }

}
