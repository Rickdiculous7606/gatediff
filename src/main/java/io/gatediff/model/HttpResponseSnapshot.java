package io.gatediff.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Snapshot of an HTTP response received from a source or target API gateway.
 */
public class HttpResponseSnapshot {
    private final int statusCode;
    private final Map<String, List<String>> headers;
    private final String body;
    private final long durationMs;

    public HttpResponseSnapshot(int statusCode, Map<String, List<String>> headers, String body, long durationMs) {
        this.statusCode = statusCode;
        // Case-insensitive header map for convenient lookups
        Map<String, List<String>> caseInsensitive = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (headers != null) {
            caseInsensitive.putAll(headers);
        }
        this.headers = Collections.unmodifiableMap(caseInsensitive);
        this.body = body != null ? body : "";
        this.durationMs = durationMs;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public List<String> getHeader(String name) {
        return headers.get(name);
    }

    public String getFirstHeader(String name) {
        List<String> values = headers.get(name);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

    public String getBody() {
        return body;
    }

    public long getDurationMs() {
        return durationMs;
    }

    @Override
    public String toString() {
        return "HTTP " + statusCode + " (" + durationMs + "ms, body length " + body.length() + ")";
    }
}
