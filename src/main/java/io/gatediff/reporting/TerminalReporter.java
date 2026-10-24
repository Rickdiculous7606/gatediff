package io.gatediff.reporting;

import io.gatediff.model.FieldMismatch;
import io.gatediff.model.TestExecutionResult;
import io.gatediff.model.TestSuiteReport;

import java.io.PrintStream;

/**
 * Produces clean, readable terminal summaries with clear pass/fail status and actionable diffs.
 */
public class TerminalReporter {

    public static void printSummary(TestSuiteReport report, PrintStream out) {
        out.println();
        out.println("================================================================================");
        out.println(" GateDiff Migration Test Execution Summary");
        out.println(" Project:   " + report.getProjectName());
        out.println(" Timestamp: " + report.getTimestamp());
        out.println(" Total:     " + report.getTotalTests() + " | Passed: " + report.getPassedCount() +
                " | Failed: " + report.getFailedCount() + " | Errors: " + report.getErrorCount());
        out.println(" Duration:  " + report.getTotalDurationMs() + "ms");
        out.println("================================================================================");
        out.println();

        for (TestExecutionResult result : report.getResults()) {
            String badge = result.isPassed() ? "[PASS]" : (result.isExecutionError() ? "[ERR ]" : "[FAIL]");
            out.printf("%s %-16s | %s %-32s (%dms)%n",
                    badge,
                    result.getServiceId(),
                    result.getRequest().getMethod(),
                    result.getRequest().getPath(),
                    result.getExecutionDurationMs());

            if (result.isExecutionError()) {
                out.println("       * Execution Error: " + result.getExecutionErrorMessage());
            } else if (!result.isPassed() && result.getComparison() != null) {
                out.println("       * Source: HTTP " + result.getSourceResponse().getStatusCode() + " (" + result.getSourceResponse().getDurationMs() + "ms)");
                out.println("       * Target: HTTP " + result.getTargetResponse().getStatusCode() + " (" + result.getTargetResponse().getDurationMs() + "ms)");
                for (FieldMismatch m : result.getComparison().getMismatches()) {
                    out.println("       * MISMATCH " + m.getPath() + ": " + m.getMessage());
                }
            }
            out.println();
        }

        out.println("--------------------------------------------------------------------------------");
        if (report.isAllPassed()) {
            out.println(" STATUS: ALL MIGRATION TESTS PASSED (Exit code: 0)");
        } else if (report.getErrorCount() > 0) {
            out.println(" STATUS: EXECUTION INFRASTRUCTURE ERRORS OCCURRED (Exit code: 3)");
        } else {
            out.println(" STATUS: MIGRATION MISMATCH REGRESSIONS DETECTED (Exit code: 1)");
        }
        out.println("================================================================================");
        out.println();
    }
}
