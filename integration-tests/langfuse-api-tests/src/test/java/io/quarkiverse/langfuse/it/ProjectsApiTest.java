package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.langfuse.api.LangfuseApi;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Integration tests for the Projects API.
 *
 */
@QuarkusTest
class ProjectsApiTest {

    @Inject
    LangfuseApi client;

    @Test
    void getProjects() {
        assertThat(client.projects().projectsGet())
                .satisfies(projects -> {
                    assertThat(projects.getData())
                            .isNotEmpty()
                            .first()
                            .satisfies(project -> {
                                assertThat(project.getId()).isNotBlank();
                                assertThat(project.getName()).isNotBlank();
                                assertThat(project.getOrganization()).isNotNull();
                            });
                });
    }
}
