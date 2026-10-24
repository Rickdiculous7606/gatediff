package io.gatediff.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GateDiffConfig {
    @JsonProperty("version")
    private Integer version = 1;

    @JsonProperty("project")
    private ProjectConfig project = new ProjectConfig();

    @JsonProperty("defaults")
    private DefaultsConfig defaults = new DefaultsConfig();

    @JsonProperty("services")
    private List<ServiceConfig> services = new ArrayList<>();

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public ProjectConfig getProject() {
        return project != null ? project : new ProjectConfig();
    }

    public void setProject(ProjectConfig project) {
        this.project = project;
    }

    public DefaultsConfig getDefaults() {
        return defaults != null ? defaults : new DefaultsConfig();
    }

    public void setDefaults(DefaultsConfig defaults) {
        this.defaults = defaults;
    }

    public List<ServiceConfig> getServices() {
        return services != null ? services : new ArrayList<>();
    }

    public void setServices(List<ServiceConfig> services) {
        this.services = services;
    }
}
