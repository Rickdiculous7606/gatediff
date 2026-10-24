package io.gatediff.cli;

import io.gatediff.config.ConfigLoader;
import io.gatediff.config.ConfigValidator;
import io.gatediff.config.GateDiffConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "config", description = "Validate GateDiff YAML configuration files")
public class ConfigValidateCommand implements Callable<Integer> {

    @Command(name = "validate", description = "Check syntax and semantic validity of migration configuration")
    public int validate(
            @Option(names = {"-f", "--file"}, required = true, description = "Path to migration YAML config") File file
    ) {
        if (!file.exists()) {
            System.err.println("Error: File not found: " + file.getAbsolutePath());
            return ExitCodes.INVALID_CONFIG_OR_USAGE;
        }

        System.out.println("Validating configuration: " + file.getAbsolutePath());

        GateDiffConfig config;
        try {
            config = ConfigLoader.load(file);
        } catch (Exception e) {
            System.err.println("[FAIL] YAML syntax error: " + e.getMessage());
            return ExitCodes.INVALID_CONFIG_OR_USAGE;
        }

        List<ConfigValidator.ValidationError> errors = ConfigValidator.validate(config);
        if (errors.isEmpty()) {
            System.out.println("[PASS] Configuration is valid (Version " + config.getVersion() + ", " +
                    config.getServices().size() + " service(s) configured).");
            return ExitCodes.SUCCESS;
        } else {
            System.err.println("[FAIL] Found " + errors.size() + " configuration validation error(s):");
            for (ConfigValidator.ValidationError err : errors) {
                System.err.println("  - " + err);
            }
            return ExitCodes.INVALID_CONFIG_OR_USAGE;
        }
    }

    @Override
    public Integer call() {
        System.out.println("Use 'gatediff config validate --file <path>' to validate configuration.");
        return ExitCodes.SUCCESS;
    }
}
