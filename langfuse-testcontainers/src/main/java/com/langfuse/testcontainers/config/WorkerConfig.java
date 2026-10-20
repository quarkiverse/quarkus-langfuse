package com.langfuse.testcontainers.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Langfuse worker configuration.
 *
 * @author Eric Deandrea
 */
public interface WorkerConfig {
    /**
     * Container image repository of the Langfuse worker, without a tag.
     */
    String IMAGE_REPOSITORY = "docker.io/langfuse/langfuse-worker";

    /**
     * Default container image for the Langfuse worker, pinned to the same release as the web server
     * ({@link LangfuseConfig#SERVER_VERSION}).
     */
    String DEFAULT_IMAGE = IMAGE_REPOSITORY + ":" + LangfuseConfig.SERVER_VERSION;

    /**
     * @return the container image
     */
    String image();

    /**
     * @return additional container environment variables
     */
    Map<String, String> containerEnv();

    /**
     * Builder for {@link WorkerConfig}.
     *
     * @author Eric Deandrea
     */
    final class Builder {
        private final LangfuseContainerConfig.Builder parent;
        String image = DEFAULT_IMAGE;
        final Map<String, String> containerEnv = new HashMap<>();

        Builder(LangfuseContainerConfig.Builder parent) {
            this.parent = parent;
        }

        /**
         * @return the parent {@link LangfuseContainerConfig.Builder}
         */
        public LangfuseContainerConfig.Builder and() {
            return parent;
        }

        /**
         * @param image the container image (default: {@value WorkerConfig#DEFAULT_IMAGE})
         * @return this builder
         */
        public Builder image(String image) {
            this.image = image;
            return this;
        }

        /**
         * @param key the environment variable name
         * @param value the environment variable value
         * @return this builder
         */
        public Builder containerEnv(String key, String value) {
            this.containerEnv.put(key, value);
            return this;
        }

        /**
         * @param env a map of environment variable names to values
         * @return this builder
         */
        public Builder containerEnv(Map<String, String> env) {
            this.containerEnv.putAll(env);
            return this;
        }

        WorkerConfig build() {
            return new DefaultWorkerConfig(this);
        }
    }
}
