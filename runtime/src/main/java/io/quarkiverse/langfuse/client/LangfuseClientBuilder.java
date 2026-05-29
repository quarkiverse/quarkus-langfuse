package io.quarkiverse.langfuse.client;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.jboss.resteasy.reactive.client.api.LoggingScope;

import io.quarkiverse.langfuse.config.LangfuseConfig;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;

public final class LangfuseClientBuilder {
    private String baseUrl;
    private Duration timeout;
    private Duration connectTimeout;
    private Duration readTimeout;
    private boolean logRequests;
    private boolean logResponses;
    private boolean prettyPrint;

    public LangfuseClientBuilder(LangfuseConfig config) {
        checkValidValue(config.baseUrl(), LangfuseConfig.BASE_URL_KEY);
        checkValidValue(config.username(), LangfuseConfig.USERNAME_KEY);
        checkValidValue(config.password(), LangfuseConfig.PASSWORD_KEY);

        baseUrl(config.baseUrl());
        timeout(config.timeout());
        logRequests(config.logRequests());
        logResponses(config.logResponses());
        prettyPrint(config.prettyPrint());
        connectTimeout(config.connectTimeout());
        readTimeout(config.readTimeout());
    }

    public LangfuseClientBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public LangfuseClientBuilder timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public LangfuseClientBuilder logRequests(boolean logRequests) {
        this.logRequests = logRequests;
        return this;
    }

    public LangfuseClientBuilder logResponses(boolean logResponses) {
        this.logResponses = logResponses;
        return this;
    }

    public LangfuseClientBuilder prettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
        return this;
    }

    public LangfuseClientBuilder connectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public LangfuseClientBuilder readTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    private static void checkValidValue(String value, String key) {
        if ((value == null) || value.trim().isBlank()) {
            throw new IllegalArgumentException("'%s' cannot be null or empty".formatted(key));
        }
    }

    public QuarkusLangfuseClient build() {

        var defaultTimeout = Optional.ofNullable(this.timeout).orElse(Duration.ofMinutes(1));
        var defaultConnectTimeout = getOrDefault(this.connectTimeout, defaultTimeout);
        var defaultReadTimeout = getOrDefault(this.readTimeout, defaultTimeout);

        var restApiBuilder = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(this.baseUrl))
                .connectTimeout(defaultConnectTimeout.toSeconds(), TimeUnit.SECONDS)
                .readTimeout(defaultReadTimeout.toSeconds(), TimeUnit.SECONDS);

        if (this.logRequests || this.logResponses) {
            restApiBuilder
                    .loggingScope(LoggingScope.REQUEST_RESPONSE)
                    .clientLogger(new LangfuseClientLogger(this.logRequests, this.logResponses, this.prettyPrint));
        }

        return restApiBuilder.build(QuarkusLangfuseClient.class);
    }

    private static Duration getOrDefault(Duration duration, Duration defaultValue) {
        return Optional.ofNullable(duration).orElse(defaultValue);
    }
}
