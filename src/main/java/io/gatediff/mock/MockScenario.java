package io.gatediff.mock;

/**
 * Supported simulation scenarios for the fictional API migration lab.
 */
public enum MockScenario {
    /**
     * Source and Target return identical matching responses (HTTP 200).
     */
    MATCHING,

    /**
     * Target returns a different JSON value (status: INACTIVE vs ACTIVE).
     */
    VALUE_MISMATCH,

    /**
     * Target returns a different HTTP status code (HTTP 404 vs HTTP 200).
     */
    STATUS_MISMATCH,

    /**
     * Target omits a required field ("tier").
     */
    MISSING_FIELD,

    /**
     * Target returns an extra unexpected field ("loyaltyPoints").
     */
    ADDITIONAL_FIELD,

    /**
     * Target returns a simulated server error (HTTP 500).
     */
    SERVER_ERROR,

    /**
     * Target returns a different header (e.g. content-type or x-gateway-provider).
     */
    HEADER_MISMATCH,

    /**
     * Target experiences artificial delay/timeout.
     */
    LATENCY_SPIKE
}
