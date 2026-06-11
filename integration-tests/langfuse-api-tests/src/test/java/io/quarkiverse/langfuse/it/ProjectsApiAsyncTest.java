package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApi;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Async integration tests for the Projects API.
 *
 */
@QuarkusTest
class ProjectsApiAsyncTest {

    @Inject
    LangfuseApi client;

    @Test
    void getProjects() {
        assertThat(client.asyncProjects().projectsGet())
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(projects -> assertThat(projects.getData()).isNotEmpty());
    }
}
