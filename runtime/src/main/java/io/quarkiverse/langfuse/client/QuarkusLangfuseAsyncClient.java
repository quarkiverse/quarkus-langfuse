package io.quarkiverse.langfuse.client;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.langfuse.api.LangfuseAsyncApis;

import io.quarkiverse.langfuse.client.jaxrs.QuarkusAnnotationQueuesAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusBlobStorageIntegrationsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusCommentsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusDatasetItemsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusDatasetRunItemsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusDatasetsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusHealthAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusIngestionAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusLegacyMetricsV1AsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusLegacyObservationsV1AsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusLegacyScoreV1AsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusLlmConnectionsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusMediaAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusMetricsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusModelsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusObservationsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusOpentelemetryAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusOrganizationsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusProjectsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusPromptVersionAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusPromptsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusScimAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusScoreConfigsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusScoresAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusSessionsAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusTraceAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusUnstableEvaluationRulesAsyncApi;
import io.quarkiverse.langfuse.client.jaxrs.QuarkusUnstableEvaluatorsAsyncApi;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.rest.client.reactive.ClientBasicAuth;

@Path("/")
@ClientBasicAuth(username = "${" + LangfuseConfig.PUBLIC_KEY + "}", password = "${" + LangfuseConfig.SECRET_KEY + "}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface QuarkusLangfuseAsyncClient extends LangfuseAsyncApis,
        QuarkusAnnotationQueuesAsyncApi,
        QuarkusBlobStorageIntegrationsAsyncApi,
        QuarkusCommentsAsyncApi,
        QuarkusDatasetItemsAsyncApi,
        QuarkusDatasetRunItemsAsyncApi,
        QuarkusDatasetsAsyncApi,
        QuarkusHealthAsyncApi,
        QuarkusIngestionAsyncApi,
        QuarkusLegacyMetricsV1AsyncApi,
        QuarkusLegacyObservationsV1AsyncApi,
        QuarkusLegacyScoreV1AsyncApi,
        QuarkusLlmConnectionsAsyncApi,
        QuarkusMediaAsyncApi,
        QuarkusMetricsAsyncApi,
        QuarkusModelsAsyncApi,
        QuarkusObservationsAsyncApi,
        QuarkusOpentelemetryAsyncApi,
        QuarkusOrganizationsAsyncApi,
        QuarkusProjectsAsyncApi,
        QuarkusPromptVersionAsyncApi,
        QuarkusPromptsAsyncApi,
        QuarkusScimAsyncApi,
        QuarkusScoreConfigsAsyncApi,
        QuarkusScoresAsyncApi,
        QuarkusSessionsAsyncApi,
        QuarkusTraceAsyncApi,
        QuarkusUnstableEvaluationRulesAsyncApi,
        QuarkusUnstableEvaluatorsAsyncApi {

}
