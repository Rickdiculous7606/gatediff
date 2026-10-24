package io.gatediff.mapping;

import io.gatediff.openapi.OpenApiOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapping relation between a source API operation and target API operation(s).
 */
public class OperationMapping {
    private final OpenApiOperation sourceOperation;
    private final OpenApiOperation targetOperation;
    private final List<OpenApiOperation> candidateOperations;
    private final MappingStatus status;
    private final int confidenceScore; // 0 to 100
    private final String reason;

    public OperationMapping(
            OpenApiOperation sourceOperation,
            OpenApiOperation targetOperation,
            List<OpenApiOperation> candidateOperations,
            MappingStatus status,
            int confidenceScore,
            String reason) {
        this.sourceOperation = sourceOperation;
        this.targetOperation = targetOperation;
        this.candidateOperations = candidateOperations != null ? candidateOperations : new ArrayList<>();
        this.status = status;
        this.confidenceScore = confidenceScore;
        this.reason = reason;
    }

    public static OperationMapping confirmed(OpenApiOperation source, OpenApiOperation target, String reason) {
        return new OperationMapping(source, target, List.of(target), MappingStatus.CONFIRMED, 100, reason);
    }

    public static OperationMapping suggested(OpenApiOperation source, OpenApiOperation target, int score, String reason) {
        return new OperationMapping(source, target, List.of(target), MappingStatus.SUGGESTED, score, reason);
    }

    public static OperationMapping ambiguous(OpenApiOperation source, List<OpenApiOperation> candidates, String reason) {
        return new OperationMapping(source, null, candidates, MappingStatus.AMBIGUOUS, 50, reason);
    }

    public static OperationMapping unresolved(OpenApiOperation source, String reason) {
        return new OperationMapping(source, null, List.of(), MappingStatus.UNRESOLVED, 0, reason);
    }

    public OpenApiOperation getSourceOperation() {
        return sourceOperation;
    }

    public OpenApiOperation getTargetOperation() {
        return targetOperation;
    }

    public List<OpenApiOperation> getCandidateOperations() {
        return candidateOperations;
    }

    public MappingStatus getStatus() {
        return status;
    }

    public int getConfidenceScore() {
        return confidenceScore;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "[" + status + " (" + confidenceScore + "%)] " + sourceOperation + " -> " +
                (targetOperation != null ? targetOperation : (status == MappingStatus.AMBIGUOUS ? candidateOperations.size() + " candidates" : "NONE"));
    }
}
