package io.gatediff.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
        name = "gatediff",
        mixinStandardHelpOptions = true,
        version = "GateDiff 1.0.0-SNAPSHOT (Java 21 / Picocli)",
        description = "Open-Source API Gateway Migration Testing CLI for Kong, Gravitee, Apigee, and AWS API Gateway.",
        subcommands = {
                RunCommand.class,
                MockCommand.class,
                ConfigValidateCommand.class,
                ImportOpenApiCommand.class,
                MapCommand.class,
                GenerateCommand.class,
                ReportCommand.class
        }
)
public class GateDiffCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return ExitCodes.SUCCESS;
    }
}
