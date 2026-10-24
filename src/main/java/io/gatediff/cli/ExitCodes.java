package io.gatediff.cli;

/**
 * Well-defined process exit codes for GateDiff CLI and CI/CD pipelines.
 */
public final class ExitCodes {
    /**
     * All executed comparisons passed successfully without regressions.
     */
    public static final int SUCCESS = 0;

    /**
     * One or more comparisons failed (status mismatch, body diff, missing/extra fields).
     */
    public static final int COMPARISON_FAILED = 1;

    /**
     * Invalid configuration file, schema validation error, or invalid CLI usage.
     */
    public static final int INVALID_CONFIG_OR_USAGE = 2;

    /**
     * Execution infrastructure error (e.g., unreachable gateway endpoint, DNS failure, timeout).
     */
    public static final int INFRASTRUCTURE_ERROR = 3;

    private ExitCodes() {}
}
