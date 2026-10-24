package io.gatediff.cli;

import io.gatediff.comparison.ComparisonEngine;
import io.gatediff.config.*;
import io.gatediff.http.HttpRunner;
import io.gatediff.model.HttpRequestSpec;
import io.gatediff.model.TestExecutionResult;
import io.gatediff.model.TestSuiteReport;
import io.gatediff.reporting.HtmlReporter;
import io.gatediff.reporting.JsonReporter;
import io.gatediff.reporting.TerminalReporter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;

@Command(name = "run", description = "Execute migration validation comparisons between source and target gateways")
public class RunCommand implements Callable<Integer> {

    @Option(names = {"-c", "--config"}, required = true, description = "Path to the migration YAML configuration file")
    private File configFile;

    @Option(names = {"-s", "--service"}, description = "Filter execution to a specific service ID")
    private String serviceIdFilter;

    @Option(names = {"--output-json"}, description = "Optional destination file for JSON report")
    private File outputJson;

    @Option(names = {"--output-html"}, description = "Optional destination file for HTML report")
    private File outputHtml;

    @Override
    public Integer call() {
        if (!configFile.exists()) {
            System.err.println("Error: Configuration file not found: " + configFile.getAbsolutePath());
            return ExitCodes.INVALID_CONFIG_OR_USAGE;
        }

        GateDiffConfig config;
        try {
            config = ConfigLoader.load(configFile);
        } catch (Exception e) {
            System.err.println("Error: Failed to parse configuration YAML: " + e.getMessage());
            return ExitCodes.INVALID_CONFIG_OR_USAGE;
        }

        List<ConfigValidator.ValidationError> errors = ConfigValidator.validate(config);
        if (!errors.isEmpty()) {
            System.err.println("Configuration validation failed with " + errors.size() + " error(s):");
            for (ConfigValidator.ValidationError err : errors) {
                System.err.println("  * " + err);
            }
            return ExitCodes.INVALID_CONFIG_OR_USAGE;
        }

        DefaultsConfig defaults = config.getDefaults();
        int timeoutMs = defaults.getTimeoutMs();
        int retries = defaults.getRetries();
        HttpRunner runner = new HttpRunner(Duration.ofMillis(timeoutMs), Duration.ofMillis(timeoutMs), retries);

        long suiteStartTime = System.currentTimeMillis();
        List<TestExecutionResult> results = new ArrayList<>();

        for (ServiceConfig service : config.getServices()) {
            if (serviceIdFilter != null && !serviceIdFilter.equalsIgnoreCase(service.getId())) {
                continue;
            }

            for (TestCaseConfig test : service.getTests()) {
                // Merge comparison rules: test overrides service, service overrides defaults
                CompareConfig effectiveCompare = defaults.getCompare();
                if (service.getCompare() != null) {
                    effectiveCompare = service.getCompare().mergeWith(effectiveCompare);
                }
                if (test.getCompare() != null) {
                    effectiveCompare = test.getCompare().mergeWith(effectiveCompare);
                }

                ComparisonEngine engine = new ComparisonEngine(
                        effectiveCompare.getStatus(),
                        effectiveCompare.getJsonBody(),
                        effectiveCompare.getAdditionalFields(),
                        new HashSet<>(effectiveCompare.getIgnoredJsonPaths()),
                        new HashSet<>(effectiveCompare.getHeaders())
                );

                // Prepare request headers (service default headers + test headers)
                Map<String, List<String>> headers = new HashMap<>();
                for (Map.Entry<String, String> e : service.getHeaders().entrySet()) {
                    headers.put(e.getKey(), List.of(e.getValue()));
                }
                for (Map.Entry<String, String> e : test.getHeaders().entrySet()) {
                    headers.put(e.getKey(), List.of(e.getValue()));
                }

                HttpRequestSpec reqSpec = new HttpRequestSpec(
                        test.getMethod(),
                        test.getPath(),
                        headers,
                        test.getBody()
                );

                TestExecutionResult res = runner.executeAndCompare(
                        service.getId(),
                        test.getId(),
                        test.getName(),
                        service.getSourceBaseUrl(),
                        service.getTargetBaseUrl(),
                        reqSpec,
                        engine
                );

                results.add(res);
            }
        }

        long totalSuiteDuration = System.currentTimeMillis() - suiteStartTime;
        TestSuiteReport report = new TestSuiteReport(config.getProject().getName(), results, totalSuiteDuration);

        // Terminal Output
        TerminalReporter.printSummary(report, System.out);

        // Optional JSON output
        if (outputJson != null) {
            try {
                JsonReporter.writeToFile(report, outputJson);
                System.out.println("JSON report written to: " + outputJson.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Warning: Failed to write JSON report: " + e.getMessage());
            }
        }

        // Optional HTML output
        if (outputHtml != null) {
            try {
                HtmlReporter.writeToFile(report, outputHtml);
                System.out.println("HTML report written to: " + outputHtml.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Warning: Failed to write HTML report: " + e.getMessage());
            }
        }

        // Determine exit code
        if (report.isAllPassed()) {
            return ExitCodes.SUCCESS;
        } else if (report.getErrorCount() > 0) {
            return ExitCodes.INFRASTRUCTURE_ERROR;
        } else {
            return ExitCodes.COMPARISON_FAILED;
        }
    }
}
