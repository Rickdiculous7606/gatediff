package io.gatediff.http;

import io.gatediff.comparison.ComparisonEngine;
import io.gatediff.model.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.*;
import java.util.logging.Logger;

/**
 * Executes parallel or dual HTTP requests against Source and Target gateway endpoints,
 * measures response timings, captures snapshots, and evaluates responses via ComparisonEngine.
 */
public class HttpRunner {

    private static final Logger LOGGER = Logger.getLogger(HttpRunner.class.getName());
    private static final Set<String> SENSITIVE_HEADERS = Set.of("authorization", "cookie", "x-api-key", "token", "proxy-authorization");
    private static final Set<String> IDEMPOTENT_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    private final HttpClient httpClient;
    private final Duration requestTimeout;
    private final int maxRetriesForIdempotent;

    public HttpRunner() {
        this(Duration.ofSeconds(5), Duration.ofSeconds(5), 1);
    }

    public HttpRunner(Duration connectTimeout, Duration requestTimeout, int maxRetriesForIdempotent) {
        this.requestTimeout = requestTimeout != null ? requestTimeout : Duration.ofSeconds(5);
        this.maxRetriesForIdempotent = Math.max(0, maxRetriesForIdempotent);

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout != null ? connectTimeout : Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Executes a migration test case against source and target URLs, then performs comparison.
     */
    public TestExecutionResult executeAndCompare(
            String serviceId,
            String testId,
            String testName,
            String sourceBaseUrl,
            String targetBaseUrl,
            HttpRequestSpec requestSpec,
            ComparisonEngine engine) {

        String sanitizedSourceUrl = buildUrl(sourceBaseUrl, requestSpec.getPath());
        String sanitizedTargetUrl = buildUrl(targetBaseUrl, requestSpec.getPath());

        long startTime = System.currentTimeMillis();

        HttpResponseSnapshot sourceSnapshot;
        try {
            sourceSnapshot = executeRequest(sanitizedSourceUrl, requestSpec);
        } catch (Exception e) {
            return TestExecutionResult.error(serviceId, testId, testName, requestSpec,
                    "Source gateway unreachable at " + sanitizedSourceUrl + ": " + sanitizeErrorMessage(e.getMessage()));
        }

        HttpResponseSnapshot targetSnapshot;
        try {
            targetSnapshot = executeRequest(sanitizedTargetUrl, requestSpec);
        } catch (Exception e) {
            return TestExecutionResult.error(serviceId, testId, testName, requestSpec,
                    "Target gateway unreachable at " + sanitizedTargetUrl + ": " + sanitizeErrorMessage(e.getMessage()));
        }

        long totalDurationMs = System.currentTimeMillis() - startTime;
        ComparisonResult comparison = engine.compare(sourceSnapshot, targetSnapshot);

        return new TestExecutionResult(
                serviceId,
                testId,
                testName,
                requestSpec,
                sourceSnapshot,
                targetSnapshot,
                comparison,
                false,
                null,
                totalDurationMs
        );
    }

    private HttpResponseSnapshot executeRequest(String url, HttpRequestSpec spec) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(requestTimeout);

        // Apply headers (safely ignoring any attempt to inject hostile control chars)
        for (Map.Entry<String, List<String>> entry : spec.getHeaders().entrySet()) {
            String headerName = entry.getKey();
            for (String val : entry.getValue()) {
                builder.header(headerName, val);
            }
        }

        // Apply HTTP Method & Body
        String method = spec.getMethod().toUpperCase();
        HttpRequest.BodyPublisher bodyPublisher = (spec.getBody() != null && !spec.getBody().isEmpty())
                ? HttpRequest.BodyPublishers.ofString(spec.getBody())
                : HttpRequest.BodyPublishers.noBody();

        switch (method) {
            case "GET":
                builder.GET();
                break;
            case "POST":
                builder.POST(bodyPublisher);
                break;
            case "PUT":
                builder.PUT(bodyPublisher);
                break;
            case "DELETE":
                builder.DELETE();
                break;
            default:
                builder.method(method, bodyPublisher);
                break;
        }

        HttpRequest request = builder.build();

        int attempts = 0;
        int maxAttempts = IDEMPOTENT_METHODS.contains(method) ? (1 + maxRetriesForIdempotent) : 1;
        Exception lastException = null;

        while (attempts < maxAttempts) {
            attempts++;
            long start = System.currentTimeMillis();
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                long duration = System.currentTimeMillis() - start;
                return new HttpResponseSnapshot(response.statusCode(), response.headers().map(), response.body(), duration);
            } catch (HttpTimeoutException | ConnectException e) {
                lastException = e;
                if (attempts >= maxAttempts) {
                    throw e;
                }
                // Backoff slightly before retry
                try {
                    Thread.sleep(100L * attempts);
                } catch (InterruptedException ignored) {}
            }
        }

        if (lastException instanceof IOException) {
            throw (IOException) lastException;
        }
        throw new IOException("Failed after " + attempts + " attempts", lastException);
    }

    private String buildUrl(String baseUrl, String path) {
        String base = (baseUrl == null) ? "http://localhost" : baseUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String p = (path == null) ? "" : path.trim();
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        return base + p;
    }

    private String sanitizeErrorMessage(String msg) {
        if (msg == null) return "Unknown network error";
        // Redact any tokens or keys in messages
        return msg.replaceAll("(?i)(authorization|token|key)[:=]\\s*[^\\s]+", "$1=***REDACTED***");
    }
}
