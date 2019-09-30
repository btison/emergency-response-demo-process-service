package com.redhat.cajun.navy.process.tracing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.spring.web.client.HttpHeadersCarrier;
import io.opentracing.contrib.spring.web.client.RestTemplateSpanDecorator;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class TracingRestTemplateInterceptor implements ClientHttpRequestInterceptor {
    private static final Log log = LogFactory.getLog(TracingRestTemplateInterceptor.class);

    private Tracer tracer;
    private List<RestTemplateSpanDecorator> spanDecorators;
    private Map<String, Object> variables;

    public TracingRestTemplateInterceptor(Map<String, Object> variables) {
        this(GlobalTracer.get(), Collections.<RestTemplateSpanDecorator>singletonList(
                new RestTemplateSpanDecorator.StandardTags()), variables);
    }

    public TracingRestTemplateInterceptor(Tracer tracer, Map<String, Object> variables) {
        this(tracer, Collections.<RestTemplateSpanDecorator>singletonList(
                new RestTemplateSpanDecorator.StandardTags()), variables);
    }

    public TracingRestTemplateInterceptor(Tracer tracer, List<RestTemplateSpanDecorator> spanDecorators, Map<String, Object> variables) {
        this.tracer = tracer;
        this.spanDecorators = new ArrayList<>(spanDecorators);
        this.variables = variables;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse httpResponse;

        SpanContext spanContext = ProcessTracingUtils.extractSpanContext(variables, tracer);

        Span span = tracer.buildSpan(httpRequest.getMethod().toString())
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .asChildOf(spanContext)
                .start();

        tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS,
                new HttpHeadersCarrier(httpRequest.getHeaders()));

        for (RestTemplateSpanDecorator spanDecorator : spanDecorators) {
            try {
                spanDecorator.onRequest(httpRequest, span);
            } catch (RuntimeException exDecorator) {
                log.error("Exception during decorating span", exDecorator);
            }
        }

        try (Scope scope = tracer.activateSpan(span)) {
            httpResponse = execution.execute(httpRequest, body);
            for (RestTemplateSpanDecorator spanDecorator : spanDecorators) {
                try {
                    spanDecorator.onResponse(httpRequest, httpResponse, span);
                } catch (RuntimeException exDecorator) {
                    log.error("Exception during decorating span", exDecorator);
                }
            }
        } catch (Exception ex) {
            for (RestTemplateSpanDecorator spanDecorator : spanDecorators) {
                try {
                    spanDecorator.onError(httpRequest, ex, span);
                } catch (RuntimeException exDecorator) {
                    log.error("Exception during decorating span", exDecorator);
                }
            }
            throw ex;
        } finally {
            span.finish();
        }

        return httpResponse;
    }
}
