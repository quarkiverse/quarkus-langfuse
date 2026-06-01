package io.quarkiverse.langfuse.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.LangfuseAsyncApis;

import io.quarkiverse.langfuse.client.jaxrs.AnnotationQueuesAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.BlobStorageIntegrationsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.CommentsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.DatasetItemsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.DatasetRunItemsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.DatasetsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.HealthAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.IngestionAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.LangfuseExceptionMapper;
import io.quarkiverse.langfuse.client.jaxrs.LegacyMetricsV1AsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.LegacyObservationsV1AsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.LegacyScoreV1AsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.LlmConnectionsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.MediaAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.MetricsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.ModelsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.ObservationsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.OpentelemetryAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.OrganizationsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.ProjectsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.PromptVersionAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.PromptsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.ScimAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.ScoreConfigsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.ScoresAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.SessionsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.TraceAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.UnstableEvaluationRulesAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.UnstableEvaluatorsAsyncApi;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.rest.client.reactive.ClientBasicAuth;

@Path("/")
@ClientBasicAuth(username = "${" + LangfuseConfig.USERNAME_KEY + "}", password = "${" + LangfuseConfig.PASSWORD_KEY + "}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface QuarkusLangfuseAsyncClient extends LangfuseAsyncApis,
        LangfuseExceptionMapper,
        AnnotationQueuesAsyncApi,
        BlobStorageIntegrationsAsyncApi,
        CommentsAsyncApi,
        DatasetItemsAsyncApi,
        DatasetRunItemsAsyncApi,
        DatasetsAsyncApi,
        HealthAsyncApi,
        IngestionAsyncApi,
        LegacyMetricsV1AsyncApi,
        LegacyObservationsV1AsyncApi,
        LegacyScoreV1AsyncApi,
        LlmConnectionsAsyncApi,
        MediaAsyncApi,
        MetricsAsyncApi,
        ModelsAsyncApi,
        ObservationsAsyncApi,
        OpentelemetryAsyncApi,
        OrganizationsAsyncApi,
        ProjectsAsyncApi,
        PromptVersionAsyncApi,
        PromptsAsyncApi,
        ScimAsyncApi,
        ScoreConfigsAsyncApi,
        ScoresAsyncApi,
        SessionsAsyncApi,
        TraceAsyncApi,
        UnstableEvaluationRulesAsyncApi,
        UnstableEvaluatorsAsyncApi {

}
