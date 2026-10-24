package io.gatediff.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Robust, lightweight OpenAPI 3.x parser supporting both YAML and JSON format.
 * Extracts service metadata, paths, operations, query/path parameters, and response status codes.
 */
public class OpenApiParser {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final Set<String> HTTP_METHODS = Set.of("get", "post", "put", "patch", "delete", "options", "head");

    public static OpenApiSpecSummary parse(File specFile) throws IOException {
        if (!specFile.exists()) {
            throw new IOException("OpenAPI specification file not found: " + specFile.getAbsolutePath());
        }

        String name = specFile.getName().toLowerCase();
        ObjectMapper mapper = (name.endsWith(".yaml") || name.endsWith(".yml")) ? YAML_MAPPER : JSON_MAPPER;
        JsonNode root = mapper.readTree(specFile);

        return parseJsonNode(root);
    }

    public static OpenApiSpecSummary parseString(String content, boolean isYaml) throws IOException {
        ObjectMapper mapper = isYaml ? YAML_MAPPER : JSON_MAPPER;
        JsonNode root = mapper.readTree(content);
        return parseJsonNode(root);
    }

    private static OpenApiSpecSummary parseJsonNode(JsonNode root) {
        String title = "API";
        String version = "1.0.0";
        String description = "";

        if (root.has("info")) {
            JsonNode info = root.get("info");
            if (info.has("title")) title = info.get("title").asText();
            if (info.has("version")) version = info.get("version").asText();
            if (info.has("description")) description = info.get("description").asText();
        }

        List<OpenApiOperation> operations = new ArrayList<>();

        if (root.has("paths") && root.get("paths").isObject()) {
            JsonNode paths = root.get("paths");
            Iterator<Map.Entry<String, JsonNode>> pathEntries = paths.fields();

            while (pathEntries.hasNext()) {
                Map.Entry<String, JsonNode> pathEntry = pathEntries.next();
                String pathStr = pathEntry.getKey();
                JsonNode pathItem = pathEntry.getValue();

                if (!pathItem.isObject()) continue;

                Iterator<Map.Entry<String, JsonNode>> opEntries = pathItem.fields();
                while (opEntries.hasNext()) {
                    Map.Entry<String, JsonNode> opEntry = opEntries.next();
                    String method = opEntry.getKey().toLowerCase();

                    if (!HTTP_METHODS.contains(method)) continue;

                    JsonNode opNode = opEntry.getValue();
                    String opId = opNode.has("operationId") ? opNode.get("operationId").asText() : "";
                    String summary = opNode.has("summary") ? opNode.get("summary").asText() : "";

                    List<String> parameters = new ArrayList<>();
                    if (opNode.has("parameters") && opNode.get("parameters").isArray()) {
                        for (JsonNode p : opNode.get("parameters")) {
                            if (p.has("name")) {
                                String in = p.has("in") ? p.get("in").asText() : "query";
                                parameters.add(in + ":" + p.get("name").asText());
                            }
                        }
                    }

                    List<String> responseCodes = new ArrayList<>();
                    if (opNode.has("responses") && opNode.get("responses").isObject()) {
                        Iterator<String> codes = opNode.get("responses").fieldNames();
                        while (codes.hasNext()) {
                            responseCodes.add(codes.next());
                        }
                    }

                    operations.add(new OpenApiOperation(method, pathStr, opId, summary, parameters, responseCodes));
                }
            }
        }

        return new OpenApiSpecSummary(title, version, description, operations);
    }
}
