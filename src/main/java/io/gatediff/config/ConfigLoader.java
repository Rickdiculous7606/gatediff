package io.gatediff.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

/**
 * Loads GateDiff YAML configuration files using Jackson.
 */
public class ConfigLoader {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    public static GateDiffConfig load(File configFile) throws IOException {
        if (!configFile.exists()) {
            throw new IOException("Configuration file not found: " + configFile.getAbsolutePath());
        }
        return YAML_MAPPER.readValue(configFile, GateDiffConfig.class);
    }

    public static GateDiffConfig fromString(String yamlContent) throws IOException {
        return YAML_MAPPER.readValue(yamlContent, GateDiffConfig.class);
    }
}
