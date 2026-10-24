package io.gatediff.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Aggregated report of an entire test run across one or more services.
 */
public class TestSuiteReport {
    private final String projectName;
    private final String timestamp;
    private final List<TestExecutionResult> results;
    private final int totalTests;
    private final int passedCount;
    private final int failedCount;
    private final int errorCount;
    private final long totalDurationMs;

    public TestSuiteReport(String projectName, List<TestExecutionResult> results, long totalDurationMs) {
        this.projectName = projectName != null ? projectName : "GateDiff Migration Run";
        this.timestamp = Instant.now().toString();
        this.results = results != null ? Collections.unmodifiableList(results) : Collections.emptyList();
        this.totalDurationMs = totalDurationMs;

        int passed = 0;
        int failed = 0;
        int errors = 0;

        for (TestExecutionResult r : this.results) {
            if (r.isExecutionError()) {
                errors++;
            } else if (r.isPassed()) {
                passed++;
            } else {
                failed++;
            }
        }

        this.totalTests = this.results.size();
        this.passedCount = passed;
        this.failedCount = failed;
        this.errorCount = errors;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public List<TestExecutionResult> getResults() {
        return results;
    }

    public int getTotalTests() {
        return totalTests;
    }

    public int getPassedCount() {
        return passedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    public boolean isAllPassed() {
        return errorCount == 0 && failedCount == 0 && totalTests > 0;
    }
}
