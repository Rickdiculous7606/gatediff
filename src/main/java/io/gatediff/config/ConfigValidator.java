package io.gatediff.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates a GateDiff migration configuration according to version 1 schema rules.
 */
public class ConfigValidator {

    public static class ValidationError {
        private final String field;
        private final String message;

        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "[" + field + "]: " + message;
        }
    }

    public static List<ValidationError> validate(GateDiffConfig config) {
        List<ValidationError> errors = new ArrayList<>();

        if (config == null) {
            errors.add(new ValidationError("root", "Configuration file is empty or could not be parsed"));
            return errors;
        }

        // 1. Version check
        if (config.getVersion() == null || config.getVersion() != 1) {
            errors.add(new ValidationError("version", "Unsupported schema version '" + config.getVersion() + "'. GateDiff MVP currently supports version: 1"));
        }

        // 2. Project check
        if (config.getProject() == null || config.getProject().getName() == null || config.getProject().getName().isBlank()) {
            errors.add(new ValidationError("project.name", "Project name is required"));
        }

        // 3. Services check
        List<ServiceConfig> services = config.getServices();
        if (services == null || services.isEmpty()) {
            errors.add(new ValidationError("services", "At least one service must be configured under 'services'"));
            return errors;
        }

        for (int i = 0; i < services.size(); i++) {
            ServiceConfig s = services.get(i);
            String svcPrefix = "services[" + i + "]";

            if (s.getId() == null || s.getId().isBlank()) {
                errors.add(new ValidationError(svcPrefix + ".id", "Service ID is required"));
            }

            validateUrl(s.getSourceBaseUrl(), svcPrefix + ".sourceBaseUrl", errors);
            validateUrl(s.getTargetBaseUrl(), svcPrefix + ".targetBaseUrl", errors);

            List<TestCaseConfig> tests = s.getTests();
            if (tests == null || tests.isEmpty()) {
                errors.add(new ValidationError(svcPrefix + ".tests", "Service '" + s.getId() + "' has no test cases configured"));
            } else {
                for (int j = 0; j < tests.size(); j++) {
                    TestCaseConfig t = tests.get(j);
                    String testPrefix = svcPrefix + ".tests[" + j + "]";

                    if (t.getId() == null || t.getId().isBlank()) {
                        errors.add(new ValidationError(testPrefix + ".id", "Test case ID is required"));
                    }

                    if (t.getPath() == null || t.getPath().isBlank()) {
                        errors.add(new ValidationError(testPrefix + ".path", "Test case path is required"));
                    } else if (!t.getPath().startsWith("/")) {
                        errors.add(new ValidationError(testPrefix + ".path", "Test case path must start with '/' (got '" + t.getPath() + "')"));
                    }

                    if (t.getMethod() == null || !isValidHttpMethod(t.getMethod())) {
                        errors.add(new ValidationError(testPrefix + ".method", "Unsupported HTTP method '" + t.getMethod() + "' in test '" + t.getId() + "'"));
                    }
                }
            }
        }

        return errors;
    }

    private static void validateUrl(String url, String fieldName, List<ValidationError> errors) {
        if (url == null || url.isBlank()) {
            errors.add(new ValidationError(fieldName, "Base URL is required"));
            return;
        }

        try {
            URI uri = URI.create(url);
            if (uri.getScheme() == null || (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https"))) {
                errors.add(new ValidationError(fieldName, "Base URL must use http:// or https:// protocol (got '" + url + "')"));
            }
        } catch (Exception e) {
            errors.add(new ValidationError(fieldName, "Invalid URL format: " + e.getMessage()));
        }
    }

    private static boolean isValidHttpMethod(String method) {
        String m = method.toUpperCase();
        return m.equals("GET") || m.equals("POST") || m.equals("PUT") || m.equals("PATCH") || m.equals("DELETE") || m.equals("HEAD") || m.equals("OPTIONS");
    }
}
