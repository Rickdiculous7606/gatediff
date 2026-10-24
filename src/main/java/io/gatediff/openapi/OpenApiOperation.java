package io.gatediff.openapi;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracted representation of an operation defined in an OpenAPI 3.x document.
 */
public class OpenApiOperation {
    private final String method;
    private final String path;
    private final String operationId;
    private final String summary;
    private final List<String> parameters;
    private final List<String> responseCodes;

    public OpenApiOperation(String method, String path, String operationId, String summary, List<String> parameters, List<String> responseCodes) {
        this.method = method != null ? method.toUpperCase() : "GET";
        this.path = path != null ? path : "/";
        this.operationId = operationId != null ? operationId : "";
        this.summary = summary != null ? summary : "";
        this.parameters = parameters != null ? parameters : new ArrayList<>();
        this.responseCodes = responseCodes != null ? responseCodes : new ArrayList<>();
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public List<String> getResponseCodes() {
        return responseCodes;
    }

    @Override
    public String toString() {
        return method + " " + path + (!operationId.isEmpty() ? " [" + operationId + "]" : "");
    }
}
