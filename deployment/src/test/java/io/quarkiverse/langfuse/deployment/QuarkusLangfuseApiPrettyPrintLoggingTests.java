package io.quarkiverse.langfuse.deployment;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.model.CreateScoreRequest;
import com.langfuse.api.model.CreateScoreValue;
import com.langfuse.api.scores.ScoresApi.APIScoresCreateRequest;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.QuarkusExtensionTest;

class QuarkusLangfuseApiPrettyPrintLoggingTests extends RequestResponseLoggingTests {
    private static final int REQUEST_BODY_PARAMETER = 3;

    @RegisterExtension
    static final QuarkusExtensionTest extensionTest = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class))
            .overrideConfigKey("quarkus.langfuse.devservices.enabled", "false")
            .overrideRuntimeConfigKey(LangfuseConfig.BASE_URL_KEY, wiremockUrlForConfig())
            .overrideRuntimeConfigKey("quarkus.langfuse.log-requests", "true")
            .overrideRuntimeConfigKey("quarkus.langfuse.log-responses", "true")
            .overrideRuntimeConfigKey("quarkus.langfuse.pretty-print", "true")
            .overrideRuntimeConfigKey("quarkus.langfuse.public-key", "quarkus")
            .overrideRuntimeConfigKey("quarkus.langfuse.secret-key", "quarkus");

    @Inject
    LangfuseApi langfuseApi;

    @Test
    void jsonArrayRequestBodyIsPrettyPrintedWithoutWarning() {
        wiremock().register(
                post(urlPathEqualTo("/api/public/scores"))
                        .willReturn(okJson("{\"accepted\":1,\"rejected\":0,\"errors\":[]}")));

        var request = CreateScoreRequest.builder()
                .traceId("trace-1")
                .name("accuracy")
                .value(new CreateScoreValue(0.75d))
                .build();

        langfuseApi.scores().scoresCreate(APIScoresCreateRequest.newBuilder()
                .createScoreRequest(List.of(request))
                .build());

        assertThat(LOG_HANDLER.getRecords())
                .hasSize(2)
                .extracting(LogRecord::getLevel)
                .containsOnly(Level.INFO);

        assertThat(LOG_HANDLER.getRecords().get(0).getParameters()[REQUEST_BODY_PARAMETER].toString())
                .startsWith("[")
                .contains("\n")
                .contains("\"traceId\" : \"trace-1\"");
    }
}
