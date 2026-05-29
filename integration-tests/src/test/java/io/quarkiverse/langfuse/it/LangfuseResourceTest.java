package io.quarkiverse.langfuse.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class LangfuseResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/langfuse")
                .then()
                .statusCode(200)
                .body(is("Hello langfuse"));
    }
}
