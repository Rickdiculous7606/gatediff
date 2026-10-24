package io.gatediff.cli;

import io.gatediff.openapi.OpenApiOperation;
import io.gatediff.openapi.OpenApiParser;
import io.gatediff.openapi.OpenApiSpecSummary;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "import", description = "Import and inspect API specifications")
public class ImportOpenApiCommand implements Callable<Integer> {

    @Command(name = "openapi", description = "Parse an OpenAPI 3.x spec (YAML or JSON) and summarize operations")
    public int openapi(
            @Option(names = {"-i", "--input"}, required = true, description = "Path to OpenAPI 3.x specification file") File specFile
    ) {
        if (!specFile.exists()) {
            System.err.println("Error: Specification file not found: " + specFile.getAbsolutePath());
            return ExitCodes.INVALID_CONFIG_OR_USAGE;
        }

        try {
            OpenApiSpecSummary summary = OpenApiParser.parse(specFile);

            System.out.println("================================================================================");
            System.out.println(" OpenAPI Specification Summary: " + summary.getTitle() + " (v" + summary.getVersion() + ")");
            System.out.println(" File:        " + specFile.getName());
            System.out.println(" Description: " + summary.getDescription());
            System.out.println(" Operations:  " + summary.getOperationCount() + " extracted");
            System.out.println("================================================================================");
            System.out.printf("%-8s %-32s %-24s %s%n", "METHOD", "PATH", "OPERATION ID", "RESPONSES");
            System.out.println("--------------------------------------------------------------------------------");

            for (OpenApiOperation op : summary.getOperations()) {
                System.out.printf("%-8s %-32s %-24s %s%n",
                        op.getMethod(),
                        op.getPath(),
                        op.getOperationId().isEmpty() ? "-" : op.getOperationId(),
                        String.join(", ", op.getResponseCodes()));
            }

            System.out.println("================================================================================");
            return ExitCodes.SUCCESS;
        } catch (Exception e) {
            System.err.println("Error parsing OpenAPI specification: " + e.getMessage());
            return ExitCodes.INVALID_CONFIG_OR_USAGE;
        }
    }

    @Override
    public Integer call() {
        System.out.println("Use 'gatediff import openapi --input <file>' to parse OpenAPI specifications.");
        return ExitCodes.SUCCESS;
    }
}
