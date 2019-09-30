package com.redhat.cajun.navy.process.tracing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import io.opentracing.propagation.TextMap;

public class ProcessVariablesMapExtractAdapter implements TextMap {

    private final Map<String, String> map = new HashMap<>();

    ProcessVariablesMapExtractAdapter(Map<String, Object> processVariables) {
        for (String key : processVariables.keySet()) {
            Object value = processVariables.get(key);
            map.put(key, value == null ? null : value.toString());
        }
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
        return map.entrySet().iterator();
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException(
                "HeadersMapExtractAdapter should only be used with Tracer.extract()");
    }
}
