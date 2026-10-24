package io.gatediff.mapping;

public enum MappingStatus {
    /**
     * Confirmed exact match (method and normalized path or operationId match with high confidence).
     */
    CONFIRMED,

    /**
     * Suggested potential match based on path similarity or operation naming.
     */
    SUGGESTED,

    /**
     * Ambiguous match: multiple target operations matched this source operation.
     * GateDiff intentionally flags this for human review rather than silently guessing.
     */
    AMBIGUOUS,

    /**
     * No matching operation found in target API.
     */
    UNRESOLVED
}
