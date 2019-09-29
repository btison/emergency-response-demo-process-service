package com.redhat.cajun.navy.process.tracing;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import io.opentracing.propagation.TextMap;

public class ProcessVariablesMapInjectAdapter implements TextMap {

    private final Map<String, Object> processVariables;

    ProcessVariablesMapInjectAdapter(Map<String, Object> processVariables) {
        this.processVariables = processVariables;
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("iterator should never be used with Tracer.inject()");
    }

    @Override
    public void put(String key, String value) {
        processVariables.put(key, value);
    }
}
