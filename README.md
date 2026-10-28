[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.langfuse/quarkus-langfuse?logo=apache-maven&style=flat-square)](https://central.sonatype.com/artifact/io.quarkiverse/langfuse/quarkus-langfuse-parent)

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-3-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

# Quarkus Langfuse

A Quarkus extension for [Langfuse](https://langfuse.com), the open-source LLM engineering platform. This extension provides a type-safe REST client (based on the [Langfuse Java SDK](https://github.com/langfuse/langfuse-java)), DevServices for zero-config local development, and a Dev UI card for quick access to the Langfuse dashboard.

[Langfuse](https://langfuse.com) helps you ship AI Agents and Products from prototype to production and beyond. It provides observability (traces, sessions, scores), prompt management (versioning, deployment, A/B testing), and evaluation (datasets, experiments, LLM-as-a-judge) for LLM-powered applications.

## Features

- **Type-safe Langfuse API client** - Synchronous and asynchronous clients covering the full [Langfuse public API](https://langfuse.com/docs) (traces, prompts, scores, datasets, observations, skills, and more), built on the Quarkus REST Client Reactive.
- **Higher-level API operations** - Curated `LangfuseOperations` and `AsyncLangfuseOperations` (Mutiny) beans covering seventeen domains - models, datasets and dataset items, LLM connections, score configs, evaluation rules, evaluators and their versions, prompts, annotation queues and their items, comments, scores, observations, experiments and experiment items, and blob storage integrations. They offer find-by-id and find-by-name, existence checks, filtered views, lazy pagination with short-circuiting, idempotent resource initialization, and batch deletes that report an outcome per identifier, with an `api()` escape hatch to the raw client.
- **CDI integration** - Inject `LangfuseApi`, `LangfuseOperations`, or `AsyncLangfuseOperations` directly into your beans.
- **DevServices** - Automatically starts a complete Langfuse stack (Langfuse server, PostgreSQL, ClickHouse, Redis, MinIO, and Worker) in dev and test mode using Testcontainers. No manual setup required.
- **Dev UI** - Provides a Dev UI card with a direct link to the Langfuse dashboard running in your local DevServices instance.
- **Native image support** - Compatible with GraalVM native image compilation.
- **OpenTelemetry integration** - When `quarkus-opentelemetry` is on the classpath, automatically exports AI-related spans to Langfuse. Zero configuration needed with DevServices.

## Compatibility

### Langfuse v4

This version of the extension requires **Langfuse v4**. If you are upgrading from a previous version of the extension that targeted Langfuse v3, complete the [Langfuse v4 migration](https://langfuse.com/changelog/2025-05-14-langfuse-v4) before upgrading the extension.

### Quarkus

This version of the extension requires **Quarkus 3.40 LTS or later**. Applications still on Quarkus 3.33 LTS should stay on the 0.8.x releases of the extension (0.9.0 is the first release that requires Quarkus 3.40).

### OpenAPI spec validation

The Langfuse v4 OpenAPI spec uses the `const` keyword (an OpenAPI 3.1 feature) while declaring version 3.0.1. The `openapi-generator-maven-plugin` cannot validate the spec with this incompatibility, so `skipValidateSpec` is set to `true` in the `langfuse-client` module's POM. This does not affect code generation -- only the upfront validation check is bypassed. This is tracked upstream as [openapi-generator#10445](https://github.com/OpenAPITools/openapi-generator/issues/10445).

## Installation

Add the extension dependency to your project:

### Maven

```xml
<dependency>
    <groupId>io.quarkiverse.langfuse</groupId>
    <artifactId>quarkus-langfuse</artifactId>
    <version>${quarkus-langfuse.version}</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.quarkiverse.langfuse:quarkus-langfuse:${quarkus-langfuse.version}'
```

## Configuration

Configure your Langfuse connection in `application.properties`:

```properties
quarkus.langfuse.base-url=https://cloud.langfuse.com
quarkus.langfuse.public-key=pk-lf-...
quarkus.langfuse.secret-key=sk-lf-...
```

> **Note:** When DevServices is enabled (the default in dev/test mode), `base-url`, `username`, and `password` are automatically configured for you. You only need to set these properties when connecting to an external Langfuse instance or in production.

### Configuration Reference

| Property | Type | Default | Description |
|---|---|---|---|
| `quarkus.langfuse.base-url` | `String` | _required_ | Base URL of the Langfuse server |
| `quarkus.langfuse.public-key` | `String` | _required_ | Langfuse project public key |
| `quarkus.langfuse.secret-key` | `String` | _required_ | Langfuse project secret key |
| `quarkus.langfuse.timeout` | `Duration` | `1m` | Default timeout for Langfuse API calls |
| `quarkus.langfuse.connect-timeout` | `Duration` | `${quarkus.langfuse.timeout}` | Timeout to establish a connection |
| `quarkus.langfuse.read-timeout` | `Duration` | `${quarkus.langfuse.timeout}` | Timeout for receiving a response |
| `quarkus.langfuse.log-requests` | `boolean` | `false` | Log outgoing requests |
| `quarkus.langfuse.log-responses` | `boolean` | `false` | Log incoming responses |
| `quarkus.langfuse.pretty-print` | `boolean` | `false` | Pretty-print JSON bodies in logs |
| `quarkus.langfuse.otel.enabled` | `boolean` | `true` | Enable/disable OpenTelemetry integration (build time, defaults to `quarkus.otel.enabled`) |
| `quarkus.langfuse.otel.export-target` | `String` | `ALL` | Export target (build time): `ALL` (Langfuse + other OTLP backends) or `LANGFUSE_ONLY` |
| `quarkus.langfuse.otel.trace-ingestion-url` | `String` | `<base-url>/api/public/otel/v1/traces` | Override the OTLP trace ingestion endpoint (applies when `export-target=ALL`) |
| `quarkus.langfuse.otel.span-filter` | `String` | `AI_ONLY` | Span filter: `AI_ONLY` (AI spans + ancestors; a span is an AI span if it has `gen_ai.*` or `langfuse.observation.*` attributes) or `ALL` (applies when `export-target=ALL`) |
| `quarkus.langfuse.otel.tls-configuration-name` | `String` | `${quarkus.otel.exporter.otlp.traces.tls-configuration-name:}` | Named TLS configuration (`quarkus.tls.<name>.*`) for HTTPS export (applies when `export-target=ALL`) |
| `quarkus.langfuse.otel.proxy-options.enabled` | `boolean` | `false` | Enable/disable proxy for OTel export (applies when `export-target=ALL`) |
| `quarkus.langfuse.otel.proxy-options.host` | `String` | _none_ | Proxy host (falls back to `https.proxyHost`/`http.proxyHost` JDK system properties) |
| `quarkus.langfuse.otel.proxy-options.port` | `int` | `3128` | Proxy port (falls back to `https.proxyPort`/`http.proxyPort` JDK system properties) |
| `quarkus.langfuse.otel.proxy-options.username` | `String` | _none_ | Proxy username (falls back to `https.proxyUser`/`http.proxyUser` JDK system properties) |
| `quarkus.langfuse.otel.proxy-options.password` | `String` | _none_ | Proxy password (falls back to `https.proxyPassword`/`http.proxyPassword` JDK system properties) |

## Usage

Simply inject `LangfuseApi` into your CDI beans. The extension automatically configures and provides the implementation:

```java
import com.langfuse.api.LangfuseApi;

@ApplicationScoped
public class MyService {

    @Inject
    LangfuseApi langfuseApi;

    public void listObservations() {
        // Synchronous call
        var observations = langfuseApi.observations().observationsGetMany(/* parameters */);

        // Asynchronous call
        var asyncObservations = langfuseApi.asyncObservations().observationsGetMany(/* parameters */);
    }

    public void listPrompts() {
        var prompts = langfuseApi.prompts().promptsList(/* parameters */);
    }

    public void listScores() {
        var scores = langfuseApi.scores().scoresGetMany(/* parameters */);
    }

    public void checkHealth() {
        var health = langfuseApi.health().healthHealth();
    }
}
```

### Available API Groups

The `LangfuseApi` bean provides access to the full [Langfuse public API](https://langfuse.com/docs). Each API group is available as a method, with both synchronous and asynchronous variants (e.g., `trace()` / `asyncTrace()`):

| API Group | Description |
|---|---|
| `health()` | Health check endpoints |
| `opentelemetry()` | OpenTelemetry trace ingestion (recommended for v4) |
| `observations()` | Query observations with selective fields and cursor pagination (v2) |
| `scores()` | Create scores (batch, `POST /api/public/scores`), query scores with cursor pagination (v3), and delete scores |
| `experiments()` | List experiments and experiment items (dataset runs) |
| `prompts()` | Manage, version, and retrieve prompts |
| `promptVersion()` | Manage individual prompt versions |
| `scoreConfigs()` | Manage score configurations |
| `datasets()` | Create and manage datasets |
| `datasetItems()` | Manage dataset items |
| `models()` | Create, upsert, and manage model definitions and pricing |
| `evaluators()` | Create and manage evaluators (v2) |
| `evaluationRules()` | Create and manage evaluation rules (v2) |
| `projects()` | Manage projects and API keys |
| `organizations()` | Manage organization memberships |
| `comments()` | Manage comments on traces |
| `media()` | Upload and manage media attachments |
| `metrics()` | Query usage metrics (v2) |
| `annotationQueues()` | Manage annotation queues for human review |
| `llmConnections()` | Manage LLM provider connections (project scope) |
| `blobStorageIntegrations()` | Manage blob storage integrations |
| `scim()` | SCIM user provisioning |
| `feedback()` | Submit feedback about Langfuse features |
| `unstableDashboardWidgets()` | Create, list, update, and delete dashboard widgets (unstable) |
| `unstableDashboards()` | Create, list, update, and delete dashboards with placements (unstable) |
| `skills()` | Manage skills, their versions, labels and file contents |
| `trace()` | Trace delete endpoints (reads are unavailable in v4 events_only mode; use `observations()`) |

## OpenTelemetry Integration

When the `quarkus-opentelemetry` extension is on the classpath, the Langfuse extension automatically exports OpenTelemetry span data to Langfuse. No additional configuration is needed -- authentication is derived from your existing `quarkus.langfuse.username` and `quarkus.langfuse.password` settings.

### Export Target

The `quarkus.langfuse.otel.export-target` property (build time) controls how spans are exported:

- **`ALL`** (default) -- The extension registers a dedicated Langfuse span processor that runs alongside any other OpenTelemetry exporters you have configured. Spans are exported to both Langfuse and your standard OTLP backend (Jaeger, Zipkin, or any other collector). In this mode, only AI-related spans are sent to Langfuse by default (see [AI Span Filtering](#ai-span-filtering) below), and the `gen_ai.prompt` / `gen_ai.completion` attributes are automatically mapped to the Langfuse trace input and output.

- **`LANGFUSE_ONLY`** -- The standard OpenTelemetry OTLP exporter is configured to send directly to Langfuse. No separate span processor is registered and no additional OTLP backend receives spans. All spans are exported to Langfuse without filtering. This is equivalent to the [manual configuration described in the Quarkus LangChain4j docs](https://docs.quarkiverse.io/quarkus-langchain4j/dev/observability.html#_manual_configuration_without_quarkus_langfuse), but handled automatically by the extension.

```properties
quarkus.langfuse.otel.export-target=LANGFUSE_ONLY
```

### HTTPS / TLS

When `quarkus.langfuse.base-url` (or `trace-ingestion-url`) uses `https://` and `export-target=ALL` (the default), the dedicated Langfuse OTel exporter automatically connects over TLS with ALPN enabled.

By default, the exporter uses the Quarkus TLS registry and honours the TLS configuration specified in `quarkus.langfuse.otel.tls-configuration-name`. This setting defaults to `${quarkus.otel.exporter.otlp.traces.tls-configuration-name:}` (the OTel traces exporter's TLS configuration), falling back to the default `quarkus.tls.*` configuration if neither is explicitly set.

For example, to configure a custom CA certificate or mTLS for the Langfuse exporter:

```properties
quarkus.langfuse.base-url=https://langfuse.internal.corp
quarkus.langfuse.otel.tls-configuration-name=langfuse-tls

quarkus.tls.langfuse-tls.trust-store.pem.certs=/path/to/langfuse-ca.crt
```

> **Note:** When using `export-target=LANGFUSE_ONLY`, the standard Quarkus OpenTelemetry exporter is used instead, and TLS is configured using the standard `quarkus.otel.exporter.otlp.*` TLS configuration properties.

### Proxy Support

When `export-target=ALL`, the dedicated Langfuse exporter can connect through an HTTP/HTTPS proxy via `quarkus.langfuse.otel.proxy-options.*`:

```properties
quarkus.langfuse.otel.proxy-options.enabled=true
quarkus.langfuse.otel.proxy-options.host=proxy.internal.corp
quarkus.langfuse.otel.proxy-options.port=8080
```

If `proxy-options.enabled=true` and `host` is omitted, the exporter automatically falls back to the standard JDK system properties (`https.proxyHost`/`https.proxyPort` for HTTPS, or `http.proxyHost`/`http.proxyPort` for HTTP).

### Export Failure Logging

When span exports to Langfuse fail (for instance due to TLS handshake errors, network issues, or HTTP error responses), the dedicated exporter logs a concise warning at `WARN` level indicating the target URL and root cause (or HTTP status). The full exception stack trace is logged at `DEBUG` level to keep logs readable during outages.

### AI Span Filtering

When `export-target=ALL` (the default), only AI-related spans -- those carrying `gen_ai.*` [OpenTelemetry Semantic Convention](https://opentelemetry.io/docs/specs/semconv/gen-ai/) attributes -- and their ancestor spans are exported to Langfuse. This avoids cluttering your Langfuse dashboard with HTTP, database, or other infrastructure spans.

The `gen_ai.prompt` and `gen_ai.completion` attributes are automatically mapped to the Langfuse trace input and output, giving you immediate visibility into what was sent to and received from the LLM.

A span that sets a `langfuse.observation.*` attribute (for example `langfuse.observation.type=chain`) is also treated as an AI span, so you can have it typed in Langfuse without adding a made-up `gen_ai.*` attribute.

To export all spans instead, set:

```properties
quarkus.langfuse.otel.span-filter=ALL
```

### Quarkus LangChain4j Integration

When both `quarkus-opentelemetry` and `quarkus-langchain4j` are on the classpath, the extension automatically enables [LangChain4j prompt tracing](https://docs.quarkiverse.io/quarkus-langchain4j/dev/observability.html#_configure_langchain4j_prompt_tracing). Prompt content, LLM responses, tool arguments, and tool results are all included in the OpenTelemetry spans emitted by LangChain4j -- and therefore visible in Langfuse traces -- without any manual configuration.

These defaults are set at a low priority, so you can override any of them in `application.properties` or via environment variables.

### Zero-Config with DevServices

When DevServices is running, the OTel integration works out of the box with no configuration at all. The Langfuse base URL, public key, and secret key are all provided automatically.

### Disabling the Integration

To disable the OpenTelemetry integration entirely (for example, if you want to manage your own OTel exporters):

```properties
quarkus.langfuse.otel.enabled=false
```

When `quarkus-opentelemetry` is not on the classpath, the integration is automatically skipped.

## DevServices

In dev and test mode, the extension automatically starts a complete Langfuse stack using Testcontainers:

- **Langfuse server** - The main Langfuse application
- **PostgreSQL** - Primary database
- **ClickHouse** - Analytics database
- **Redis** - Cache
- **MinIO** - Object storage (for media/blob storage)
- **Worker** - Background job processing

No configuration is needed - just add the extension and run `quarkus dev`. The extension automatically configures `quarkus.langfuse.base-url`, `quarkus.langfuse.username`, and `quarkus.langfuse.password` to point to the DevServices instance.

The Langfuse server and worker images are pinned to the Langfuse release whose OpenAPI specification the client is generated from, so the DevServices server always serves the API the extension was built against. A weekly CI job moves both to newer Langfuse releases once the build passes against them. To run a different release, set `quarkus.langfuse.devservices.langfuse.image-version`. It applies to both the server and the worker, so they always stay on the same release. Set `image-name` on either one to use a custom image instead.

### DevServices Configuration

DevServices can be customized under `quarkus.langfuse.devservices`:

| Property | Type | Default | Description |
|---|---|---|---|
| `quarkus.langfuse.devservices.enabled` | `boolean` | `true` | Enable/disable DevServices |
| `quarkus.langfuse.devservices.shared` | `boolean` | `true` | Share containers across applications |
| `quarkus.langfuse.devservices.service-name` | `String` | `langfuse` | Label for container discovery |
| `quarkus.langfuse.devservices.langfuse.image-version` | `String` | _(Langfuse release the client is generated from)_ | Langfuse release shared by the server and worker images |
| `quarkus.langfuse.devservices.langfuse.image-name` | `String` | _(official image at `image-version`)_ | Langfuse server container image |
| `quarkus.langfuse.devservices.langfuse.port` | `int` | `8080` | Langfuse server port |
| `quarkus.langfuse.devservices.langfuse.username` | `String` | `quarkus` | Project public key |
| `quarkus.langfuse.devservices.langfuse.password` | `String` | `quarkuslangfuse` | Project secret key |
| `quarkus.langfuse.devservices.langfuse.startup-timeout` | `Duration` | `PT3M` | Container startup timeout |

The Langfuse server also has properties to configure the initial organization, project, and user. See the [full configuration reference](https://docs.quarkiverse.io/quarkus-langfuse/dev/index.html) for details.

## Dev UI

When running in dev mode (`quarkus dev`), the extension adds a **Langfuse** card to the Quarkus Dev UI. The card includes a link to the Langfuse web dashboard running in your DevServices container, allowing you to browse traces, manage prompts, view scores, and more directly from your development environment.

## Documentation

The full documentation is available at [https://docs.quarkiverse.io/quarkus-langfuse/dev/index.html](https://docs.quarkiverse.io/quarkus-langfuse/dev/index.html).

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://developers.redhat.com/author/eric-deandrea"><img src="https://avatars.githubusercontent.com/u/363447?v=4?s=100" width="100px;" alt="Eric Deandrea"/><br /><sub><b>Eric Deandrea</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-langfuse/commits?author=edeandrea" title="Code">💻</a> <a href="#maintenance-edeandrea" title="Maintenance">🚧</a> <a href="https://github.com/quarkiverse/quarkus-langfuse/commits?author=edeandrea" title="Tests">⚠️</a> <a href="#ideas-edeandrea" title="Ideas, Planning, & Feedback">🤔</a> <a href="#content-edeandrea" title="Content">🖋</a> <a href="https://github.com/quarkiverse/quarkus-langfuse/commits?author=edeandrea" title="Documentation">📖</a></td>
      <td align="center" valign="top" width="14.28%"><a href="http://bill.burkecentral.com"><img src="https://avatars.githubusercontent.com/u/704239?v=4?s=100" width="100px;" alt="Bill Burke"/><br /><sub><b>Bill Burke</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-langfuse/commits?author=patriot1burke" title="Code">💻</a> <a href="#maintenance-patriot1burke" title="Maintenance">🚧</a> <a href="#content-patriot1burke" title="Content">🖋</a> <a href="https://github.com/quarkiverse/quarkus-langfuse/commits?author=patriot1burke" title="Documentation">📖</a> <a href="#ideas-patriot1burke" title="Ideas, Planning, & Feedback">🤔</a></td>
      <td align="center" valign="top" width="14.28%"><a href="http://blog.omatheusmesmo.dev"><img src="https://avatars.githubusercontent.com/u/99829531?v=4?s=100" width="100px;" alt="Matheus Oliveira"/><br /><sub><b>Matheus Oliveira</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-langfuse/commits?author=omatheusmesmo" title="Code">💻</a> <a href="#design-omatheusmesmo" title="Design">🎨</a> <a href="#maintenance-omatheusmesmo" title="Maintenance">🚧</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
