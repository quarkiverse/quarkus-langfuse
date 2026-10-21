package io.quarkiverse.langfuse.it;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;

import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;

/**
 * Verifies that the running application, packaged as a JVM jar or as a native executable, ships a span to the Langfuse
 * OTLP ingestion endpoint. This exercises the exporter factory that the build selected for the Quarkus version in use,
 * including its reflective instantiation in native mode. A factory built against the wrong OTel package would fail to
 * load, so a successful export shows that the right one was chosen.
 */
@QuarkusIntegrationTest
@WithTestResource(LangfuseStubResource.class)
class LangfuseOtelExportIT {
    private static final String SPAN_NAME = "langfuse-it-export-chat";

    @InjectLangfuseStub
    WireMockServer langfuseStub;

    @Test
    void spanIsExportedToLangfuse() {
        given()
                .queryParam("name", SPAN_NAME)
                .when()
                .get("/otel-export")
                .then()
                .statusCode(200)
                .body(is("true"));

        assertThat(this.langfuseStub.findAll(
                postRequestedFor(urlPathEqualTo(LangfuseStubResource.TRACES_PATH))
                        .withHeader("x-langfuse-ingestion-version", equalTo("4"))
                        .withHeader("Content-Type", equalTo("application/x-protobuf"))))
                .singleElement()
                .extracting(request -> new String(request.getBody(), StandardCharsets.ISO_8859_1))
                .asString()
                .contains(SPAN_NAME);
    }
}
