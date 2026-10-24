package io.gatediff.mapping;

import io.gatediff.openapi.OpenApiOperation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MappingSuggestionEngineTest {

    @Test
    @DisplayName("Matches identical operations with CONFIRMED status")
    void testExactMatchConfirmed() {
        OpenApiOperation src = new OpenApiOperation("GET", "/api/customers/{id}", "getCustomer", "", List.of(), List.of("200"));
        OpenApiOperation tgt = new OpenApiOperation("GET", "/api/customers/{id}", "getCustomer", "", List.of(), List.of("200"));

        MappingSuggestionEngine engine = new MappingSuggestionEngine();
        List<OperationMapping> mappings = engine.mapOperations(List.of(src), List.of(tgt));

        assertThat(mappings).hasSize(1);
        OperationMapping m = mappings.get(0);
        assertThat(m.getStatus()).isEqualTo(MappingStatus.CONFIRMED);
        assertThat(m.getConfidenceScore()).isGreaterThanOrEqualTo(90);
        assertThat(m.getTargetOperation()).isEqualTo(tgt);
    }

    @Test
    @DisplayName("Matches normalized path parameters with high confidence")
    void testNormalizedParameterMatch() {
        OpenApiOperation src = new OpenApiOperation("GET", "/api/customers/{id}", "getCustomer", "", List.of(), List.of());
        OpenApiOperation tgt = new OpenApiOperation("GET", "/api/customers/{customerId}", "getCustomer", "", List.of(), List.of());

        MappingSuggestionEngine engine = new MappingSuggestionEngine();
        List<OperationMapping> mappings = engine.mapOperations(List.of(src), List.of(tgt));

        assertThat(mappings).hasSize(1);
        OperationMapping m = mappings.get(0);
        assertThat(m.getStatus()).isEqualTo(MappingStatus.CONFIRMED);
        assertThat(m.getTargetOperation()).isEqualTo(tgt);
    }

    @Test
    @DisplayName("Flags ambiguous mappings when multiple targets have equal score")
    void testAmbiguousMappingDetection() {
        OpenApiOperation src = new OpenApiOperation("GET", "/api/v1/resource", "getResource", "", List.of(), List.of());
        OpenApiOperation tgt1 = new OpenApiOperation("GET", "/api/v2/resource", "getResourceV2", "", List.of(), List.of());
        OpenApiOperation tgt2 = new OpenApiOperation("GET", "/api/v3/resource", "getResourceV3", "", List.of(), List.of());

        MappingSuggestionEngine engine = new MappingSuggestionEngine();
        List<OperationMapping> mappings = engine.mapOperations(List.of(src), List.of(tgt1, tgt2));

        assertThat(mappings).hasSize(1);
        OperationMapping m = mappings.get(0);
        assertThat(m.getStatus()).isEqualTo(MappingStatus.AMBIGUOUS);
        assertThat(m.getCandidateOperations()).hasSize(2);
    }

    @Test
    @DisplayName("Marks unmatched operations as UNRESOLVED")
    void testUnresolvedOperation() {
        OpenApiOperation src = new OpenApiOperation("DELETE", "/api/secret-cleanup", "cleanup", "", List.of(), List.of());
        OpenApiOperation tgt = new OpenApiOperation("GET", "/api/customers", "listCustomers", "", List.of(), List.of());

        MappingSuggestionEngine engine = new MappingSuggestionEngine();
        List<OperationMapping> mappings = engine.mapOperations(List.of(src), List.of(tgt));

        assertThat(mappings).hasSize(1);
        assertThat(mappings.get(0).getStatus()).isEqualTo(MappingStatus.UNRESOLVED);
    }
}
