package io.quarkiverse.langfuse.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.LangfuseApis;

import io.quarkiverse.langfuse.client.jaxrs.QuarkusAnnotationQueuesApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusBlobStorageIntegrationsApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusCommentsApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusDatasetItemsApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusDatasetRunItemsApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusDatasetsApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusHealthApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusIngestionApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusLegacyMetricsV1Api;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusLegacyObservationsV1Api;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusLegacyScoreV1Api;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusLlmConnectionsApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusMediaApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusMetricsApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusModelsApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusObservationsApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusOpentelemetryApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusOrganizationsApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusProjectsApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusPromptVersionApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusPromptsApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusScimApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusScoreConfigsApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusScoresApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusSessionsApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusTraceApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusUnstableEvaluationRulesApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusUnstableEvaluatorsApi;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.rest.client.reactive.ClientBasicAuth;

@Path("/")
@ClientBasicAuth(username = "${" + LangfuseConfig.PUBLIC_KEY + "}", password = "${" + LangfuseConfig.SECRET_KEY + "}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface QuarkusLangfuseClient extends LangfuseApis,
        QuarkusAnnotationQueuesApi,
        QuarkusBlobStorageIntegrationsApi,
        QuarkusCommentsApi,
        QuarkusDatasetItemsApi,
        QuarkusDatasetRunItemsApi,
        QuarkusDatasetsApi,
        QuarkusHealthApi,
        QuarkusIngestionApi,
        QuarkusLegacyMetricsV1Api,
        QuarkusLegacyObservationsV1Api,
        QuarkusLegacyScoreV1Api,
        QuarkusLlmConnectionsApi,
        QuarkusMediaApi,
        QuarkusMetricsApi,
        QuarkusModelsApi,
        QuarkusObservationsApi,
        QuarkusOpentelemetryApi,
        QuarkusOrganizationsApi,
        QuarkusProjectsApi,
        QuarkusPromptVersionApi,
        QuarkusPromptsApi,
        QuarkusScimApi,
        QuarkusScoreConfigsApi,
        QuarkusScoresApi,
        QuarkusSessionsApi,
        QuarkusTraceApi,
        QuarkusUnstableEvaluationRulesApi,
        QuarkusUnstableEvaluatorsApi {

}
