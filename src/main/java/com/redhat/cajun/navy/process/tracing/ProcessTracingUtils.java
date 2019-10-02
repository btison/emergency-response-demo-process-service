package com.redhat.cajun.navy.process.tracing;

import java.util.Map;

import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;

public class ProcessTracingUtils {

    public static final String COMPONENT = "process-service";

    public static void injectInProcessInstance(SpanContext spanContext, Map<String, Object> processVariables,
                       Tracer tracer) {
        tracer.inject(spanContext, Format.Builtin.TEXT_MAP,
                new ProcessVariablesMapInjectAdapter(processVariables));
    }

    public static SpanContext extractSpanContext(Map<String, Object> processvariables, Tracer tracer) {
        return tracer.extract(Format.Builtin.TEXT_MAP, new ProcessVariablesMapExtractAdapter(processvariables));
    }

    public static Span buildChildSpan(String operation, Map<String, Object> processVariables, Tracer tracer) {
        SpanContext parentContext = extractSpanContext(processVariables, tracer);
        Tracer.SpanBuilder spanBuilder = tracer.buildSpan(operation)
                .withTag(Tags.SPAN_KIND, "process-instance.node");
        if (parentContext != null) {
            spanBuilder.addReference(References.FOLLOWS_FROM, parentContext);
        }
        return spanBuilder.start();
    }

}
