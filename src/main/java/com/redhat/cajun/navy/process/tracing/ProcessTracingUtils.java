package com.redhat.cajun.navy.process.tracing;

import java.util.Map;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

public class ProcessTracingUtils {

    public static final String COMPONENT = "process-service";

    public static void injectInProcessInstance(SpanContext spanContext, Map<String, Object> processVariables,
                       Tracer tracer) {
        tracer.inject(spanContext, Format.Builtin.TEXT_MAP,
                new ProcessVariablesMapInjectAdapter(processVariables));
    }

}
