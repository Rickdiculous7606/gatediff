package io.gatediff.reporting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.gatediff.model.TestSuiteReport;

import java.io.File;
import java.io.IOException;

/**
 * Serializes migration test reports to machine-readable JSON for CI/CD integration.
 */
public class JsonReporter {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static String toJson(TestSuiteReport report) throws IOException {
        return MAPPER.writeValueAsString(report);
    }

    public static void writeToFile(TestSuiteReport report, File destination) throws IOException {
        if (destination.getParentFile() != null && !destination.getParentFile().exists()) {
            destination.getParentFile().mkdirs();
        }
        MAPPER.writeValue(destination, report);
    }
}
