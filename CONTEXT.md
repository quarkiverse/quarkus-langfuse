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
| 0.9.1 (latest)    | 3.40.1.x      | 1.14.2               | 17+      |
|                   | 4.0.0.Beta2   | 2.0.0.Beta2          | 21+      |

The `quarkus-baseline.version` property in the parent POM controls the default (Quarkus 3) baseline; `quarkus4.version` pins the Quarkus 4 BOM for the `runtime-compat-quarkus4` module, and `quarkus4-langchain4j.version` pins the LangChain4j line that matches it (LangChain4j 1.x is built for Vert.x 4 and fails against Vert.x 5). Both properties are consumed by the `quarkus4` Maven profile, which rebuilds the whole tree against the Quarkus 4 line, by the `build-quarkus4` job in [`.github/workflows/build.yml`](.github/workflows/build.yml) (so a PR that breaks Quarkus 4 fails on that PR), and — for the LangChain4j pin — by [`.github/quarkus-ecosystem-test`](.github/quarkus-ecosystem-test), which builds against Quarkus `main` nightly.

## Upstream Context: langfuse-java PRs #31 and #36

The architecture of this project is directly influenced by two pending items in the upstream [`langfuse/langfuse-java`](https://github.com/langfuse/langfuse-java) repository:

- **Issue #31** ([Break api into 2 parts](https://github.com/langfuse/langfuse-java/issues/31)) — raised the need to separate the Langfuse Java API (models + interfaces) from the HTTP client implementation, so downstream frameworks (Spring, Quarkus) can supply their own HTTP client and Jackson version. The issue specifically noted that apps using Jackson 3 cannot use the current API at all, and that `CompletableFuture` in async methods forces a concurrency implementation on the end user.

- **PR #36** ([Break api into 2 parts](https://github.com/langfuse/langfuse-java/pull/36)) — implements that split: a `langfuse-java-api` module (generated models + ServiceLoader SPI) and a `langfuse-java-client` module (reference `java.net.http.HttpClient` implementation with dual Jackson 2/3 support). This PR has been kept up to date with spec changes for months but has not yet been merged by the Langfuse maintainers.

Until PR #36 lands and is released, `quarkus-langfuse` ships its own copies of both the API (in `langfuse-client`) and the testcontainers helper (in `langfuse-testcontainers`). The long-term goal is to depend on the upstream releases once they are published.

## Nightly Snapshot Build Status

The [Quarkus ecosystem CI workflow](https://github.com/quarkiverse/quarkus-langfuse/actions/workflows/quarkus-snapshot.yaml) (`quarkus-snapshot.yaml`) runs nightly against Quarkus `main` (currently 4.x) via the [`quarkiversebot`](https://github.com/apps/quarkusbot). It had been failing on every run; **both root causes are now identified**, one of which hid behind the other.

### 1. The `TlsConfiguration` split (fixed here)

Since 2026-10-30 the build stopped at compiling `runtime`'s test sources: Quarkus 4 removed `TlsConfiguration.getSSLOptions()` in favour of `getServerSSLOptions()` / `getClientSSLOptions()`, and `LangfuseHttpClientOptionsCustomizerTests` stubbed the Quarkus 3 shape. Production code was never affected. The test is now written against only the `TlsConfiguration` members that exist and behave identically on both lines — see the TLS entry under [Key Design Decisions](#key-design-decisions) for what changed and where the lost coverage moved.

### 2. LangChain4j `OpenAiRestApi$ObjectMapperHolder` initialised at build time (worked around)

Behind that, the native image build of `integration-tests/langfuse-api-tests` fails on Quarkus `main` with:

```
Error: Class initialization of io.quarkiverse.langchain4j.openai.common.OpenAiRestApi$ObjectMapperHolder failed
Caused by: java.lang.NullPointerException: Cannot invoke "io.quarkus.arc.ArcContainer.instance(...)"
           because the return value of "io.quarkus.arc.Arc.container()" is null
```

`OpenAiRestApi$ObjectMapperHolder` resolves its `ObjectMapper` from Arc (`QuarkusJsonCodecFactory.SnakeCaseObjectMapperHolder.MAPPER`) in a static initializer. `OpenAiRestApi` is a REST Client interface and so is registered for reflection; on Quarkus `main` that registration pulls in all nest members (`ReflectionDataBuilder.registerAllNestMembersQuery`), making the holder reachable and initialised at *build* time, where Arc is not running. Neither `OpenAiCommonProcessor#nativeImageSupport` nor `OpenAiProcessor#nativeSupport` registers it as a `RuntimeInitializedClassBuildItem`.

This is not a regression on `main` — `main` is the 4.x line, and the failure reproduces equally on released Quarkus 4.0.0.Beta2 + LangChain4j 2.0.0.Beta2. It is reported upstream as [quarkus-langchain4j#3127](https://github.com/quarkiverse/quarkus-langchain4j/issues/3127) (the missing runtime-init registration) and [quarkus#58088](https://github.com/quarkusio/quarkus/issues/58088) (whether a nest-member reflection *query* registration should imply type reachability at all). Until those land, [`integration-tests/langfuse-api-tests/src/main/resources/application.properties`](integration-tests/langfuse-api-tests/src/main/resources/application.properties) carries a `quarkus.native.additional-build-args=--initialize-at-run-time=...` workaround. The property is applied unconditionally rather than only under the `quarkus4` profile, because the ecosystem CI overrides `quarkus.version` directly and never activates that profile — scoping it to the profile would leave the one build that most needs it uncovered. That is safe: the holder's FQN is identical in LangChain4j 1.14.2 and 2.0.0.Beta2, and the Quarkus 3 baseline native build is green with the flag in place.

**Remove that property once the upstream issues are closed** — it is the only reason that line exists, and note that `quarkus.native.additional-build-args` is a single comma-separated value, so anything else needing a native build arg in that module must join it rather than add a second line.

## Recent History

| Commit | PR | Description |
|--------|----|-------------|
| | [#165](https://github.com/quarkiverse/quarkus-langfuse/pull/165) | Make the Quarkus ecosystem CI build green again — version-agnostic TLS unit test, Quarkus 4 pins refreshed to 4.0.0.Beta2 / LangChain4j 2.0.0.Beta2, new `quarkus4` profile and `build-quarkus4` PR CI job, native `ObjectMapperHolder` workaround |
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
- **OTel Exporter TLS and Proxy via `TlsConfigUtils` and SPI customizer** — instead of duplicating OTel exporter internals or splitting TLS configuration across compat modules, `LangfuseSpanExporterConfig` carries a `Consumer<HttpClientOptions>` customizer through the SPI. TLS configuration delegates to the shared Quarkus helper `TlsConfigUtils.configure(HttpClientOptions, TlsConfiguration)`, honouring the Quarkus TLS registry (`quarkus.langfuse.otel.tls-configuration-name` or default). Failed batch exports are wrapped with `FailureLoggingSpanExporter` to log at WARN with the cause instead of silent FINE.

  Only the **signature** of `TlsConfigUtils.configure` is identical across Quarkus 3.40 and 4.0 — what it reads is not. Quarkus 4 split `TlsConfiguration.getSSLOptions()` into `getServerSSLOptions()` / `getClientSSLOptions()` and now sources trust options, key/cert options, `trustAll`, hostname verification and ALPN from `getClientSSLOptions()` rather than from `getTrustStoreOptions()` / `getKeyStoreOptions()` / `isTrustAll()` / `getSSLOptions()`. The `ClientSSLOptions` return type does not exist in Vert.x 4, so neither `runtime` production code nor its tests may name any version-specific `TlsConfiguration` member — `LangfuseHttpClientOptionsCustomizer` sticks to `TlsConfiguration.from(...)`, `isTrustAll()` and `TlsConfigUtils.configure`, all stable on both lines. Consequently [`LangfuseHttpClientOptionsCustomizerTests`](runtime/src/test/java/io/quarkiverse/langfuse/runtime/otel/LangfuseHttpClientOptionsCustomizerTests.java) asserts only version-stable behaviour (`usesSni()` → `isForceSni()`, SSL/ALPN, trustAll, registry resolution); that real trust-store options reach the HTTP client is asserted end-to-end instead by the deployment module's [`BaseHttpsSpanExportTests`](deployment/src/test/java/io/quarkiverse/langfuse/deployment/otel/BaseHttpsSpanExportTests.java) and its subclasses, which run a real TLS handshake against an HTTPS WireMock server. Do not weaken those tests.
