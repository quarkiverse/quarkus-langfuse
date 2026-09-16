package io.quarkiverse.langfuse.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.langfuse.api.model.CreateModelRequest;
import com.langfuse.api.model.ModelTokenizerId;
import com.langfuse.api.model.ModelUsageUnit;

import io.quarkiverse.langfuse.api.AsyncLangfuseOperations;
import io.quarkiverse.langfuse.api.DeletionOutcome;
import io.quarkiverse.langfuse.api.LangfuseOperations;
import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.smallrye.mutiny.Uni;

/**
 * Verifies that {@code quarkus.langfuse.api.delete-concurrency} changes only timing, never which
 * outcomes are produced.
 *
 * <p>
 * Runs under a {@link QuarkusTestProfile} pinning the ceiling to {@code 1}, so the whole batch runs
 * serially on the calling thread with no fan-out. A profile starts a separate Quarkus instance, so
 * this class shares no state with {@link OperationsIntegrationTest} and creates every fixture it
 * needs.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@QuarkusTest
@TestProfile(SerialDeleteConcurrencyIntegrationTest.SerialDeleteProfile.class)
class SerialDeleteConcurrencyIntegrationTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final String RUN_ID = UUID.randomUUID().toString().substring(0, 8);
    private static final String MODEL_PREFIX = "it-serial-model-" + RUN_ID + "-";

    public static class SerialDeleteProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("quarkus.langfuse.api.delete-concurrency", "1");
        }
    }

    @Inject
    LangfuseOperations langfuse;

    @Inject
    AsyncLangfuseOperations asyncLangfuse;

    @Inject
    LangfuseConfig config;

    @Test
    @Order(1)
    void theProfileOverrideReachesTheValueTheDeleteEnginesRead() {
        // Asserted on the injected mapping rather than the raw config source: this is the exact value
        // the operations read per call, so a profile that failed to apply cannot pass unnoticed and
        // leave every later assertion proving nothing.
        assertThat(config.api().deleteConcurrency()).isEqualTo(1);
    }

    @Test
    @Order(2)
    void outcomesAreIdenticalWhenDeletesRunSerially() {
        var present = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> createModel(MODEL_PREFIX + i))
                .toList();
        var absent = "absent-" + UUID.randomUUID();

        var identifiers = Stream.concat(present.stream(), Stream.of(absent))
                .toList();

        var result = langfuse.models().deleteById(identifiers);

        assertThat(result.size()).isEqualTo(6);
        assertThat(result.deleted()).containsExactlyInAnyOrderElementsOf(present);
        assertThat(result.notFound()).containsExactlyInAnyOrder(absent);
        assertThat(result.hasFailures()).isFalse();
    }

    @Test
    @Order(3)
    void theAsyncTreeAgreesWhenRunSerially() {
        var present = createModel(MODEL_PREFIX + "async");
        var absent = "absent-" + UUID.randomUUID();

        var result = await(asyncLangfuse.models().deleteById(present, absent));

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.outcome(present)).get().isInstanceOf(DeletionOutcome.Deleted.class);
        assertThat(result.outcome(absent)).get().isInstanceOf(DeletionOutcome.NotFound.class);
    }

    private String createModel(String name) {
        return langfuse.models().createIfAbsent(CreateModelRequest.builder()
                .modelName(name)
                .matchPattern("(?i)^(%s)$".formatted(name))
                .unit(ModelUsageUnit.TOKENS)
                .inputPrice(0.001)
                .outputPrice(0.002)
                .tokenizerId(ModelTokenizerId.OPENAI)
                .build())
                .getId();
    }

    private static <T> T await(Uni<T> uni) {
        return uni.await().atMost(TIMEOUT);
    }
}
