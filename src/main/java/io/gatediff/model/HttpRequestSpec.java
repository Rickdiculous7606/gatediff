package io.gatediff.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Specification of an HTTP request to be executed against an API gateway.
 */
public class HttpRequestSpec {
    private final String method;
    private final String path;
    private final Map<String, List<String>> headers;
    private final String body;

    public HttpRequestSpec(String method, String path, Map<String, List<String>> headers, String body) {
        this.method = Objects.requireNonNull(method, "HTTP method must not be null").toUpperCase();
        this.path = Objects.requireNonNull(path, "Path must not be null");
        this.headers = headers != null ? Collections.unmodifiableMap(headers) : Collections.emptyMap();
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return method + " " + path;
    }
}
