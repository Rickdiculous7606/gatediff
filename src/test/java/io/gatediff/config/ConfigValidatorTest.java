package io.gatediff.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigValidatorTest {

    @Test
    @DisplayName("Validates sample migration.yaml without errors")
    void testSampleMigrationConfigIsValid() throws Exception {
        File file = new File("examples/configs/migration.yaml");
        GateDiffConfig config = ConfigLoader.load(file);

        List<ConfigValidator.ValidationError> errors = ConfigValidator.validate(config);

        assertThat(errors).isEmpty();
        assertThat(config.getVersion()).isEqualTo(1);
        assertThat(config.getServices()).hasSize(1);
        assertThat(config.getServices().get(0).getTests()).hasSize(4);
    }

    @Test
    @DisplayName("Catches unsupported schema version")
    void testUnsupportedSchemaVersion() {
        GateDiffConfig config = new GateDiffConfig();
        config.setVersion(99);
        config.getProject().setName("test");

        List<ConfigValidator.ValidationError> errors = ConfigValidator.validate(config);

        assertThat(errors).anyMatch(e -> e.getField().equals("version") && e.getMessage().contains("Unsupported schema version '99'"));
    }

    @Test
    @DisplayName("Catches missing project name and empty services")
    void testMissingProjectNameAndServices() {
        GateDiffConfig config = new GateDiffConfig();
        config.setVersion(1);
        config.getProject().setName("");

        List<ConfigValidator.ValidationError> errors = ConfigValidator.validate(config);

        assertThat(errors).anyMatch(e -> e.getField().equals("project.name"));
        assertThat(errors).anyMatch(e -> e.getField().equals("services"));
    }

    @Test
    @DisplayName("Catches invalid URL schemes")
    void testInvalidUrlScheme() {
        GateDiffConfig config = new GateDiffConfig();
        config.setVersion(1);
        config.getProject().setName("Test Project");

        ServiceConfig svc = new ServiceConfig();
        svc.setId("test-svc");
        svc.setSourceBaseUrl("ftp://bad-url");
        svc.setTargetBaseUrl("http://localhost:8082");

        TestCaseConfig testCase = new TestCaseConfig();
        testCase.setId("test-1");
        testCase.setMethod("GET");
        testCase.setPath("/test");
        svc.setTests(List.of(testCase));

        config.setServices(List.of(svc));

        List<ConfigValidator.ValidationError> errors = ConfigValidator.validate(config);

        assertThat(errors).anyMatch(e -> e.getField().contains("sourceBaseUrl") && e.getMessage().contains("http:// or https://"));
    }

    @Test
    @DisplayName("Catches test path not starting with slash")
    void testPathMissingLeadingSlash() {
        GateDiffConfig config = new GateDiffConfig();
        config.setVersion(1);
        config.getProject().setName("Test Project");

        ServiceConfig svc = new ServiceConfig();
        svc.setId("test-svc");
        svc.setSourceBaseUrl("http://localhost:8081");
        svc.setTargetBaseUrl("http://localhost:8082");

        TestCaseConfig testCase = new TestCaseConfig();
        testCase.setId("test-1");
        testCase.setMethod("GET");
        testCase.setPath("missing-slash");
        svc.setTests(List.of(testCase));

        config.setServices(List.of(svc));

        List<ConfigValidator.ValidationError> errors = ConfigValidator.validate(config);

        assertThat(errors).anyMatch(e -> e.getField().contains("path") && e.getMessage().contains("must start with '/'"));
    }
}
