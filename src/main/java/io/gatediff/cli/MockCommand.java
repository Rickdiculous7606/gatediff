package io.gatediff.cli;

import io.gatediff.mock.LocalMockLab;
import io.gatediff.mock.MockScenario;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "mock", description = "Run local fictional API migration lab servers for offline testing")
public class MockCommand implements Callable<Integer> {

    @Command(name = "start", description = "Start fictional Source (Kong) and Target (Gravitee) mock servers on localhost")
    public int start(
            @Option(names = {"--port-source"}, defaultValue = "8081", description = "Port for source gateway mock (default: 8081)") int sourcePort,
            @Option(names = {"--port-target"}, defaultValue = "8082", description = "Port for target gateway mock (default: 8082)") int targetPort,
            @Option(names = {"--scenario"}, defaultValue = "MATCHING", description = "Scenario mode: MATCHING, VALUE_MISMATCH, STATUS_MISMATCH, MISSING_FIELD, ADDITIONAL_FIELD, SERVER_ERROR, HEADER_MISMATCH, LATENCY_SPIKE") String scenarioName
    ) {
        MockScenario scenario;
        try {
            scenario = MockScenario.valueOf(scenarioName.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Unknown scenario '" + scenarioName + "'. Available: MATCHING, VALUE_MISMATCH, STATUS_MISMATCH, MISSING_FIELD, ADDITIONAL_FIELD, SERVER_ERROR, HEADER_MISMATCH, LATENCY_SPIKE");
            return ExitCodes.INVALID_CONFIG_OR_USAGE;
        }

        try {
            LocalMockLab lab = new LocalMockLab(sourcePort, targetPort, scenario);
            lab.start();

            System.out.println("================================================================================");
            System.out.println(" GateDiff Fictional API Migration Lab Started");
            System.out.println(" Source Gateway (Kong Mock):     " + lab.getSourceBaseUrl());
            System.out.println(" Target Gateway (Gravitee Mock): " + lab.getTargetBaseUrl());
            System.out.println(" Active Scenario Mode:           " + scenario);
            System.out.println(" Sample Endpoint:                GET " + lab.getSourceBaseUrl() + "/api/customers/C-101");
            System.out.println(" Security Note:                  Bound strictly to 127.0.0.1 (Localhost only)");
            System.out.println(" Press Ctrl+C to stop.");
            System.out.println("================================================================================");

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nStopping GateDiff mock lab servers...");
                lab.stop();
            }));

            // Keep alive
            Thread.currentThread().join();
            return ExitCodes.SUCCESS;
        } catch (Exception e) {
            System.err.println("Error starting mock lab: " + e.getMessage());
            return ExitCodes.INFRASTRUCTURE_ERROR;
        }
    }

    @Override
    public Integer call() {
        System.out.println("Use 'gatediff mock start' to launch the fictional API lab. Run 'gatediff mock start --help' for options.");
        return ExitCodes.SUCCESS;
    }
}
