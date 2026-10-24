package io.gatediff.model;

/**
 * Details of an individual mismatch between source and target responses.
 */
public class FieldMismatch {

    public enum Type {
        STATUS_MISMATCH,
        VALUE_MISMATCH,
        MISSING_FIELD,
        ADDITIONAL_FIELD,
        HEADER_MISMATCH,
        MALFORMED_JSON,
        TYPE_MISMATCH
    }

    private final Type type;
    private final String path;
    private final Object expected;
    private final Object actual;
    private final String message;

    public FieldMismatch(Type type, String path, Object expected, Object actual, String message) {
        this.type = type;
        this.path = path;
        this.expected = expected;
        this.actual = actual;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public Object getExpected() {
        return expected;
    }

    public Object getActual() {
        return actual;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return path + ": " + message;
    }
}
