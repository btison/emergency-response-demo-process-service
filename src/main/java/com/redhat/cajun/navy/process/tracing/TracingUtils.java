package com.redhat.cajun.navy.process.tracing;

import java.util.Map;

import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;

public class TracingUtils {

    public static final String COMPONENT = "process-service";

    public static Scope buildChildSpan(String operationName, Map<String, Object> headers, Tracer tracer) {

        SpanContext spanContext = extractSpanContext(headers, tracer);

        System.out.println("SpanContext = " + spanContext);

        if (spanContext != null) {
            Span span = spanBuilder(operationName, tracer).addReference(References.CHILD_OF, spanContext).start();
            return tracer.scopeManager().activate(span);
        }
        return null;
    }

    private static Tracer.SpanBuilder spanBuilder(String operationName, Tracer tracer) {
        return  tracer.buildSpan(operationName).ignoreActiveSpan()
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER)
                .withTag(Tags.COMPONENT.getKey(), COMPONENT);
    }

    public static SpanContext extractSpanContext(Map<String, Object> headers, Tracer tracer) {
        return tracer
                .extract(Format.Builtin.TEXT_MAP, new HeadersMapExtractAdapter(headers));
    }
}
