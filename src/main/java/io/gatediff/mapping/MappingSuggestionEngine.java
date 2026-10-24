package io.gatediff.mapping;

import io.gatediff.openapi.OpenApiOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * Suggests mappings between source operations and target operations.
 * Evaluates HTTP method match, path structure, normalized parameters, and operation IDs.
 * Strictly avoids silent guesses: presents ambiguous candidates when confidence is tied.
 */
public class MappingSuggestionEngine {

    public List<OperationMapping> mapOperations(List<OpenApiOperation> sourceOps, List<OpenApiOperation> targetOps) {
        List<OperationMapping> mappings = new ArrayList<>();

        for (OpenApiOperation src : sourceOps) {
            List<ScoredTarget> matches = new ArrayList<>();

            for (OpenApiOperation tgt : targetOps) {
                int score = calculateMatchScore(src, tgt);
                if (score >= 40) {
                    matches.add(new ScoredTarget(tgt, score, describeMatch(src, tgt, score)));
                }
            }

            if (matches.isEmpty()) {
                mappings.add(OperationMapping.unresolved(src, "No matching operation found in target specification"));
            } else if (matches.size() == 1) {
                ScoredTarget best = matches.get(0);
                if (best.score >= 90) {
                    mappings.add(OperationMapping.confirmed(src, best.target, best.reason));
                } else {
                    mappings.add(OperationMapping.suggested(src, best.target, best.score, best.reason));
                }
            } else {
                // Multiple matches: check if there is an undisputed top match
                matches.sort((a, b) -> Integer.compare(b.score, a.score));
                ScoredTarget top = matches.get(0);
                ScoredTarget second = matches.get(1);

                if (top.score >= 90 && top.score - second.score >= 30) {
                    mappings.add(OperationMapping.confirmed(src, top.target, top.reason));
                } else {
                    List<OpenApiOperation> candidates = new ArrayList<>();
                    for (ScoredTarget m : matches) {
                        candidates.add(m.target);
                    }
                    mappings.add(OperationMapping.ambiguous(src, candidates,
                            "Multiple candidate operations (" + matches.size() + ") with similar path/method similarity"));
                }
            }
        }

        return mappings;
    }

    private int calculateMatchScore(OpenApiOperation src, OpenApiOperation tgt) {
        // Method mismatch: heavy penalty
        if (!src.getMethod().equalsIgnoreCase(tgt.getMethod())) {
            return 0;
        }

        int score = 40; // Base score for matching method

        // Exact path match
        if (src.getPath().equalsIgnoreCase(tgt.getPath())) {
            score += 50;
        } else {
            // Normalized parameter path match (e.g. /customers/{id} vs /customers/{customerId})
            String normSrc = normalizePath(src.getPath());
            String normTgt = normalizePath(tgt.getPath());
            if (normSrc.equalsIgnoreCase(normTgt)) {
                score += 45;
            } else if (normSrc.endsWith(normTgt) || normTgt.endsWith(normSrc)) {
                score += 30; // Prefix diff (e.g. /api/v1/ vs /api/)
            }
        }

        // OperationId match bonus
        if (!src.getOperationId().isEmpty() && !tgt.getOperationId().isEmpty()) {
            if (src.getOperationId().equalsIgnoreCase(tgt.getOperationId())) {
                score += 20;
            }
        }

        return Math.min(100, score);
    }

    private String describeMatch(OpenApiOperation src, OpenApiOperation tgt, int score) {
        if (score >= 90) {
            return "Exact method (" + src.getMethod() + ") and matching path (" + src.getPath() + ")";
        } else if (score >= 70) {
            return "Matching method with normalized parameter path mapping (" + normalizePath(src.getPath()) + ")";
        } else {
            return "Partial path similarity with matching HTTP method";
        }
    }

    private String normalizePath(String path) {
        if (path == null) return "/";
        // Replace {paramName} with {*}
        return path.replaceAll("\\{[^}]+\\}", "{*}").replaceAll("/+", "/");
    }

    private static class ScoredTarget {
        final OpenApiOperation target;
        final int score;
        final String reason;

        ScoredTarget(OpenApiOperation target, int score, String reason) {
            this.target = target;
            this.score = score;
            this.reason = reason;
        }
    }
}
