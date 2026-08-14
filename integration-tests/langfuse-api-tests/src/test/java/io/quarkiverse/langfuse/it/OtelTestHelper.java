package io.quarkiverse.langfuse.it;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.langfuse.api.LangfuseApi;
import com.langfuse.api.model.OpentelemetryExportTracesRequest;
import com.langfuse.api.model.OtelAttribute;
import com.langfuse.api.model.OtelAttributeValue;
import com.langfuse.api.model.OtelResourceSpan;
import com.langfuse.api.model.OtelScopeSpan;
import com.langfuse.api.model.OtelSpan;
import com.langfuse.api.opentelemetry.OpentelemetryApi;

final class OtelTestHelper {

    private OtelTestHelper() {
    }

    static void ingestTrace(LangfuseApi client, String traceId, String traceName) {
        var span = buildRootSpan(traceId, traceName, List.of());
        client.opentelemetry().opentelemetryExportTraces(
                buildApiRequest(List.of(span)));
    }

    static void ingestTrace(LangfuseApi client, String traceId, String spanId, String traceName) {
        var span = buildRootSpan(traceId, traceName, spanId, List.of());
        client.opentelemetry().opentelemetryExportTraces(
                buildApiRequest(List.of(span)));
    }

    static CompletionStage<Object> ingestTraceAsync(LangfuseApi client, String traceId, String traceName) {
        var span = buildRootSpan(traceId, traceName, List.of());
        return client.asyncOpentelemetry().opentelemetryExportTraces(
                buildApiRequest(List.of(span)));
    }

    static CompletionStage<Object> ingestTraceAsync(LangfuseApi client, String traceId, String spanId,
            String traceName) {
        var span = buildRootSpan(traceId, traceName, spanId, List.of());
        return client.asyncOpentelemetry().opentelemetryExportTraces(
                buildApiRequest(List.of(span)));
    }

    static void ingestTraceWithSpan(LangfuseApi client, String traceId, String traceName, String childSpanName) {
        var rootSpanId = generateSpanId();
        var rootSpan = buildRootSpan(traceId, traceName, rootSpanId, List.of());
        var childSpan = buildChildSpan(traceId, rootSpanId, childSpanName);
        client.opentelemetry().opentelemetryExportTraces(
                buildApiRequest(List.of(rootSpan, childSpan)));
    }

    static CompletionStage<Object> ingestTraceWithSpanAsync(LangfuseApi client, String traceId, String traceName,
            String childSpanName) {
        var rootSpanId = generateSpanId();
        var rootSpan = buildRootSpan(traceId, traceName, rootSpanId, List.of());
        var childSpan = buildChildSpan(traceId, rootSpanId, childSpanName);
        return client.asyncOpentelemetry().opentelemetryExportTraces(
                buildApiRequest(List.of(rootSpan, childSpan)));
    }

    static void ingestTraceWithSession(LangfuseApi client, String traceId, String traceName, String sessionId) {
        var extraAttributes = List.of(
                OtelAttribute.builder()
                        .key("langfuse.session.id")
                        .value(OtelAttributeValue.builder().stringValue(sessionId).build())
                        .build());
        var span = buildRootSpan(traceId, traceName, extraAttributes);
        client.opentelemetry().opentelemetryExportTraces(
                buildApiRequest(List.of(span)));
    }

    static CompletionStage<Object> ingestTraceWithSessionAsync(LangfuseApi client, String traceId, String traceName,
            String sessionId) {
        var extraAttributes = List.of(
                OtelAttribute.builder()
                        .key("langfuse.session.id")
                        .value(OtelAttributeValue.builder().stringValue(sessionId).build())
                        .build());
        var span = buildRootSpan(traceId, traceName, extraAttributes);
        return client.asyncOpentelemetry().opentelemetryExportTraces(
                buildApiRequest(List.of(span)));
    }

    private static OtelSpan buildRootSpan(String traceId, String traceName, List<OtelAttribute> extraAttributes) {
        return buildRootSpan(traceId, traceName, generateSpanId(), extraAttributes);
    }

    private static OtelSpan buildRootSpan(String traceId, String traceName, String spanId,
            List<OtelAttribute> extraAttributes) {
        var now = String.valueOf(System.currentTimeMillis() * 1_000_000L);
        var attributes = new ArrayList<OtelAttribute>();
        attributes.add(OtelAttribute.builder()
                .key("langfuse.trace.id")
                .value(OtelAttributeValue.builder().stringValue(traceId).build())
                .build());
        attributes.addAll(extraAttributes);

        return OtelSpan.builder()
                .traceId(toHexTraceId(traceId))
                .spanId(spanId)
                .name(traceName)
                .kind(1)
                .startTimeUnixNano(now)
                .endTimeUnixNano(now)
                .attributes(attributes)
                .build();
    }

    private static OtelSpan buildChildSpan(String traceId, String parentSpanId, String spanName) {
        var now = String.valueOf(System.currentTimeMillis() * 1_000_000L);
        return OtelSpan.builder()
                .traceId(toHexTraceId(traceId))
                .spanId(generateSpanId())
                .parentSpanId(parentSpanId)
                .name(spanName)
                .kind(1)
                .startTimeUnixNano(now)
                .endTimeUnixNano(now)
                .build();
    }

    private static OpentelemetryApi.APIOpentelemetryExportTracesRequest buildApiRequest(List<OtelSpan> spans) {
        var request = OpentelemetryExportTracesRequest.builder()
                .resourceSpans(List.of(
                        OtelResourceSpan.builder()
                                .scopeSpans(List.of(
                                        OtelScopeSpan.builder()
                                                .spans(spans)
                                                .build()))
                                .build()))
                .build();

        return OpentelemetryApi.APIOpentelemetryExportTracesRequest.newBuilder()
                .opentelemetryExportTracesRequest(request)
                .build();
    }

    private static String toHexTraceId(String uuid) {
        return uuid.replace("-", "");
    }

    private static String generateSpanId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
