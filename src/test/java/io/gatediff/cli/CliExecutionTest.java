package io.gatediff.cli;

import io.gatediff.GateDiffMain;
import io.gatediff.mock.LocalMockLab;
import io.gatediff.mock.MockScenario;
import org.junit.jupiter.api.*;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class CliExecutionTest {

    private static LocalMockLab lab;

    @BeforeAll
    static void startLab() throws Exception {
        lab = new LocalMockLab(8081, 8082, MockScenario.MATCHING);
        lab.start();
    }

    @AfterAll
    static void stopLab() {
        if (lab != null) {
            lab.stop();
        }
    }

    @Test
    @DisplayName("gatediff --help exits with 0")
    void testHelpCommand() {
        int exitCode = GateDiffMain.execute("--help");
        assertThat(exitCode).isEqualTo(ExitCodes.SUCCESS);
    }

    @Test
    @DisplayName("gatediff --version exits with 0")
    void testVersionCommand() {
        int exitCode = GateDiffMain.execute("--version");
        assertThat(exitCode).isEqualTo(ExitCodes.SUCCESS);
    }

    @Test
    @DisplayName("gatediff config validate on valid file exits with 0")
    void testConfigValidateSuccess() {
        int exitCode = GateDiffMain.execute("config", "validate", "--file", "examples/configs/migration.yaml");
        assertThat(exitCode).isEqualTo(ExitCodes.SUCCESS);
    }

    @Test
    @DisplayName("gatediff config validate on missing file exits with 2")
    void testConfigValidateMissingFile() {
        int exitCode = GateDiffMain.execute("config", "validate", "--file", "examples/configs/does-not-exist.yaml");
        assertThat(exitCode).isEqualTo(ExitCodes.INVALID_CONFIG_OR_USAGE);
    }

    @Test
    @DisplayName("gatediff import openapi exits with 0")
    void testImportOpenApi() {
        int exitCode = GateDiffMain.execute("import", "openapi", "--input", "examples/specs/customer-api.yaml");
        assertThat(exitCode).isEqualTo(ExitCodes.SUCCESS);
    }

    @Test
    @DisplayName("gatediff map exits with 0 and displays coverage")
    void testMapCommand() {
        int exitCode = GateDiffMain.execute("map",
                "--source", "examples/specs/customer-api.yaml",
                "--target", "examples/specs/target-customer-api.yaml");
        assertThat(exitCode).isEqualTo(ExitCodes.SUCCESS);
    }

    @Test
    @DisplayName("gatediff generate creates valid YAML configuration")
    void testGenerateCommand() {
        File genFile = new File("target/generated-migration.yaml");
        int exitCode = GateDiffMain.execute("generate",
                "--source", "examples/specs/customer-api.yaml",
                "--target", "examples/specs/target-customer-api.yaml",
                "--output", genFile.getPath());

        assertThat(exitCode).isEqualTo(ExitCodes.SUCCESS);
        assertThat(genFile).exists();
    }

    @Test
    @DisplayName("gatediff run exits with 0 when mock lab is in MATCHING mode")
    void testRunMatchingPasses() {
        lab.setScenario(MockScenario.MATCHING);
        int exitCode = GateDiffMain.execute("run",
                "--config", "examples/configs/migration.yaml",
                "--output-json", "target/reports/test-run-pass.json",
                "--output-html", "target/reports/test-run-pass.html");

        assertThat(exitCode).isEqualTo(ExitCodes.SUCCESS);
    }

    @Test
    @DisplayName("gatediff run exits with 1 when target returns regression mismatch")
    void testRunMismatchFails() {
        lab.setScenario(MockScenario.VALUE_MISMATCH);
        int exitCode = GateDiffMain.execute("run",
                "--config", "examples/configs/migration.yaml",
                "--output-json", "target/reports/test-run-fail.json");

        assertThat(exitCode).isEqualTo(ExitCodes.COMPARISON_FAILED);
    }
}
