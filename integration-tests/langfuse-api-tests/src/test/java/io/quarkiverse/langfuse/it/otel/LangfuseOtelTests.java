package io.quarkiverse.langfuse.it.otel;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.model.ObservationV2;
import com.langfuse.api.model.ObservationsV2Response;
import com.langfuse.api.observations.ObservationsApi.APIObservationsGetManyRequest;

import io.quarkiverse.langfuse.rest.ChatResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class LangfuseOtelTests {
    @Inject
    LangfuseApi langfuseApi;

    @TestHTTPEndpoint(ChatResource.class)
    @TestHTTPResource
    URL chatResourceUrl;

    @Test
    void otelTracesPropagatedToLangfuse() throws InterruptedException {
        // Wait for any previous test spans to flush before capturing the baseline timestamp
        TimeUnit.SECONDS.sleep(5);

        var beforeRequest = OffsetDateTime.now().minusSeconds(1);

        // Invoke the chat endpoint which triggers LangChain4j OTel span generation
        var response = given()
                .queryParam("message", "Help me!")
                .get(this.chatResourceUrl)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .contentType(ContentType.TEXT)
                .extract().asString();

        assertThat(response)
                .isNotBlank();

        // Poll the v2 observations API until the GENERATION span appears — uses fromStartTime to isolate this test's data
        await()
                .atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofSeconds(1))
                .pollDelay(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(getObservationsSince(beforeRequest))
                        .anySatisfy(observation -> {
                            assertThat(observation.getType()).isEqualToIgnoringCase("GENERATION");
                            assertThat(observation.getInput()).isNotNull();
                        }));
    }

    private List<ObservationV2> getObservationsSince(OffsetDateTime fromStartTime) {
        var request = APIObservationsGetManyRequest.newBuilder()
                .fromStartTime(fromStartTime)
                .fields("core,basic,io")
                .build();

        return Optional.ofNullable(this.langfuseApi.observations().observationsGetMany(request))
                .map(ObservationsV2Response::getData)
                .orElseGet(List::of);
    }
}
