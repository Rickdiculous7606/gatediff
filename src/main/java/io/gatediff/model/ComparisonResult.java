package io.gatediff.model;

import java.util.Collections;
import java.util.List;

/**
 * Result of comparing a source response against a target response.
 */
public class ComparisonResult {
    private final boolean passed;
    private final List<FieldMismatch> mismatches;
    private final String summary;

    public ComparisonResult(boolean passed, List<FieldMismatch> mismatches, String summary) {
        this.passed = passed;
        this.mismatches = mismatches != null ? Collections.unmodifiableList(mismatches) : Collections.emptyList();
        this.summary = summary != null ? summary : (passed ? "Passed: Responses match" : "Failed: " + this.mismatches.size() + " mismatch(es) detected");
    }

    public static ComparisonResult pass() {
        return new ComparisonResult(true, Collections.emptyList(), "Passed: Responses match");
    }

    public static ComparisonResult fail(List<FieldMismatch> mismatches) {
        return new ComparisonResult(false, mismatches, null);
    }

    public boolean isPassed() {
        return passed;
    }

    public List<FieldMismatch> getMismatches() {
        return mismatches;
    }

    public String getSummary() {
        return summary;
    }

    @Override
    public String toString() {
        return summary;
    }
}
