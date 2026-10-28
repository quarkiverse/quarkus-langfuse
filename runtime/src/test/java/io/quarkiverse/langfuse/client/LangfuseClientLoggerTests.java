package io.quarkiverse.langfuse.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.InMemoryLogHandler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;

class LangfuseClientLoggerTests {
    private static final int REQUEST_BODY_PARAMETER = 3;

    private final InMemoryLogHandler logHandler = new InMemoryLogHandler(logRecord -> true);
    private final java.util.logging.Logger julLogger = LogManager.getLogManager()
            .getLogger(LangfuseClientLogger.class.getName());

    private HttpClientRequest request;

    @BeforeEach
    void setUp() {
        this.julLogger.addHandler(this.logHandler);
        this.request = mock(HttpClientRequest.class);
        when(this.request.getMethod()).thenReturn(HttpMethod.POST);
        when(this.request.absoluteURI()).thenReturn("http://localhost/api/public/scores");
        when(this.request.headers()).thenReturn(MultiMap.caseInsensitiveMultiMap());
    }

    @AfterEach
    void tearDown() {
        this.julLogger.removeHandler(this.logHandler);
    }

    @Test
    void jsonArrayBodyIsPrettyPrinted() {
        var body = logRequest("[{\"name\":\"accuracy\"}]", true);

        assertThat(body)
                .startsWith("[")
                .contains("\n")
                .contains("\"name\" : \"accuracy\"");
    }

    @Test
    void jsonObjectBodyIsPrettyPrinted() {
        var body = logRequest("{\"name\":\"accuracy\"}", true);

        assertThat(body)
                .startsWith("{")
                .contains("\n")
                .contains("\"name\" : \"accuracy\"");
    }

    @Test
    void nonJsonBodyFallsBackToRawStringWithoutWarning() {
        var body = logRequest("this is not json", true);

        assertThat(body).isEqualTo("this is not json");
    }

    @Test
    void bodyIsNotReformattedWhenPrettyPrintIsDisabled() {
        var body = logRequest("[{\"name\":\"accuracy\"}]", false);

        assertThat(body).isEqualTo("[{\"name\":\"accuracy\"}]");
    }

    private String logRequest(String payload, boolean prettyPrint) {
        var logger = new LangfuseClientLogger(true, false, prettyPrint);
        logger.setBodySize(1024);
        logger.logRequest(this.request, Buffer.buffer(payload), false);

        assertThat(this.logHandler.getRecords())
                .hasSize(1)
                .extracting(LogRecord::getLevel)
                .containsOnly(Level.INFO);

        return this.logHandler.getRecords().get(0).getParameters()[REQUEST_BODY_PARAMETER].toString();
    }
}
