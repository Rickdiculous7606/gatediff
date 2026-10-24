package io.gatediff.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatediff.model.TestSuiteReport;
import io.gatediff.reporting.HtmlReporter;
import io.gatediff.reporting.TerminalReporter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "report", description = "Render or convert GateDiff JSON test results into terminal or HTML format")
public class ReportCommand implements Callable<Integer> {

    @Option(names = {"-i", "--input"}, required = true, description = "Path to the JSON report file")
    private File inputJson;

    @Option(names = {"--format"}, defaultValue = "terminal", description = "Output format: terminal, html (default: terminal)")
    private String format;

    @Option(names = {"-o", "--output"}, description = "Optional output file when converting to HTML")
    private File outputFile;

    @Override
    public Integer call() {
        if (!inputJson.exists()) {
            System.err.println("Error: JSON report file not found: " + inputJson.getAbsolutePath());
            return ExitCodes.INVALID_CONFIG_OR_USAGE;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            TestSuiteReport report = mapper.readValue(inputJson, TestSuiteReport.class);

            if ("html".equalsIgnoreCase(format)) {
                if (outputFile == null) {
                    outputFile = new File("reports/gatediff-report.html");
                }
                HtmlReporter.writeToFile(report, outputFile);
                System.out.println("HTML report generated at: " + outputFile.getAbsolutePath());
            } else {
                TerminalReporter.printSummary(report, System.out);
            }

            return report.isAllPassed() ? ExitCodes.SUCCESS : ExitCodes.COMPARISON_FAILED;
        } catch (Exception e) {
            System.err.println("Error reading JSON report: " + e.getMessage());
            return ExitCodes.INVALID_CONFIG_OR_USAGE;
        }
    }
}
