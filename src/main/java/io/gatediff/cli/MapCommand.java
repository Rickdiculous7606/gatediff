package io.gatediff.cli;

import io.gatediff.mapping.MappingStatus;
import io.gatediff.mapping.MappingSuggestionEngine;
import io.gatediff.mapping.OperationMapping;
import io.gatediff.openapi.OpenApiOperation;
import io.gatediff.openapi.OpenApiParser;
import io.gatediff.openapi.OpenApiSpecSummary;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "map", description = "Suggest route mappings between source and target OpenAPI specs")
public class MapCommand implements Callable<Integer> {

    @Option(names = {"-s", "--source"}, required = true, description = "Path to Source OpenAPI specification file")
    private File sourceSpecFile;

    @Option(names = {"-t", "--target"}, required = true, description = "Path to Target OpenAPI specification file")
    private File targetSpecFile;

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

            int confirmed = 0;
            int suggested = 0;
            int ambiguous = 0;
            int unresolved = 0;

            System.out.println("================================================================================");
            System.out.println(" GateDiff Migration Operation Mapping Report");
            System.out.println(" Source: " + sourceSummary.getTitle() + " (" + sourceSummary.getOperationCount() + " ops)");
            System.out.println(" Target: " + targetSummary.getTitle() + " (" + targetSummary.getOperationCount() + " ops)");
            System.out.println("================================================================================");
            System.out.printf("%-12s %-30s -> %-30s %s%n", "STATUS", "SOURCE OPERATION", "TARGET OPERATION", "NOTES");
            System.out.println("--------------------------------------------------------------------------------");

            for (OperationMapping m : mappings) {
                switch (m.getStatus()) {
                    case CONFIRMED -> confirmed++;
                    case SUGGESTED -> suggested++;
                    case AMBIGUOUS -> ambiguous++;
                    case UNRESOLVED -> unresolved++;
                }

                String targetDesc = (m.getTargetOperation() != null)
                        ? m.getTargetOperation().getMethod() + " " + m.getTargetOperation().getPath()
                        : (m.getStatus() == MappingStatus.AMBIGUOUS ? "[" + m.getCandidateOperations().size() + " candidates]" : "[UNRESOLVED]");

                System.out.printf("[%-10s] %-30s -> %-30s %s%n",
                        m.getStatus(),
                        m.getSourceOperation().getMethod() + " " + m.getSourceOperation().getPath(),
                        targetDesc,
                        m.getReason());

                if (m.getStatus() == MappingStatus.AMBIGUOUS) {
                    for (OpenApiOperation cand : m.getCandidateOperations()) {
                        System.out.println("              ? Candidate: " + cand.getMethod() + " " + cand.getPath() + (cand.getOperationId().isEmpty() ? "" : " (" + cand.getOperationId() + ")"));
                    }
                }
            }

            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("Coverage: %d Confirmed | %d Suggested | %d Ambiguous | %d Unresolved%n",
                    confirmed, suggested, ambiguous, unresolved);
            System.out.println("================================================================================");

            return ExitCodes.SUCCESS;
        } catch (Exception e) {
            System.err.println("Error generating operation mapping: " + e.getMessage());
            return ExitCodes.INVALID_CONFIG_OR_USAGE;
        }
    }
}
