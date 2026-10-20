package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.model.CreateSkillVersionRequest;
import com.langfuse.api.model.SkillVersionFileCreateInput;
import com.langfuse.api.model.SkillVersionFileInput;
import com.langfuse.api.model.UpdateSkillLabelsRequest;
import com.langfuse.api.skills.SkillsApi.APISkillsCreateVersionRequest;
import com.langfuse.api.skills.SkillsApi.APISkillsDeleteVersionRequest;
import com.langfuse.api.skills.SkillsApi.APISkillsGetRequest;
import com.langfuse.api.skills.SkillsApi.APISkillsListRequest;
import com.langfuse.api.skills.SkillsApi.APISkillsSetLabelsRequest;

import io.quarkus.test.junit.QuarkusTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class SkillsApiAsyncTest {

    private static final String SKILL_NAME = "it-async-skill-" + UUID.randomUUID();
    private static final String SKILL_MD = "---\nname: %s\ndescription: integration test skill\n---\n# Test skill\n"
            .formatted(SKILL_NAME);

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void createVersion() {
        assertThat(client.asyncSkills().skillsCreateVersion(
                APISkillsCreateVersionRequest.newBuilder()
                        .createSkillVersionRequest(CreateSkillVersionRequest.builder()
                                .files(List.of(new SkillVersionFileCreateInput(SkillVersionFileInput.builder()
                                        .path("SKILL.md")
                                        .content(SKILL_MD)
                                        .build())))
                                .commitMessage("integration test")
                                .build())
                        .build()))
                .succeedsWithin(Duration.ofSeconds(10))
                .satisfies(version -> {
                    assertThat(version.getName()).isEqualTo(SKILL_NAME);
                    assertThat(version.getVersion()).isEqualTo(1);
                    assertThat(version.getId()).isNotBlank();
                });
    }

    @Test
    @Order(2)
    void getSkillByVersion() {
        assertThat(client.asyncSkills().skillsGet(
                APISkillsGetRequest.newBuilder()
                        .skillName(SKILL_NAME)
                        .version(1)
                        .includeContent(true)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(10))
                .satisfies(version -> {
                    assertThat(version.getName()).isEqualTo(SKILL_NAME);
                    assertThat(version.getVersion()).isEqualTo(1);
                });
    }

    @Test
    @Order(3)
    void listSkillsContainsCreated() {
        assertThat(client.asyncSkills().skillsList(
                APISkillsListRequest.newBuilder()
                        .name(SKILL_NAME)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(10))
                .satisfies(list -> assertThat(list.getData())
                        .anyMatch(meta -> SKILL_NAME.equals(meta.getName())));
    }

    @Test
    @Order(4)
    void setLabelsOnVersion() {
        assertThat(client.asyncSkills().skillsSetLabels(
                APISkillsSetLabelsRequest.newBuilder()
                        .skillName(SKILL_NAME)
                        .skillVersion(1)
                        .updateSkillLabelsRequest(UpdateSkillLabelsRequest.builder()
                                .labels(List.of("integration-test"))
                                .build())
                        .build()))
                .succeedsWithin(Duration.ofSeconds(10))
                .satisfies(version -> assertThat(version.getLabels()).contains("integration-test"));
    }

    @Test
    @Order(5)
    void deleteVersion() {
        assertThat(client.asyncSkills().skillsDeleteVersion(
                APISkillsDeleteVersionRequest.newBuilder()
                        .skillName(SKILL_NAME)
                        .skillVersion(1)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(10))
                .isNotNull();
    }
}
