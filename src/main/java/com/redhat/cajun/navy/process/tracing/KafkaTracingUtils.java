package com.redhat.cajun.navy.process.tracing;

import java.util.Map;

import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;

public class KafkaTracingUtils {

    public static final String COMPONENT = "process-service";

    public static Span buildChildSpan(String operationName, Map<String, Object> headers, Tracer tracer) {

        SpanContext parentContext = extractSpanContext(headers, tracer);

        System.out.println("SpanContext = " + parentContext);

        Tracer.SpanBuilder spanBuilder = spanBuilder(operationName, tracer);
        if (parentContext != null) {
            spanBuilder.addReference(References.CHILD_OF, parentContext);
        }
        return spanBuilder.start();
    }

    private static Tracer.SpanBuilder spanBuilder(String operationName, Tracer tracer) {
        return tracer.buildSpan(operationName).ignoreActiveSpan()
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER)
                .withTag(Tags.COMPONENT.getKey(), COMPONENT);
    }

    private static SpanContext extractSpanContext(Map<String, Object> headers, Tracer tracer) {
        return tracer
                .extract(Format.Builtin.TEXT_MAP, new HeadersMapExtractAdapter(headers));
    }

    public static void injectInProcessInstance(SpanContext spanContext, Map<String, Object> processVariables,
                       Tracer tracer) {
        tracer.inject(spanContext, Format.Builtin.TEXT_MAP,
                new ProcessVariablesMapInjectAdapter(processVariables));
    }
}
