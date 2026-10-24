package io.gatediff.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCaseConfig {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("method")
    private String method = "GET";

    @JsonProperty("path")
    private String path;

    @JsonProperty("headers")
    private Map<String, String> headers = new HashMap<>();

    @JsonProperty("body")
    private String body;

    @JsonProperty("expectedStatus")
    private Integer expectedStatus = 200;

    @JsonProperty("compare")
    private CompareConfig compare;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return (name != null && !name.isEmpty()) ? name : id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method != null ? method.toUpperCase() : "GET";
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getHeaders() {
        return headers != null ? headers : new HashMap<>();
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getExpectedStatus() {
        return expectedStatus != null ? expectedStatus : 200;
    }

    public void setExpectedStatus(Integer expectedStatus) {
        this.expectedStatus = expectedStatus;
    }

    public CompareConfig getCompare() {
        return compare;
    }

    public void setCompare(CompareConfig compare) {
        this.compare = compare;
    }
}
