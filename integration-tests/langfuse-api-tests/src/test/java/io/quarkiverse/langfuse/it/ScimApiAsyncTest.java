package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.model.ScimCreateUserRequest;
import com.langfuse.api.scim.ScimApi.APIScimCreateUserRequest;
import com.langfuse.api.scim.ScimApi.APIScimDeleteUserRequest;
import com.langfuse.api.scim.ScimApi.APIScimGetUserRequest;
import com.langfuse.api.scim.ScimApi.APIScimListUsersRequest;

import io.quarkus.test.junit.QuarkusTest;

@Disabled("Requires org-admin role")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
class ScimApiAsyncTest {

    private static String userId;

    @Inject
    LangfuseApi client;

    @Test
    @Order(1)
    void getServiceProviderConfig() {
        assertThat(client.asyncScim().scimGetServiceProviderConfig())
                .succeedsWithin(Duration.ofSeconds(5))
                .isNotNull();
    }

    @Test
    @Order(1)
    void getResourceTypes() {
        assertThat(client.asyncScim().scimGetResourceTypes())
                .succeedsWithin(Duration.ofSeconds(5))
                .isNotNull();
    }

    @Test
    @Order(1)
    void getSchemas() {
        assertThat(client.asyncScim().scimGetSchemas())
                .succeedsWithin(Duration.ofSeconds(5))
                .isNotNull();
    }

    @Test
    @Order(1)
    void listUsers() {
        assertThat(client.asyncScim().scimListUsers(
                APIScimListUsersRequest.newBuilder()
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(response -> assertThat(response.getResources()).isNotNull());
    }

    @Test
    @Order(2)
    void createUser() {
        assertThat(client.asyncScim().scimCreateUser(
                APIScimCreateUserRequest.newBuilder()
                        .scimCreateUserRequest(ScimCreateUserRequest.builder()
                                .userName("async-scim-test-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com")
                                .build())
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(user -> {
                    assertThat(user.getId()).isNotBlank();
                    assertThat(user.getUserName()).isNotBlank();
                    userId = user.getId();
                });
    }

    @Test
    @Order(3)
    void getUser() {
        assertThat(client.asyncScim().scimGetUser(
                APIScimGetUserRequest.newBuilder()
                        .userId(userId)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5))
                .satisfies(user -> {
                    assertThat(user.getId()).isEqualTo(userId);
                    assertThat(user.getUserName()).isNotBlank();
                });
    }

    @Test
    @Order(4)
    void deleteUser() {
        assertThat(client.asyncScim().scimDeleteUser(
                APIScimDeleteUserRequest.newBuilder()
                        .userId(userId)
                        .build()))
                .succeedsWithin(Duration.ofSeconds(5));
    }
}
