package io.gatediff;

import io.gatediff.cli.GateDiffCommand;
import picocli.CommandLine;

/**
 * Main application entry point for GateDiff CLI.
 */
public class GateDiffMain {

    public static void main(String[] args) {
        int exitCode = execute(args);
        System.exit(exitCode);
    }

    /**
     * Executes CLI command without calling System.exit, enabling programmatic and unit testing.
     */
    public static int execute(String... args) {
        CommandLine cmd = new CommandLine(new GateDiffCommand());
        return cmd.execute(args);
    }
}
