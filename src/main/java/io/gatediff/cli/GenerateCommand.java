package io.gatediff.cli;

import io.gatediff.mapping.MappingSuggestionEngine;
import io.gatediff.mapping.OperationMapping;
import io.gatediff.mapping.YamlConfigGenerator;
import io.gatediff.openapi.OpenApiParser;
import io.gatediff.openapi.OpenApiSpecSummary;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "generate", description = "Generate reviewable migration YAML configuration from confirmed mappings")
public class GenerateCommand implements Callable<Integer> {

    @Option(names = {"-s", "--source"}, required = true, description = "Path to Source OpenAPI specification file")
    private File sourceSpecFile;

    @Option(names = {"-t", "--target"}, required = true, description = "Path to Target OpenAPI specification file")
    private File targetSpecFile;

    @Option(names = {"-o", "--output"}, required = true, description = "Destination path for generated migration YAML config")
    private File outputFile;

    @Option(names = {"--source-url"}, defaultValue = "http://localhost:8081", description = "Base URL of source gateway (default: http://localhost:8081)")
    private String sourceBaseUrl;

    @Option(names = {"--target-url"}, defaultValue = "http://localhost:8082", description = "Base URL of target gateway (default: http://localhost:8082)")
    private String targetBaseUrl;

    @Override
    public Integer call() {
        if (!sourceSpecFile.exists()) {
            System.err.println("Error: Source spec file not found: " + sourceSpecFile.getAbsolutePath());
            return ExitCodes.INVALID_CONFIG_OR_USAGE;
        }
        if (!targetSpecFile.exists()) {
            System.err.println("Error: Target spec file not found: " + targetSpecFile.getAbsolutePath());
            return ExitCodes.INVALID_CONFIG_OR_USAGE;
        }

        try {
            OpenApiSpecSummary sourceSummary = OpenApiParser.parse(sourceSpecFile);
            OpenApiSpecSummary targetSummary = OpenApiParser.parse(targetSpecFile);

            MappingSuggestionEngine engine = new MappingSuggestionEngine();
            List<OperationMapping> mappings = engine.mapOperations(sourceSummary.getOperations(), targetSummary.getOperations());

            String yaml = YamlConfigGenerator.generate(
                    "generated-gateway-migration",
                    "migrated-service",
                    sourceSummary.getTitle(),
                    sourceBaseUrl,
                    targetBaseUrl,
                    mappings
            );

            if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            try (FileWriter fw = new FileWriter(outputFile)) {
                fw.write(yaml);
            }

            System.out.println("[SUCCESS] Migration YAML successfully generated at: " + outputFile.getAbsolutePath());
            System.out.println("Generated tests for " + mappings.stream().filter(m -> m.getTargetOperation() != null).count() + " mapped operation(s).");
            System.out.println("Next steps: Review paths and sample payloads, then run 'gatediff config validate --file " + outputFile.getName() + "'.");

            return ExitCodes.SUCCESS;
        } catch (Exception e) {
            System.err.println("Error generating migration configuration: " + e.getMessage());
            return ExitCodes.INVALID_CONFIG_OR_USAGE;
        }
    }
}
