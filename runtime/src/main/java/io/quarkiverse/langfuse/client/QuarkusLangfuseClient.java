package io.quarkiverse.langfuse.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.LangfuseApis;

import io.quarkiverse.langfuse.client.jaxrs.AnnotationQueuesApi;
import io.quarkiverse.langfuse.client.jaxrs.BlobStorageIntegrationsApi;
import io.quarkiverse.langfuse.client.jaxrs.CommentsApi;
import io.quarkiverse.langfuse.client.jaxrs.DatasetItemsApi;
import io.quarkiverse.langfuse.client.jaxrs.DatasetRunItemsApi;
import io.quarkiverse.langfuse.client.jaxrs.DatasetsApi;
import io.quarkiverse.langfuse.client.jaxrs.HealthApi;
import io.quarkiverse.langfuse.client.jaxrs.IngestionApi;
import io.quarkiverse.langfuse.client.jaxrs.LangfuseExceptionMapper;
import io.quarkiverse.langfuse.client.jaxrs.LegacyMetricsV1Api;
import io.quarkiverse.langfuse.client.jaxrs.LegacyObservationsV1Api;
import io.quarkiverse.langfuse.client.jaxrs.LegacyScoreV1Api;
import io.quarkiverse.langfuse.client.jaxrs.LlmConnectionsApi;
import io.quarkiverse.langfuse.client.jaxrs.MediaApi;
import io.quarkiverse.langfuse.client.jaxrs.MetricsApi;
import io.quarkiverse.langfuse.client.jaxrs.ModelsApi;
import io.quarkiverse.langfuse.client.jaxrs.ObservationsApi;
import io.quarkiverse.langfuse.client.jaxrs.OpentelemetryApi;
import io.quarkiverse.langfuse.client.jaxrs.OrganizationsApi;
import io.quarkiverse.langfuse.client.jaxrs.ProjectsApi;
import io.quarkiverse.langfuse.client.jaxrs.PromptVersionApi;
import io.quarkiverse.langfuse.client.jaxrs.PromptsApi;
import io.quarkiverse.langfuse.client.jaxrs.ScimApi;
import io.quarkiverse.langfuse.client.jaxrs.ScoreConfigsApi;
import io.quarkiverse.langfuse.client.jaxrs.ScoresApi;
import io.quarkiverse.langfuse.client.jaxrs.SessionsApi;
import io.quarkiverse.langfuse.client.jaxrs.TraceApi;
import io.quarkiverse.langfuse.client.jaxrs.UnstableEvaluationRulesApi;
import io.quarkiverse.langfuse.client.jaxrs.UnstableEvaluatorsApi;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.rest.client.reactive.ClientBasicAuth;

@Path("/")
@ClientBasicAuth(username = "${" + LangfuseConfig.USERNAME_KEY + "}", password = "${" + LangfuseConfig.PASSWORD_KEY + "}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface QuarkusLangfuseClient extends LangfuseExceptionMapper, LangfuseApis,
        AnnotationQueuesApi,
        BlobStorageIntegrationsApi,
        CommentsApi,
        DatasetItemsApi,
        DatasetRunItemsApi,
        DatasetsApi,
        HealthApi,
        IngestionApi,
        LegacyMetricsV1Api,
        LegacyObservationsV1Api,
        LegacyScoreV1Api,
        LlmConnectionsApi,
        MediaApi,
        MetricsApi,
        ModelsApi,
        ObservationsApi,
        OpentelemetryApi,
        OrganizationsApi,
        ProjectsApi,
        PromptVersionApi,
        PromptsApi,
        ScimApi,
        ScoreConfigsApi,
        ScoresApi,
        SessionsApi,
        TraceApi,
        UnstableEvaluationRulesApi,
        UnstableEvaluatorsApi {

}
