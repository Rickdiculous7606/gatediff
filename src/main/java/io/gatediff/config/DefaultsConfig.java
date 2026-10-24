package io.gatediff.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultsConfig {
    @JsonProperty("timeoutMs")
    private Integer timeoutMs = 5000;

    @JsonProperty("retries")
    private Integer retries = 1;

    @JsonProperty("compare")
    private CompareConfig compare = new CompareConfig();

    public Integer getTimeoutMs() {
        return timeoutMs != null ? timeoutMs : 5000;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public Integer getRetries() {
        return retries != null ? retries : 1;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public CompareConfig getCompare() {
        return compare != null ? compare : new CompareConfig();
    }

    public void setCompare(CompareConfig compare) {
        this.compare = compare;
    }
}
