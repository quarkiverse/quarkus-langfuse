package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.LangfuseApi;

import io.quarkus.test.junit.QuarkusTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class ProjectsApiAsyncTest {

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void getProjects() {
        assertThat(client.asyncProjects().projectsGet())
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(projects -> assertThat(projects.getData()).isNotEmpty());
    }

    @Test
    @Order(2)
    @Disabled("Requires org-admin role")
    void createProject() {
    }

    @Test
    @Order(3)
    @Disabled("Requires org-admin role")
    void updateProject() {
    }

    @Test
    @Order(4)
    @Disabled("Requires org-admin role")
    void createApiKey() {
    }

    @Test
    @Order(5)
    @Disabled("Requires org-admin role")
    void getApiKeys() {
    }

    @Test
    @Order(6)
    @Disabled("Requires org-admin role")
    void deleteApiKey() {
    }

    @Test
    @Order(7)
    @Disabled("Requires org-admin role")
    void deleteProject() {
    }
}
