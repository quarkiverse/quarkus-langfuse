[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.langfuse/quarkus-langfuse?logo=apache-maven&style=flat-square)](https://central.sonatype.com/artifact/io.quarkiverse/langfuse/quarkus-langfuse-parent)

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-2-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

# Quarkus Langfuse

A Quarkus extension for [Langfuse](https://langfuse.com), the open-source LLM engineering platform. This extension provides a type-safe REST client (based on the [Langfuse Java SDK](https://github.com/langfuse/langfuse-java)), DevServices for zero-config local development, and a Dev UI card for quick access to the Langfuse dashboard.

[Langfuse](https://langfuse.com) helps you ship AI Agents and Products from prototype to production and beyond. It provides observability (traces, sessions, scores), prompt management (versioning, deployment, A/B testing), and evaluation (datasets, experiments, LLM-as-a-judge) for LLM-powered applications.

## Features

- **Type-safe Langfuse API client** - Synchronous and asynchronous clients covering the full [Langfuse public API](https://langfuse.com/docs) (traces, ingestion, prompts, scores, datasets, observations, sessions, and more), built on the Quarkus REST Client Reactive.
- **CDI integration** - Inject `LangfuseApi` directly into your beans. No Quarkus-specific types needed.
- **DevServices** - Automatically starts a complete Langfuse stack (Langfuse server, PostgreSQL, ClickHouse, Redis, MinIO, and Worker) in dev and test mode using Testcontainers. No manual setup required.
- **Dev UI** - Provides a Dev UI card with a direct link to the Langfuse dashboard running in your local DevServices instance.
- **Native image support** - Compatible with GraalVM native image compilation.
- **OpenTelemetry integration** - When `quarkus-opentelemetry` is on the classpath, automatically exports AI-related spans to Langfuse. Zero configuration needed with DevServices.

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
quarkus.langfuse.username=pk-lf-...
quarkus.langfuse.password=sk-lf-...
```

> **Note:** When DevServices is enabled (the default in dev/test mode), `base-url`, `username`, and `password` are automatically configured for you. You only need to set these properties when connecting to an external Langfuse instance or in production.

### Configuration Reference

| Property | Type | Default | Description |
|---|---|---|---|
| `quarkus.langfuse.base-url` | `String` | _required_ | Base URL of the Langfuse server |
| `quarkus.langfuse.username` | `String` | _required_ | Langfuse project public key |
| `quarkus.langfuse.password` | `String` | _required_ | Langfuse project secret key |
| `quarkus.langfuse.timeout` | `Duration` | `1m` | Default timeout for Langfuse API calls |
| `quarkus.langfuse.connect-timeout` | `Duration` | `${quarkus.langfuse.timeout}` | Timeout to establish a connection |
| `quarkus.langfuse.read-timeout` | `Duration` | `${quarkus.langfuse.timeout}` | Timeout for receiving a response |
| `quarkus.langfuse.log-requests` | `boolean` | `false` | Log outgoing requests |
| `quarkus.langfuse.log-responses` | `boolean` | `false` | Log incoming responses |
| `quarkus.langfuse.pretty-print` | `boolean` | `false` | Pretty-print JSON bodies in logs |
| `quarkus.langfuse.otel.enabled` | `boolean` | `true` | Enable/disable OpenTelemetry integration (build time, defaults to `quarkus.otel.enabled`) |
| `quarkus.langfuse.otel.export-target` | `String` | `ALL` | Export target (build time): `ALL` (Langfuse + other OTLP backends) or `LANGFUSE_ONLY` |
| `quarkus.langfuse.otel.trace-ingestion-url` | `String` | `<base-url>/api/public/otel/v1/traces` | Override the OTLP trace ingestion endpoint (applies when `export-target=ALL`) |
| `quarkus.langfuse.otel.span-filter` | `String` | `AI_ONLY` | Span filter: `AI_ONLY` (AI spans + ancestors) or `ALL` (applies when `export-target=ALL`) |

## Usage

Simply inject `LangfuseApi` into your CDI beans. The extension automatically configures and provides the implementation:

```java
import com.langfuse.api.LangfuseApi;

@ApplicationScoped
public class MyService {

    @Inject
    LangfuseApi langfuseApi;

    public void listTraces() {
        // Synchronous call
        var traces = langfuseApi.trace().traceList(/* parameters */);

        // Asynchronous call
        var asyncTraces = langfuseApi.asyncTrace().traceList(/* parameters */);
    }

    public void listPrompts() {
        var prompts = langfuseApi.prompts().promptsList(/* parameters */);
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
| `trace()` | Create, list, and manage traces |
| `observations()` | Query observations (spans and generations) |
| `ingestion()` | Batch ingest traces, spans, generations, scores, and events |
| `prompts()` | Manage, version, and retrieve prompts |
| `promptVersion()` | Manage individual prompt versions |
| `scores()` | Create and query evaluation scores |
| `scoreConfigs()` | Manage score configurations |
| `sessions()` | List and query sessions |
| `datasets()` | Create and manage datasets |
| `datasetItems()` | Manage dataset items |
| `datasetRunItems()` | Manage dataset run items |
| `models()` | Manage model definitions and pricing |
| `projects()` | Query projects |
| `organizations()` | Query organizations |
| `comments()` | Manage comments on traces |
| `media()` | Upload and manage media attachments |
| `metrics()` | Query usage metrics (cost, token counts, latency) |
| `opentelemetry()` | OpenTelemetry trace ingestion |
| `annotationQueues()` | Manage annotation queues for human review |
| `llmConnections()` | Manage LLM provider connections |

## OpenTelemetry Integration

When the `quarkus-opentelemetry` extension is on the classpath, the Langfuse extension automatically exports OpenTelemetry span data to Langfuse. No additional configuration is needed -- authentication is derived from your existing `quarkus.langfuse.username` and `quarkus.langfuse.password` settings.

### Export Target

The `quarkus.langfuse.otel.export-target` property (build time) controls how spans are exported:

- **`ALL`** (default) -- The extension registers a dedicated Langfuse span processor that runs alongside any other OpenTelemetry exporters you have configured. Spans are exported to both Langfuse and your standard OTLP backend (Jaeger, Zipkin, or any other collector). In this mode, only AI-related spans are sent to Langfuse by default (see [AI Span Filtering](#ai-span-filtering) below), and the `gen_ai.prompt` / `gen_ai.completion` attributes are automatically mapped to the Langfuse trace input and output.

- **`LANGFUSE_ONLY`** -- The standard OpenTelemetry OTLP exporter is configured to send directly to Langfuse. No separate span processor is registered and no additional OTLP backend receives spans. All spans are exported to Langfuse without filtering. This is equivalent to the [manual configuration described in the Quarkus LangChain4j docs](https://docs.quarkiverse.io/quarkus-langchain4j/dev/observability.html#_manual_configuration_without_quarkus_langfuse), but handled automatically by the extension.

```properties
quarkus.langfuse.otel.export-target=LANGFUSE_ONLY
```

### AI Span Filtering

When `export-target=ALL` (the default), only AI-related spans -- those carrying `gen_ai.*` [OpenTelemetry Semantic Convention](https://opentelemetry.io/docs/specs/semconv/gen-ai/) attributes -- and their ancestor spans are exported to Langfuse. This avoids cluttering your Langfuse dashboard with HTTP, database, or other infrastructure spans.

The `gen_ai.prompt` and `gen_ai.completion` attributes are automatically mapped to the Langfuse trace input and output, giving you immediate visibility into what was sent to and received from the LLM.

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

### DevServices Configuration

DevServices can be customized under `quarkus.langfuse.devservices`:

| Property | Type | Default | Description |
|---|---|---|---|
| `quarkus.langfuse.devservices.enabled` | `boolean` | `true` | Enable/disable DevServices |
| `quarkus.langfuse.devservices.shared` | `boolean` | `true` | Share containers across applications |
| `quarkus.langfuse.devservices.service-name` | `String` | `langfuse` | Label for container discovery |
| `quarkus.langfuse.devservices.langfuse.image-name` | `String` | _(Langfuse default)_ | Langfuse server container image |
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
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!