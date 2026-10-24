package io.gatediff.comparison;

import io.gatediff.model.ComparisonResult;
import io.gatediff.model.FieldMismatch;
import io.gatediff.model.HttpResponseSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ComparisonEngineTest {

    private HttpResponseSnapshot snapshot(int status, String body) {
        return new HttpResponseSnapshot(status, Collections.emptyMap(), body, 50);
    }

    private HttpResponseSnapshot snapshotWithHeaders(int status, String body, Map<String, List<String>> headers) {
        return new HttpResponseSnapshot(status, headers, body, 50);
    }

    @Test
    @DisplayName("1. Identical responses pass")
    void testIdenticalResponsesPass() {
        ComparisonEngine engine = new ComparisonEngine();
        String json = "{\"id\":\"C-101\",\"status\":\"ACTIVE\",\"tier\":\"STANDARD\"}";
        HttpResponseSnapshot source = snapshot(200, json);
        HttpResponseSnapshot target = snapshot(200, json);

        ComparisonResult result = engine.compare(source, target);

        assertThat(result.isPassed()).isTrue();
        assertThat(result.getMismatches()).isEmpty();
    }

    @Test
    @DisplayName("2. Different HTTP status codes fail")
    void testDifferentStatusCodesFail() {
        ComparisonEngine engine = new ComparisonEngine();
        String json = "{\"id\":\"C-101\"}";
        HttpResponseSnapshot source = snapshot(200, json);
        HttpResponseSnapshot target = snapshot(404, json);

        ComparisonResult result = engine.compare(source, target);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMismatches()).hasSize(1);
        FieldMismatch mismatch = result.getMismatches().get(0);
        assertThat(mismatch.getType()).isEqualTo(FieldMismatch.Type.STATUS_MISMATCH);
        assertThat(mismatch.getPath()).isEqualTo("$.status");
        assertThat(mismatch.getMessage()).contains("expected HTTP 200 but received HTTP 404");
    }

    @Test
    @DisplayName("3. Different JSON values fail with JSONPath")
    void testDifferentJsonValuesFail() {
        ComparisonEngine engine = new ComparisonEngine();
        HttpResponseSnapshot source = snapshot(200, "{\"id\":\"C-101\",\"status\":\"ACTIVE\"}");
        HttpResponseSnapshot target = snapshot(200, "{\"id\":\"C-101\",\"status\":\"INACTIVE\"}");

        ComparisonResult result = engine.compare(source, target);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMismatches()).hasSize(1);
        FieldMismatch mismatch = result.getMismatches().get(0);
        assertThat(mismatch.getType()).isEqualTo(FieldMismatch.Type.VALUE_MISMATCH);
        assertThat(mismatch.getPath()).isEqualTo("$.status");
        assertThat(mismatch.getMessage()).contains("expected ACTIVE but received INACTIVE");
    }

    @Test
    @DisplayName("4. Missing fields fail")
    void testMissingFieldsFail() {
        ComparisonEngine engine = new ComparisonEngine();
        HttpResponseSnapshot source = snapshot(200, "{\"id\":\"C-101\",\"status\":\"ACTIVE\",\"tier\":\"STANDARD\"}");
        HttpResponseSnapshot target = snapshot(200, "{\"id\":\"C-101\",\"status\":\"ACTIVE\"}");

        ComparisonResult result = engine.compare(source, target);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMismatches()).hasSize(1);
        FieldMismatch mismatch = result.getMismatches().get(0);
        assertThat(mismatch.getType()).isEqualTo(FieldMismatch.Type.MISSING_FIELD);
        assertThat(mismatch.getPath()).isEqualTo("$.tier");
        assertThat(mismatch.getMessage()).contains("missing expected field 'tier'");
    }

    @Test
    @DisplayName("5. Additional fields fail by default")
    void testAdditionalFieldsFailByDefault() {
        ComparisonEngine engine = new ComparisonEngine();
        HttpResponseSnapshot source = snapshot(200, "{\"id\":\"C-101\"}");
        HttpResponseSnapshot target = snapshot(200, "{\"id\":\"C-101\",\"unexpectedField\":\"surprise\"}");

        ComparisonResult result = engine.compare(source, target);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMismatches()).hasSize(1);
        FieldMismatch mismatch = result.getMismatches().get(0);
        assertThat(mismatch.getType()).isEqualTo(FieldMismatch.Type.ADDITIONAL_FIELD);
        assertThat(mismatch.getPath()).isEqualTo("$.unexpectedField");
        assertThat(mismatch.getMessage()).contains("unexpected additional field 'unexpectedField'");
    }

    @Test
    @DisplayName("6. Ignored JSON paths do not fail comparison")
    void testIgnoredJsonPathsDoNotFail() {
        Set<String> ignored = Set.of("$.requestId", "$.meta.timestamp");
        ComparisonEngine engine = new ComparisonEngine(true, true, true, ignored, Collections.emptySet());

        HttpResponseSnapshot source = snapshot(200,
                "{\"id\":\"C-101\",\"requestId\":\"req-source-111\",\"meta\":{\"timestamp\":\"2026-10-24T00:00:00Z\",\"region\":\"us-east\"}}");
        HttpResponseSnapshot target = snapshot(200,
                "{\"id\":\"C-101\",\"requestId\":\"req-target-999\",\"meta\":{\"timestamp\":\"2026-10-24T00:05:00Z\",\"region\":\"us-east\"}}");

        ComparisonResult result = engine.compare(source, target);

        assertThat(result.isPassed()).isTrue();
        assertThat(result.getMismatches()).isEmpty();
    }

    @Test
    @DisplayName("7. Ignored fields do not hide unrelated mismatches")
    void testIgnoredFieldsDoNotHideUnrelatedMismatches() {
        Set<String> ignored = Set.of("$.requestId");
        ComparisonEngine engine = new ComparisonEngine(true, true, true, ignored, Collections.emptySet());

        // requestId differs (ignored), BUT tier also differs (STANDARD vs PREMIUM)
        HttpResponseSnapshot source = snapshot(200, "{\"id\":\"C-101\",\"requestId\":\"req-1\",\"tier\":\"STANDARD\"}");
        HttpResponseSnapshot target = snapshot(200, "{\"id\":\"C-101\",\"requestId\":\"req-2\",\"tier\":\"PREMIUM\"}");

        ComparisonResult result = engine.compare(source, target);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMismatches()).hasSize(1);
        assertThat(result.getMismatches().get(0).getPath()).isEqualTo("$.tier");
        assertThat(result.getMismatches().get(0).getMessage()).contains("expected STANDARD but received PREMIUM");
    }

    @Test
    @DisplayName("8. Configured headers are compared correctly")
    void testConfiguredHeadersComparison() {
        Set<String> headers = Set.of("content-type", "x-gateway-provider");
        ComparisonEngine engine = new ComparisonEngine(true, true, true, Collections.emptySet(), headers);

        Map<String, List<String>> sourceHeaders = Map.of(
                "Content-Type", List.of("application/json; charset=utf-8"),
                "X-Gateway-Provider", List.of("Kong"),
                "X-Internal-Ignored", List.of("source-only")
        );

        Map<String, List<String>> targetHeadersMismatch = Map.of(
                "content-type", List.of("application/json; charset=utf-8"),
                "x-gateway-provider", List.of("Gravitee"),
                "X-Another-Ignored", List.of("target-only")
        );

        HttpResponseSnapshot source = snapshotWithHeaders(200, "{\"ok\":true}", sourceHeaders);
        HttpResponseSnapshot target = snapshotWithHeaders(200, "{\"ok\":true}", targetHeadersMismatch);

        ComparisonResult result = engine.compare(source, target);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMismatches()).hasSize(1);
        FieldMismatch mismatch = result.getMismatches().get(0);
        assertThat(mismatch.getType()).isEqualTo(FieldMismatch.Type.HEADER_MISMATCH);
        assertThat(mismatch.getPath()).isEqualTo("header.x-gateway-provider");
        assertThat(mismatch.getMessage()).contains("expected 'Kong' but received 'Gravitee'");
    }

    @Test
    @DisplayName("9. JSON key order does not affect equality")
    void testJsonKeyOrderDoesNotAffectEquality() {
        ComparisonEngine engine = new ComparisonEngine();
        String json1 = "{\"name\":\"Alpha\",\"id\":\"C-101\",\"active\":true}";
        String json2 = "{\"active\":true,\"id\":\"C-101\",\"name\":\"Alpha\"}";

        HttpResponseSnapshot source = snapshot(200, json1);
        HttpResponseSnapshot target = snapshot(200, json2);

        ComparisonResult result = engine.compare(source, target);

        assertThat(result.isPassed()).isTrue();
        assertThat(result.getMismatches()).isEmpty();
    }

    @Test
    @DisplayName("10. Malformed JSON produces a controlled error rather than an unhandled crash")
    void testMalformedJsonProducesControlledError() {
        ComparisonEngine engine = new ComparisonEngine();
        HttpResponseSnapshot source = snapshot(200, "{\"valid\":\"json\"}");
        HttpResponseSnapshot target = snapshot(200, "{malformed json broken syntax::");

        ComparisonResult result = engine.compare(source, target);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMismatches()).hasSize(1);
        FieldMismatch mismatch = result.getMismatches().get(0);
        assertThat(mismatch.getType()).isEqualTo(FieldMismatch.Type.MALFORMED_JSON);
        assertThat(mismatch.getMessage()).contains("target response was malformed/non-JSON");
    }

    @Test
    @DisplayName("11. Array order sensitivity: array index order matters by default")
    void testArrayOrderSensitivity() {
        ComparisonEngine engine = new ComparisonEngine();
        HttpResponseSnapshot source = snapshot(200, "{\"roles\":[\"ADMIN\",\"USER\"]}");
        HttpResponseSnapshot target = snapshot(200, "{\"roles\":[\"USER\",\"ADMIN\"]}");

        ComparisonResult result = engine.compare(source, target);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMismatches()).hasSize(2);
        assertThat(result.getMismatches().get(0).getPath()).isEqualTo("$.roles[0]");
        assertThat(result.getMismatches().get(0).getMessage()).contains("expected ADMIN but received USER");
    }

    @Test
    @DisplayName("12. Null values handled explicitly")
    void testNullValuesHandledExplicitly() {
        ComparisonEngine engine = new ComparisonEngine();
        HttpResponseSnapshot source = snapshot(200, "{\"email\":null}");
        HttpResponseSnapshot target = snapshot(200, "{\"email\":\"user@example.com\"}");

        ComparisonResult result = engine.compare(source, target);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getMismatches()).hasSize(1);
        assertThat(result.getMismatches().get(0).getPath()).isEqualTo("$.email");
    }
}
