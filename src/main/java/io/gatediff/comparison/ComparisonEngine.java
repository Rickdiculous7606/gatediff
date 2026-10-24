package io.gatediff.comparison;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.gatediff.model.ComparisonResult;
import io.gatediff.model.FieldMismatch;
import io.gatediff.model.HttpResponseSnapshot;

import java.util.*;

/**
 * Core response comparison engine for GateDiff.
 * <p>
 * Compares two HTTP responses (source vs target) with:
 * - HTTP status code check
 * - Case-insensitive configured header checks
 * - Semantics-aware JSON body tree comparison (insensitive to object key order and whitespace)
 * - Array element order preservation (by default order matters)
 * - Path-specific diffs with JSONPath notation (e.g., "$.status: expected ACTIVE but received INACTIVE")
 * - Ignored JSON paths (e.g., "$.requestId", "$.timestamp", or wildcards like "$.meta.*")
 * - Configurable additional fields detection (strict by default)
 * - Graceful handling of null values, malformed JSON, empty bodies, and non-JSON payloads.
 */
public class ComparisonEngine {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final boolean compareStatus;
    private final boolean compareJsonBody;
    private final boolean strictAdditionalFields;
    private final Set<String> ignoredJsonPaths;
    private final Set<String> headersToCompare;

    public ComparisonEngine() {
        this(true, true, true, Collections.emptySet(), Collections.emptySet());
    }

    public ComparisonEngine(
            boolean compareStatus,
            boolean compareJsonBody,
            boolean strictAdditionalFields,
            Set<String> ignoredJsonPaths,
            Set<String> headersToCompare) {
        this.compareStatus = compareStatus;
        this.compareJsonBody = compareJsonBody;
        this.strictAdditionalFields = strictAdditionalFields;
        this.ignoredJsonPaths = (ignoredJsonPaths != null) ? new HashSet<>(ignoredJsonPaths) : Collections.emptySet();

        Set<String> caseInsensitiveHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        if (headersToCompare != null) {
            caseInsensitiveHeaders.addAll(headersToCompare);
        }
        this.headersToCompare = Collections.unmodifiableSet(caseInsensitiveHeaders);
    }

    /**
     * Compares source and target HTTP responses according to configured rules.
     */
    public ComparisonResult compare(HttpResponseSnapshot source, HttpResponseSnapshot target) {
        Objects.requireNonNull(source, "Source response must not be null");
        Objects.requireNonNull(target, "Target response must not be null");

        List<FieldMismatch> mismatches = new ArrayList<>();

        // 1. Status Code Comparison
        if (compareStatus && source.getStatusCode() != target.getStatusCode()) {
            mismatches.add(new FieldMismatch(
                    FieldMismatch.Type.STATUS_MISMATCH,
                    "$.status",
                    source.getStatusCode(),
                    target.getStatusCode(),
                    "expected HTTP " + source.getStatusCode() + " but received HTTP " + target.getStatusCode()
            ));
        }

        // 2. Selected Headers Comparison
        for (String headerName : headersToCompare) {
            String sourceVal = source.getFirstHeader(headerName);
            String targetVal = target.getFirstHeader(headerName);

            if (sourceVal == null && targetVal != null) {
                mismatches.add(new FieldMismatch(
                        FieldMismatch.Type.HEADER_MISMATCH,
                        "header." + headerName.toLowerCase(),
                        null,
                        targetVal,
                        "header '" + headerName + "' expected to be absent but was present with value '" + targetVal + "'"
                ));
            } else if (sourceVal != null && targetVal == null) {
                mismatches.add(new FieldMismatch(
                        FieldMismatch.Type.HEADER_MISMATCH,
                        "header." + headerName.toLowerCase(),
                        sourceVal,
                        null,
                        "header '" + headerName + "' expected '" + sourceVal + "' but was missing"
                ));
            } else if (sourceVal != null && !valuesEqual(sourceVal, targetVal, headerName)) {
                mismatches.add(new FieldMismatch(
                        FieldMismatch.Type.HEADER_MISMATCH,
                        "header." + headerName.toLowerCase(),
                        sourceVal,
                        targetVal,
                        "header '" + headerName + "' expected '" + sourceVal + "' but received '" + targetVal + "'"
                ));
            }
        }

        // 3. Response Body Comparison
        if (compareJsonBody) {
            compareBodies(source.getBody(), target.getBody(), mismatches);
        }

        if (mismatches.isEmpty()) {
            return ComparisonResult.pass();
        } else {
            return ComparisonResult.fail(mismatches);
        }
    }

    private boolean valuesEqual(String v1, String v2, String headerName) {
        if ("content-type".equalsIgnoreCase(headerName)) {
            // Normalize media types (strip extra spaces in charset)
            return normalizeContentType(v1).equalsIgnoreCase(normalizeContentType(v2));
        }
        return v1.equals(v2);
    }

    private String normalizeContentType(String ct) {
        if (ct == null) return "";
        return ct.replaceAll("\\s*;\\s*", ";").trim();
    }

    private void compareBodies(String sourceBody, String targetBody, List<FieldMismatch> mismatches) {
        String sBody = (sourceBody == null) ? "" : sourceBody.trim();
        String tBody = (targetBody == null) ? "" : targetBody.trim();

        if (sBody.isEmpty() && tBody.isEmpty()) {
            return; // Both empty
        }

        if (sBody.isEmpty() && !tBody.isEmpty()) {
            mismatches.add(new FieldMismatch(
                    FieldMismatch.Type.VALUE_MISMATCH,
                    "$",
                    "<empty>",
                    tBody,
                    "expected empty body but received content"
            ));
            return;
        }

        if (!sBody.isEmpty() && tBody.isEmpty()) {
            mismatches.add(new FieldMismatch(
                    FieldMismatch.Type.VALUE_MISMATCH,
                    "$",
                    sBody,
                    "<empty>",
                    "expected body content but received empty body"
            ));
            return;
        }

        // Parse JSON
        JsonNode sourceNode = null;
        JsonNode targetNode = null;

        try {
            sourceNode = OBJECT_MAPPER.readTree(sBody);
        } catch (JsonProcessingException e) {
            // Source is not JSON
        }

        try {
            targetNode = OBJECT_MAPPER.readTree(tBody);
        } catch (JsonProcessingException e) {
            // Target is not JSON
        }

        if (sourceNode == null && targetNode == null) {
            // Neither is JSON -> Compare as raw text strings
            if (!sBody.equals(tBody)) {
                mismatches.add(new FieldMismatch(
                        FieldMismatch.Type.VALUE_MISMATCH,
                        "$",
                        sBody,
                        tBody,
                        "raw non-JSON bodies differ"
                ));
            }
            return;
        }

        if (sourceNode == null) {
            mismatches.add(new FieldMismatch(
                    FieldMismatch.Type.MALFORMED_JSON,
                    "$",
                    sBody,
                    targetNode.toString(),
                    "source response was malformed/non-JSON while target was JSON"
            ));
            return;
        }

        if (targetNode == null) {
            mismatches.add(new FieldMismatch(
                    FieldMismatch.Type.MALFORMED_JSON,
                    "$",
                    sourceNode.toString(),
                    tBody,
                    "target response was malformed/non-JSON while source was JSON"
            ));
            return;
        }

        // Both are valid JSON trees -> perform deep recursive comparison
        compareJsonNodes("$", sourceNode, targetNode, mismatches);
    }

    private void compareJsonNodes(String currentPath, JsonNode expected, JsonNode actual, List<FieldMismatch> mismatches) {
        if (isPathIgnored(currentPath)) {
            return;
        }

        // Check if types match
        if (expected.getNodeType() != actual.getNodeType()) {
            // Special case: numeric equality across int/float (e.g. 10 vs 10.0)
            if (expected.isNumber() && actual.isNumber()) {
                if (expected.asDouble() == actual.asDouble()) {
                    return;
                }
            }

            mismatches.add(new FieldMismatch(
                    FieldMismatch.Type.TYPE_MISMATCH,
                    currentPath,
                    expected.getNodeType().name(),
                    actual.getNodeType().name(),
                    "expected type " + expected.getNodeType() + " (" + expected.asText() + ") but received " + actual.getNodeType() + " (" + actual.asText() + ")"
            ));
            return;
        }

        // Object comparison
        if (expected.isObject()) {
            ObjectNode expectedObj = (ObjectNode) expected;
            ObjectNode actualObj = (ObjectNode) actual;

            // Check all expected fields in actual
            Iterator<Map.Entry<String, JsonNode>> expectedFields = expectedObj.fields();
            while (expectedFields.hasNext()) {
                Map.Entry<String, JsonNode> entry = expectedFields.next();
                String fieldName = entry.getKey();
                String fieldPath = appendPath(currentPath, fieldName);

                if (isPathIgnored(fieldPath)) {
                    continue;
                }

                if (!actualObj.has(fieldName)) {
                    mismatches.add(new FieldMismatch(
                            FieldMismatch.Type.MISSING_FIELD,
                            fieldPath,
                            entry.getValue().toString(),
                            null,
                            "missing expected field '" + fieldName + "'"
                    ));
                } else {
                    compareJsonNodes(fieldPath, entry.getValue(), actualObj.get(fieldName), mismatches);
                }
            }

            // Check for additional fields in actual if strict mode is enabled
            if (strictAdditionalFields) {
                Iterator<String> actualFieldNames = actualObj.fieldNames();
                while (actualFieldNames.hasNext()) {
                    String actualField = actualFieldNames.next();
                    String fieldPath = appendPath(currentPath, actualField);

                    if (isPathIgnored(fieldPath)) {
                        continue;
                    }

                    if (!expectedObj.has(actualField)) {
                        mismatches.add(new FieldMismatch(
                                FieldMismatch.Type.ADDITIONAL_FIELD,
                                fieldPath,
                                null,
                                actualObj.get(actualField).toString(),
                                "unexpected additional field '" + actualField + "' with value " + actualObj.get(actualField)
                        ));
                    }
                }
            }
            return;
        }

        // Array comparison: strict order by default
        if (expected.isArray()) {
            ArrayNode expectedArr = (ArrayNode) expected;
            ArrayNode actualArr = (ArrayNode) actual;

            int minLength = Math.min(expectedArr.size(), actualArr.size());

            for (int i = 0; i < minLength; i++) {
                String indexPath = currentPath + "[" + i + "]";
                if (!isPathIgnored(indexPath)) {
                    compareJsonNodes(indexPath, expectedArr.get(i), actualArr.get(i), mismatches);
                }
            }

            if (expectedArr.size() > actualArr.size()) {
                for (int i = minLength; i < expectedArr.size(); i++) {
                    String indexPath = currentPath + "[" + i + "]";
                    if (!isPathIgnored(indexPath)) {
                        mismatches.add(new FieldMismatch(
                                FieldMismatch.Type.MISSING_FIELD,
                                indexPath,
                                expectedArr.get(i).toString(),
                                null,
                                "array element missing at index " + i
                        ));
                    }
                }
            } else if (expectedArr.size() < actualArr.size() && strictAdditionalFields) {
                for (int i = minLength; i < actualArr.size(); i++) {
                    String indexPath = currentPath + "[" + i + "]";
                    if (!isPathIgnored(indexPath)) {
                        mismatches.add(new FieldMismatch(
                                FieldMismatch.Type.ADDITIONAL_FIELD,
                                indexPath,
                                null,
                                actualArr.get(i).toString(),
                                "unexpected additional array element at index " + i
                        ));
                    }
                }
            }
            return;
        }

        // Primitive values (string, number, boolean, null)
        if (!expected.equals(actual)) {
            // Null comparison check
            if (expected.isNull() || actual.isNull()) {
                mismatches.add(new FieldMismatch(
                        FieldMismatch.Type.VALUE_MISMATCH,
                        currentPath,
                        expected.asText(),
                        actual.asText(),
                        "expected " + expected + " but received " + actual
                ));
                return;
            }

            mismatches.add(new FieldMismatch(
                    FieldMismatch.Type.VALUE_MISMATCH,
                    currentPath,
                    expected.asText(),
                    actual.asText(),
                    "expected " + expected.asText() + " but received " + actual.asText()
            ));
        }
    }

    private String appendPath(String currentPath, String fieldName) {
        if ("$".equals(currentPath)) {
            return "$." + fieldName;
        }
        return currentPath + "." + fieldName;
    }

    private boolean isPathIgnored(String path) {
        if (ignoredJsonPaths.contains(path)) {
            return true;
        }
        // Support wildcards, e.g. "$.meta.*" or "$..timestamp"
        for (String pattern : ignoredJsonPaths) {
            if (pattern.endsWith(".*")) {
                String prefix = pattern.substring(0, pattern.length() - 2);
                if (path.startsWith(prefix)) {
                    return true;
                }
            } else if (pattern.contains("[*]")) {
                // e.g. "$.items[*].id" matches "$.items[0].id"
                String regex = pattern.replace(".", "\\.").replace("[*]", "\\[\\d+\\]");
                if (path.matches(regex)) {
                    return true;
                }
            }
        }
        return false;
    }
}
