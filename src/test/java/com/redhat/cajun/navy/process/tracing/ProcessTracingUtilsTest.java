package com.redhat.cajun.navy.process.tracing;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;

import io.jaegertracing.internal.JaegerSpan;
import io.jaegertracing.internal.JaegerSpanContext;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.metrics.InMemoryMetricsFactory;
import io.jaegertracing.internal.metrics.Metrics;
import io.jaegertracing.internal.reporters.InMemoryReporter;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.spi.MetricsFactory;
import io.opentracing.References;
import io.opentracing.tag.Tags;
import org.hamcrest.core.StringEndsWith;
import org.hamcrest.core.StringStartsWith;
import org.junit.Before;
import org.junit.Test;

public class ProcessTracingUtilsTest {

    private JaegerTracer tracer;

    private MetricsFactory metricsFactory;

    @Before
    public void setup() {
        metricsFactory = new InMemoryMetricsFactory();
        tracer = new JaegerTracer.Builder("TracerTestService")
                .withReporter(new InMemoryReporter())
                .withSampler(new ConstSampler(true))
                .withMetrics(new Metrics(metricsFactory))
                .build();
    }

    @Test
    public void testInjectInProcessInstance() {

        JaegerSpanContext spanContext = new JaegerSpanContext(0L, 255L, 221L, 204L, (byte) 4);

        JaegerSpan span = tracer.buildSpan("test")
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER)
                .withTag(Tags.COMPONENT.getKey(), "testComponent")
                .addReference(References.CHILD_OF, spanContext)
                .start();

        Map<String, Object> variables = new HashMap<>();

        ProcessTracingUtils.injectInProcessInstance(span.context(), variables, tracer);
        assertThat(variables.get("uber-trace-id"), notNullValue());
        assertThat(variables.get("uber-trace-id"), instanceOf(String.class));
        String spanAsString = (String)variables.get("uber-trace-id");
        assertThat(spanAsString, StringStartsWith.startsWith("ff:"));
        assertThat(spanAsString, StringEndsWith.endsWith(":dd:4"));
    }

}
