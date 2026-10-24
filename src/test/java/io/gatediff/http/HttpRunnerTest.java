package io.gatediff.http;

import io.gatediff.comparison.ComparisonEngine;
import io.gatediff.mock.LocalMockLab;
import io.gatediff.mock.MockScenario;
import io.gatediff.model.HttpRequestSpec;
import io.gatediff.model.TestExecutionResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpRunnerTest {

    private static LocalMockLab lab;
    private static HttpRunner runner;

    @BeforeAll
    static void init() throws IOException {
        lab = new LocalMockLab(19081, 19082, MockScenario.MATCHING);
        lab.start();
        runner = new HttpRunner(Duration.ofSeconds(2), Duration.ofSeconds(2), 0);
    }

    @AfterAll
    static void cleanup() {
        if (lab != null) {
            lab.stop();
        }
    }

    @Test
    @DisplayName("Executes and compares matching GET request against mock lab")
    void testMatchingGetExecution() {
        lab.setScenario(MockScenario.MATCHING);
        ComparisonEngine engine = new ComparisonEngine(true, true, true, Set.of("$.requestId"), Collections.emptySet());
        HttpRequestSpec req = new HttpRequestSpec("GET", "/api/customers/C-101", Collections.emptyMap(), null);

        TestExecutionResult result = runner.executeAndCompare(
                "customer-api",
                "test-get-customer",
                "Get Customer C-101",
                lab.getSourceBaseUrl(),
                lab.getTargetBaseUrl(),
                req,
                engine
        );

        assertThat(result.isExecutionError()).isFalse();
        assertThat(result.isPassed()).isTrue();
        assertThat(result.getSourceResponse().getStatusCode()).isEqualTo(200);
        assertThat(result.getTargetResponse().getStatusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("Detects value mismatch when target returns INACTIVE")
    void testValueMismatchDetection() {
        lab.setScenario(MockScenario.VALUE_MISMATCH);
        ComparisonEngine engine = new ComparisonEngine(true, true, true, Set.of("$.requestId"), Collections.emptySet());
        HttpRequestSpec req = new HttpRequestSpec("GET", "/api/customers/C-101", Collections.emptyMap(), null);

        TestExecutionResult result = runner.executeAndCompare(
                "customer-api",
                "test-get-customer",
                "Get Customer C-101",
                lab.getSourceBaseUrl(),
                lab.getTargetBaseUrl(),
                req,
                engine
        );

        assertThat(result.isExecutionError()).isFalse();
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getComparison().getMismatches()).isNotEmpty();
        assertThat(result.getComparison().getMismatches().get(0).getPath()).isEqualTo("$.status");
    }

    @Test
    @DisplayName("Gracefully catches unreachable endpoint error with clean message")
    void testUnreachableEndpointError() {
        ComparisonEngine engine = new ComparisonEngine();
        HttpRequestSpec req = new HttpRequestSpec("GET", "/api/customers/C-101", Collections.emptyMap(), null);

        // Port 19999 is closed
        TestExecutionResult result = runner.executeAndCompare(
                "customer-api",
                "test-unreachable",
                "Unreachable target",
                lab.getSourceBaseUrl(),
                "http://localhost:19999",
                req,
                engine
        );

        assertThat(result.isExecutionError()).isTrue();
        assertThat(result.isPassed()).isFalse();
        assertThat(result.getExecutionErrorMessage()).contains("Target gateway unreachable");
    }
}
