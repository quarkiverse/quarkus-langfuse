# Quarkus Langfuse — Project Context

## Purpose

`quarkus-langfuse` is a Quarkus extension that integrates [Langfuse](https://langfuse.com/) — an open-source LLM observability platform — into Quarkus applications. It provides:

- A fully generated REST client against the Langfuse OpenAPI spec
- A higher-level operations API ([`LangfuseOperations`](runtime/src/main/java/io/quarkiverse/langfuse/api/LangfuseOperations.java) / [`AsyncLangfuseOperations`](runtime/src/main/java/io/quarkiverse/langfuse/api/AsyncLangfuseOperations.java)) built on Jakarta REST Client and Mutiny
- OpenTelemetry span export to Langfuse (when `quarkus-opentelemetry` is on the classpath)
- LangChain4j AI trace integration (when `quarkus-langchain4j` is on the classpath)
- Dev Services support (automatic Langfuse container provisioning)

## Module Layout

```
quarkus-langfuse-parent/
├── langfuse-client              # Generated Langfuse API + models (from OpenAPI spec - temporary until langfuse-java PR #36 lands)
├── langfuse-testcontainers      # LangfuseTestcontainers helper (temporary until langfuse-java PR #36 lands)
├── runtime-compat-spi           # OTel exporter factory SPI (cross-Quarkus version)
├── runtime-compat-quarkus3      # Quarkus 3.x: Jackson 2 mixins + OTel HttpExporter (Quarkus 3 package)
├── runtime-compat-quarkus4      # Quarkus 4.x: Jackson 3 mixins + OTel HttpExporter (Quarkus 4 package)
├── deployment                   # Quarkus build-time processor
├── runtime                      # Extension runtime (CDI beans, config, ops API)
├── integration-tests/           # Multi-profile integration tests
│   ├── langfuse-api-tests
│   ├── langfuse-otel-public-api-tests
│   ├── langfuse-otel-no-lc4j-tests
│   └── langfuse-no-otel-no-lc4j-tests
└── docs                         # Asciidoc reference docs (docs profile only)
```

### `langfuse-client` — API + models

Generated from the Langfuse v4 OpenAPI spec ([`langfuse-client/src/main/openapi/langfuse.yml`](langfuse-client/src/main/openapi/langfuse.yml)) using openapi-generator. It produces:

- **Models**: POJOs in `com.langfuse.api.model` with builders, bean validation annotations, and JSPECIFY nullability markers
- **API interfaces**: Sync and async interfaces in `com.langfuse.api`, split into per-endpoint subpackages (`scores`, `traces`, `ingestion`, etc.)
- **ServiceLoader SPI**: [`LangfuseApiBuilderFactory`](langfuse-client/src/main/java/com/langfuse/api/spi/LangfuseApiBuilderFactory.java) (in `com.langfuse.api.spi`) so downstream frameworks can plug in their own HTTP client

The client module ([`pom.xml`](langfuse-client/pom.xml)) declares **both Jackson 2 and Jackson 3** as `provided` dependencies so the generated model annotations compile against either. At runtime the application supplies the active Jackson version.

**This module is temporary.** It mirrors the upcoming [`langfuse/langfuse-java`](https://github.com/langfuse/langfuse-java) project ([issue #31](https://github.com/langfuse/langfuse-java/issues/31), [PR #36](https://github.com/langfuse/langfuse-java/pull/36)) which will publish a standalone `langfuse-java-api` artifact. Once that lands, `quarkus-langfuse` will depend on it instead of shipping its own copy.

### `langfuse-testcontainers`

A lightweight [`LangfuseContainer`](langfuse-testcontainers/src/main/java/com/langfuse/testcontainers/LangfuseContainer.java) Testcontainers wrapper (PostgreSQL, ClickHouse, MinIO, Redis). Temporarily housed here because the upstream split referenced above ([PR #36](https://github.com/langfuse/langfuse-java/pull/36)) has not yet been merged.

### `runtime-compat-*` — Quarkus version compatibility

Three modules solve the Jackson 2 → Jackson 3, OTel API relocation, and Vert.x 4 → Vert.x 5 divide between Quarkus 3.x and Quarkus 4.x. **This split is necessary because Quarkus 3.x and Quarkus 4.x ship incompatible binary APIs**:

| Module | Jackson | OTel `HttpExporter` package | Vert.x |
|--------|---------|------------------------------|--------|
| `runtime-compat-spi` | None (interface only) | None (interface only) | `vertx-core` (optional) |
| `runtime-compat-quarkus3` | `com.fasterxml.jackson.*` (Jackson 2) | `io.opentelemetry.exporter.internal.http` | Vert.x 4 (from Quarkus 3 BOM) |
| `runtime-compat-quarkus4` | `tools.jackson.*` (Jackson 3) | `io.opentelemetry.exporter.otlp.internal` | Vert.x 5 (from Quarkus 4 BOM) |

**Why the split?** Three breaking changes between Quarkus 3.x and 4.x each require a separate compiled artifact:

1. **Jackson 2 → Jackson 3** — Quarkus 4 ships Jackson 3 with the package namespace `tools.jackson.*` instead of `com.fasterxml.jackson.*`. A class compiled against Jackson 2 cannot be loaded by a JVM using Jackson 3 (different packages, different types). The `ChatMessageWithPlaceholders` deserialization Mixin (which fixes a generated-model polymorphic deserialization issue) exists in two variants — [`ChatMessageWithPlaceholdersJackson2Mixin`](runtime-compat-quarkus3/src/main/java/io/quarkiverse/langfuse/runtime/jackson/ChatMessageWithPlaceholdersJackson2Mixin.java) in the Quarkus 3 module and [`ChatMessageWithPlaceholdersJackson3Mixin`](runtime-compat-quarkus4/src/main/java/io/quarkiverse/langfuse/runtime/jackson/ChatMessageWithPlaceholdersJackson3Mixin.java) in the Quarkus 4 module.

2. **OTel `HttpExporter` relocation** — The class `io.opentelemetry.exporter.internal.http.HttpExporter` (used by the Quarkus 3 OTel span exporter) was moved to `io.opentelemetry.exporter.otlp.internal.HttpExporter` in Quarkus 4. The `LangfuseSpanExporterFactory` SPI exists in [`runtime-compat-spi/src/main/java/io/quarkiverse/langfuse/runtime/otel/LangfuseSpanExporterFactory.java`](runtime-compat-spi/src/main/java/io/quarkiverse/langfuse/runtime/otel/LangfuseSpanExporterFactory.java); the Quarkus 3 and Quarkus 4 implementations each reference the correct package ([`runtime-compat-quarkus3/src/main/java/io/quarkiverse/langfuse/runtime/otel/LangfuseSpanExporterFactoryQuarkus3.java`](runtime-compat-quarkus3/src/main/java/io/quarkiverse/langfuse/runtime/otel/LangfuseSpanExporterFactoryQuarkus3.java) and [`runtime-compat-quarkus4/src/main/java/io/quarkiverse/langfuse/runtime/otel/LangfuseSpanExporterFactoryQuarkus4.java`](runtime-compat-quarkus4/src/main/java/io/quarkiverse/langfuse/runtime/otel/LangfuseSpanExporterFactoryQuarkus4.java)). The SPI's [`LangfuseSpanExporterConfig`](runtime-compat-spi/src/main/java/io/quarkiverse/langfuse/runtime/otel/LangfuseSpanExporterConfig.java) carries a `Consumer<HttpClientOptions> clientOptionsCustomizer` parameter, allowing the `runtime` module to configure TLS and proxy settings without duplicating logic across compat modules.

3. **Vert.x 4 → Vert.x 5** — Quarkus 4 upgraded Vert.x from 4.x to 5.x. While `JsonObject` remains compatible at the API level, other Vert.x internals differ, and the OTel sender (`VertxHttpSender`) wiring needs to match the Vert.x version the application ships.

The deployment module ([`deployment/src/main/java/io/quarkiverse/langfuse/deployment/LangfuseProcessor.java`](deployment/src/main/java/io/quarkiverse/langfuse/deployment/LangfuseProcessor.java)) detects which Jackson/OTel classes are present at runtime and indexes only the matching compat module. The compat modules each pin their own BOM (Quarkus 3 baseline or Quarkus 4 snapshot) so they compile independently of whatever version the top-level build is targeting.

See also: [PR #150](https://github.com/quarkiverse/quarkus-langfuse/pull/150) (dual OTel package support) and [PR #152](https://github.com/quarkiverse/quarkus-langfuse/pull/152) (module rename, Jackson 3 mixins).

### `runtime`

The Quarkus extension runtime ([`runtime/pom.xml`](runtime/pom.xml)). It contains:

- CDI bean factories for `LangfuseApi`, [`QuarkusLangfuseClient`](runtime/src/main/java/io/quarkiverse/langfuse/client/QuarkusLangfuseClient.java), and async clients
- The higher-level [`LangfuseOperations`](runtime/src/main/java/io/quarkiverse/langfuse/api/LangfuseOperations.java) / [`AsyncLangfuseOperations`](runtime/src/main/java/io/quarkiverse/langfuse/api/AsyncLangfuseOperations.java) API (wraps the raw client with fluent builders, pagination, cursor APIs, and batch delete)
- [`LangfuseRecorder`](runtime/src/main/java/io/quarkiverse/langfuse/runtime/LangfuseRecorder.java) (runtime initializers wired from the deployment module)
- Jackson mixin wiring via the unpacked `langfuse-client` models
- [`LangfuseErrorBodies`](runtime/src/main/java/io/quarkiverse/langfuse/client/jaxrs/LangfuseErrorBodies.java) — error body parsing using Vert.x `JsonObject` (works on both Vert.x 4 and 5)
- [`LangfuseHttpClientOptionsCustomizer`](runtime/src/main/java/io/quarkiverse/langfuse/runtime/otel/LangfuseHttpClientOptionsCustomizer.java) — configures `HttpClientOptions` for `export-target=ALL`: enables SSL and ALPN for `https://` URLs, applies TLS settings from the Quarkus TLS registry via `TlsConfigUtils.configure(HttpClientOptions, TlsConfiguration)` (which shares the same signature in Quarkus 3.40 and 4.0), and handles proxy configuration (`quarkus.langfuse.otel.proxy-options.*` with fallback to JDK system properties)
- [`FailureLoggingSpanExporter`](runtime/src/main/java/io/quarkiverse/langfuse/runtime/otel/FailureLoggingSpanExporter.java) — wraps the raw Vert.x OTLP exporter to intercept failed batch exports (which OTel's `BatchSpanProcessor` only logs at `FINE`), logging a single `WARN` per failed batch with the target URL and root cause / HTTP error response, and the full stack trace at `DEBUG`

### `deployment`

The Quarkus build-time processor ([`deployment/src/main/java/io/quarkiverse/langfuse/deployment/LangfuseProcessor.java`](deployment/src/main/java/io/quarkiverse/langfuse/deployment/LangfuseProcessor.java)), a `@BuildStep` class that:

- Registers synthetic CDI beans for the client and API
- Proves the [`LangfuseApiBuilderFactory`](langfuse-client/src/main/java/com/langfuse/api/spi/LangfuseApiBuilderFactory.java) ServiceLoader provider
- Dynamically selects the correct `runtime-compat-*` module at build time based on which Jackson/OTel classes are present on the classpath
- Registers LangChain4j capabilities when `quarkus-langchain4j` is available
- Handles OTel span export configuration (LANGFUSE_ONLY vs. ALL backends)

## Compatibility Matrix

| Extension version | Quarkus       | Langchain4j          | Java     |
|-------------------|---------------|----------------------|----------|
| 0.9.0 (latest)    | 3.40.1.x      | 1.14.1               | 17+      |
|                   | 4.x (Beta)    | 2.0.0.Beta1          | 21+      |

The `quarkus-baseline.version` property in the parent POM controls the default (Quarkus 3) baseline; `quarkus4.version` pins the Quarkus 4 BOM for the `runtime-compat-quarkus4` module. The Quarkus ecosystem CI tests both.

## Upstream Context: langfuse-java PRs #31 and #36

The architecture of this project is directly influenced by two pending items in the upstream [`langfuse/langfuse-java`](https://github.com/langfuse/langfuse-java) repository:

- **Issue #31** ([Break api into 2 parts](https://github.com/langfuse/langfuse-java/issues/31)) — raised the need to separate the Langfuse Java API (models + interfaces) from the HTTP client implementation, so downstream frameworks (Spring, Quarkus) can supply their own HTTP client and Jackson version. The issue specifically noted that apps using Jackson 3 cannot use the current API at all, and that `CompletableFuture` in async methods forces a concurrency implementation on the end user.

- **PR #36** ([Break api into 2 parts](https://github.com/langfuse/langfuse-java/pull/36)) — implements that split: a `langfuse-java-api` module (generated models + ServiceLoader SPI) and a `langfuse-java-client` module (reference `java.net.http.HttpClient` implementation with dual Jackson 2/3 support). This PR has been kept up to date with spec changes for months but has not yet been merged by the Langfuse maintainers.

Until PR #36 lands and is released, `quarkus-langfuse` ships its own copies of both the API (in `langfuse-client`) and the testcontainers helper (in `langfuse-testcontainers`). The long-term goal is to depend on the upstream releases once they are published.

## Nightly Snapshot Build Status

The [Quarkus ecosystem CI workflow](https://github.com/quarkiverse/quarkus-langfuse/actions/workflows/quarkus-snapshot.yaml) (`quarkus-snapshot.yaml`) runs nightly against Quarkus `main` (currently 4.x) via the [`quarkiversebot`](https://github.com/apps/quarkusbot). As of the latest release (0.9.0 / [commit `0bb7e56`](https://github.com/quarkiverse/quarkus-langfuse/commit/0bb7e568f9ad3a073a03c4fc37cf02b1cf5e278a)), **this build consistently fails** — [all recent runs](https://github.com/quarkiverse/quarkus-langfuse/actions?query=workflow%3A%22Quarkus+ecosystem+CI%22) show the build failing.

The failure is specific to the `langfuse-api-tests` integration test module: the GraalVM native-image builder exits with code 1 during image generation. The other three integration test modules (`langfuse-otel-no-lc4j-tests`, `langfuse-no-otel-no-lc4j-tests`, and `langfuse-otel-public-api-tests`) all build native images successfully. The `langfuse-api-tests` module is the only one that includes the full extension suite: `quarkus-rest-jackson`, `quarkus-wiremock`, `quarkus-opentelemetry`, `quarkus-micrometer-opentelemetry`, `quarkus-smallrye-health`, `quarkus-smallrye-openapi`, and `quarkus-langchain4j-openai`.

The quarkus-maven-plugin's docker wrapper captures the native-image stdout but truncates the actual GraalVM error before the `langfuse-api-tests` build. The visible output shows warnings about unknown module exports (`io.vertx.core`, `io.quarkus.resteasy.reactive.client`) but not the fatal error itself. **Root cause not yet identified.**

## Recent History

| Commit | PR | Description |
|--------|----|-------------|
| `0bb7e56` | [#152](https://github.com/quarkiverse/quarkus-langfuse/pull/152) | Make the build work against Quarkus 4 (Jackson 3, Vert.x 5) — renamed compat modules, added Jackson 3 mixins, fixed OTel sampler |
| `df7822c` | [#150](https://github.com/quarkiverse/quarkus-langfuse/pull/150) | Support both Quarkus 3 and Quarkus 4 OTel HttpExporter packages via SPI |
| `cf3d666` | [#147](https://github.com/quarkiverse/quarkus-langfuse/pull/147) | Release 0.9.0 |
| `f69e4bb` | [#121](https://github.com/quarkiverse/quarkus-langfuse/pull/121) | Drop split OTel support (Quarkus 3.40 LTS baseline unified the OTel packages) |
| `91e0559` | [#142](https://github.com/quarkiverse/quarkus-langfuse/pull/142) | Refresh generated client against updated Langfuse OpenAPI spec |
| `ed3a716` | [#144](https://github.com/quarkiverse/quarkus-langfuse/pull/144) | Pin default Langfuse images to spec's server release |

## Key Design Decisions

- **API/client decoupling via ServiceLoader** — follows the pattern established in `langfuse-java` PR #36 and the docling-java project: models and API interfaces in one artifact, HTTP transport in another.
- **Jakarta RS at the transport boundary** — Quarkus supplies `quarkus-rest-client-jackson` (Jakarta REST Client) as the default HTTP implementation; the API layer has no HTTP dependency.
- **Optional OTel and LangChain4j** — neither is required; they activate only when on the classpath. The deployment module detects availability and configures accordingly.
- **Generated models, hand-coded operations** — the REST client is auto-generated from OpenAPI, but the higher-level API (`LangfuseOperations`) and the Quarkus integration (Dev Services, OTel exporter, config) are hand-written.
- **No Jackson annotations in generated code** — the generated models avoid framework-specific annotations; Jackson configuration is provided via Mixins (for polymorphic deserialization fixes) and custom deserializers in the compat modules.
- **OTel Exporter TLS and Proxy via `TlsConfigUtils` and SPI customizer** — instead of duplicating OTel exporter internals or splitting TLS configuration across compat modules, `LangfuseSpanExporterConfig` carries a `Consumer<HttpClientOptions>` customizer through the SPI. TLS configuration delegates to the shared Quarkus helper `TlsConfigUtils.configure(HttpClientOptions, TlsConfiguration)` (identical signature across Quarkus 3.40 and 4.0), honouring the Quarkus TLS registry (`quarkus.langfuse.otel.tls-configuration-name` or default). Failed batch exports are wrapped with `FailureLoggingSpanExporter` to log at WARN with the cause instead of silent FINE.
