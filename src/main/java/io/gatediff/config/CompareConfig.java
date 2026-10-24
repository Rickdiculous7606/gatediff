package io.gatediff.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompareConfig {
    @JsonProperty("status")
    private Boolean status = true;

    @JsonProperty("jsonBody")
    private Boolean jsonBody = true;

    @JsonProperty("additionalFields")
    private Boolean additionalFields = true;

    @JsonProperty("ignoredJsonPaths")
    private List<String> ignoredJsonPaths = new ArrayList<>();

    @JsonProperty("headers")
    private List<String> headers = new ArrayList<>();

    public Boolean getStatus() {
        return status != null ? status : true;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Boolean getJsonBody() {
        return jsonBody != null ? jsonBody : true;
    }

    public void setJsonBody(Boolean jsonBody) {
        this.jsonBody = jsonBody;
    }

    public Boolean getAdditionalFields() {
        return additionalFields != null ? additionalFields : true;
    }

    public void setAdditionalFields(Boolean additionalFields) {
        this.additionalFields = additionalFields;
    }

    public List<String> getIgnoredJsonPaths() {
        return ignoredJsonPaths != null ? ignoredJsonPaths : new ArrayList<>();
    }

    public void setIgnoredJsonPaths(List<String> ignoredJsonPaths) {
        this.ignoredJsonPaths = ignoredJsonPaths;
    }

    public List<String> getHeaders() {
        return headers != null ? headers : new ArrayList<>();
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    /**
     * Merges parent defaults with child overrides.
     */
    public CompareConfig mergeWith(CompareConfig parent) {
        if (parent == null) return this;
        CompareConfig merged = new CompareConfig();
        merged.setStatus(this.status != null ? this.status : parent.getStatus());
        merged.setJsonBody(this.jsonBody != null ? this.jsonBody : parent.getJsonBody());
        merged.setAdditionalFields(this.additionalFields != null ? this.additionalFields : parent.getAdditionalFields());

        List<String> mergedIgnored = new ArrayList<>(parent.getIgnoredJsonPaths());
        if (this.ignoredJsonPaths != null) {
            for (String path : this.ignoredJsonPaths) {
                if (!mergedIgnored.contains(path)) {
                    mergedIgnored.add(path);
                }
            }
        }
        merged.setIgnoredJsonPaths(mergedIgnored);

        List<String> mergedHeaders = new ArrayList<>(parent.getHeaders());
        if (this.headers != null) {
            for (String h : this.headers) {
                if (!mergedHeaders.contains(h)) {
                    mergedHeaders.add(h);
                }
            }
        }
        merged.setHeaders(mergedHeaders);

        return merged;
    }
}
