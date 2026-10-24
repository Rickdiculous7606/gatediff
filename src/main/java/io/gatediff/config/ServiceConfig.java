package io.gatediff.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceConfig {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("sourceBaseUrl")
    private String sourceBaseUrl;

    @JsonProperty("targetBaseUrl")
    private String targetBaseUrl;

    @JsonProperty("openApiSpec")
    private String openApiSpec;

    @JsonProperty("headers")
    private Map<String, String> headers = new HashMap<>();

    @JsonProperty("compare")
    private CompareConfig compare;

    @JsonProperty("tests")
    private List<TestCaseConfig> tests = new ArrayList<>();

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

    public String getSourceBaseUrl() {
        return sourceBaseUrl;
    }

    public void setSourceBaseUrl(String sourceBaseUrl) {
        this.sourceBaseUrl = sourceBaseUrl;
    }

    public String getTargetBaseUrl() {
        return targetBaseUrl;
    }

    public void setTargetBaseUrl(String targetBaseUrl) {
        this.targetBaseUrl = targetBaseUrl;
    }

    public String getOpenApiSpec() {
        return openApiSpec;
    }

    public void setOpenApiSpec(String openApiSpec) {
        this.openApiSpec = openApiSpec;
    }

    public Map<String, String> getHeaders() {
        return headers != null ? headers : new HashMap<>();
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public CompareConfig getCompare() {
        return compare;
    }

    public void setCompare(CompareConfig compare) {
        this.compare = compare;
    }

    public List<TestCaseConfig> getTests() {
        return tests != null ? tests : new ArrayList<>();
    }

    public void setTests(List<TestCaseConfig> tests) {
        this.tests = tests;
    }
}
