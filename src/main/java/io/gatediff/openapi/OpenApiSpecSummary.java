package io.gatediff.openapi;

import java.util.Collections;
import java.util.List;

/**
 * High-level summary of an OpenAPI 3.x specification.
 */
public class OpenApiSpecSummary {
    private final String title;
    private final String version;
    private final String description;
    private final List<OpenApiOperation> operations;

    public OpenApiSpecSummary(String title, String version, String description, List<OpenApiOperation> operations) {
        this.title = title != null ? title : "API";
        this.version = version != null ? version : "1.0.0";
        this.description = description != null ? description : "";
        this.operations = operations != null ? Collections.unmodifiableList(operations) : Collections.emptyList();
    }

    public String getTitle() {
        return title;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public List<OpenApiOperation> getOperations() {
        return operations;
    }

    public int getOperationCount() {
        return operations.size();
    }
}
