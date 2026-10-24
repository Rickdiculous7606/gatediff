package io.gatediff.model;

/**
 * Encapsulates the execution and comparison of a single migration test case.
 */
public class TestExecutionResult {
    private final String serviceId;
    private final String testId;
    private final String testName;
    private final HttpRequestSpec request;
    private final HttpResponseSnapshot sourceResponse;
    private final HttpResponseSnapshot targetResponse;
    private final ComparisonResult comparison;
    private final boolean executionError;
    private final String executionErrorMessage;
    private final long executionDurationMs;

    public TestExecutionResult(
            String serviceId,
            String testId,
            String testName,
            HttpRequestSpec request,
            HttpResponseSnapshot sourceResponse,
            HttpResponseSnapshot targetResponse,
            ComparisonResult comparison,
            boolean executionError,
            String executionErrorMessage,
            long executionDurationMs) {
        this.serviceId = serviceId;
        this.testId = testId;
        this.testName = testName != null ? testName : testId;
        this.request = request;
        this.sourceResponse = sourceResponse;
        this.targetResponse = targetResponse;
        this.comparison = comparison;
        this.executionError = executionError;
        this.executionErrorMessage = executionErrorMessage;
        this.executionDurationMs = executionDurationMs;
    }

    public static TestExecutionResult error(
            String serviceId,
            String testId,
            String testName,
            HttpRequestSpec request,
            String errorMessage) {
        return new TestExecutionResult(
                serviceId,
                testId,
                testName,
                request,
                null,
                null,
                new ComparisonResult(false, java.util.Collections.emptyList(), "Execution Error: " + errorMessage),
                true,
                errorMessage,
                0
        );
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getTestId() {
        return testId;
    }

    public String getTestName() {
        return testName;
    }

    public HttpRequestSpec getRequest() {
        return request;
    }

    public HttpResponseSnapshot getSourceResponse() {
        return sourceResponse;
    }

    public HttpResponseSnapshot getTargetResponse() {
        return targetResponse;
    }

    public ComparisonResult getComparison() {
        return comparison;
    }

    public boolean isExecutionError() {
        return executionError;
    }

    public String getExecutionErrorMessage() {
        return executionErrorMessage;
    }

    public long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public boolean isPassed() {
        return !executionError && comparison != null && comparison.isPassed();
    }
}
