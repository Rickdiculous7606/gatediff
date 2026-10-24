package io.gatediff.openapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenApiParserTest {

    @Test
    @DisplayName("Parses Customer API OpenAPI YAML specification successfully")
    void testParseCustomerApiSpec() throws Exception {
        File file = new File("examples/specs/customer-api.yaml");
        OpenApiSpecSummary summary = OpenApiParser.parse(file);

        assertThat(summary.getTitle()).contains("Customer Service");
        assertThat(summary.getOperations()).isNotEmpty();
        assertThat(summary.getOperationCount()).isEqualTo(4);

        OpenApiOperation getCustomer = summary.getOperations().stream()
                .filter(op -> op.getMethod().equals("GET") && op.getPath().equals("/api/customers/{id}"))
                .findFirst()
                .orElse(null);

        assertThat(getCustomer).isNotNull();
        assertThat(getCustomer.getOperationId()).isEqualTo("getCustomerById");
        assertThat(getCustomer.getParameters()).contains("path:id");
        assertThat(getCustomer.getResponseCodes()).contains("200", "404");
    }
}
