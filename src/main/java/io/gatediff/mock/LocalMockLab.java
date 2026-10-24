package io.gatediff.mock;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.Executors;

/**
 * Fictional API migration lab hosting lightweight embedded HTTP mock servers
 * representing a Source Gateway (e.g., Kong on 8081) and Target Gateway (e.g., Gravitee on 8082).
 * <p>
 * Bound strictly to 127.0.0.1 (localhost) to guarantee security.
 * Uses only Java's built-in HttpServer (no external web frameworks).
 */
public class LocalMockLab implements AutoCloseable {

    private final int sourcePort;
    private final int targetPort;
    private volatile MockScenario scenario;

    private HttpServer sourceServer;
    private HttpServer targetServer;

    public LocalMockLab() {
        this(8081, 8082, MockScenario.MATCHING);
    }

    public LocalMockLab(int sourcePort, int targetPort, MockScenario scenario) {
        this.sourcePort = sourcePort;
        this.targetPort = targetPort;
        this.scenario = (scenario != null) ? scenario : MockScenario.MATCHING;
    }

    public synchronized void start() throws IOException {
        if (sourceServer != null || targetServer != null) {
            return; // Already started
        }

        // Source Server (e.g. Kong gateway on 8081)
        sourceServer = HttpServer.create(new InetSocketAddress("127.0.0.1", sourcePort), 0);
        sourceServer.createContext("/api/customers", new SourceCustomerHandler());
        sourceServer.createContext("/health", exchange -> sendResponse(exchange, 200, "{\"status\":\"UP\",\"gateway\":\"source-kong\"}", "application/json"));
        sourceServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        sourceServer.start();

        // Target Server (e.g. Gravitee gateway on 8082)
        targetServer = HttpServer.create(new InetSocketAddress("127.0.0.1", targetPort), 0);
        targetServer.createContext("/api/customers", new TargetCustomerHandler());
        targetServer.createContext("/health", exchange -> sendResponse(exchange, 200, "{\"status\":\"UP\",\"gateway\":\"target-gravitee\"}", "application/json"));
        targetServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        targetServer.start();
    }

    public synchronized void stop() {
        if (sourceServer != null) {
            sourceServer.stop(0);
            sourceServer = null;
        }
        if (targetServer != null) {
            targetServer.stop(0);
            targetServer = null;
        }
    }

    @Override
    public void close() {
        stop();
    }

    public void setScenario(MockScenario scenario) {
        this.scenario = (scenario != null) ? scenario : MockScenario.MATCHING;
    }

    public MockScenario getScenario() {
        return scenario;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public String getSourceBaseUrl() {
        return "http://localhost:" + sourcePort;
    }

    public String getTargetBaseUrl() {
        return "http://localhost:" + targetPort;
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String responseBody, String contentType) throws IOException {
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("X-Gateway-Timestamp", Instant.now().toString());
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private class SourceCustomerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("X-Gateway-Provider", "Kong-Enterprise-3.4");
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                if (path.endsWith("/C-101")) {
                    String json = "{\"id\":\"C-101\",\"status\":\"ACTIVE\",\"tier\":\"STANDARD\",\"name\":\"Acme Corp\",\"requestId\":\"req-src-123\"}";
                    sendResponse(exchange, 200, json, "application/json; charset=utf-8");
                } else if (path.equals("/api/customers") || path.equals("/api/customers/")) {
                    String json = "[{\"id\":\"C-101\",\"status\":\"ACTIVE\",\"tier\":\"STANDARD\"},{\"id\":\"C-102\",\"status\":\"PENDING\",\"tier\":\"PREMIUM\"}]";
                    sendResponse(exchange, 200, json, "application/json; charset=utf-8");
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Customer not found\"}", "application/json");
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                sendResponse(exchange, 201, "{\"id\":\"C-999\",\"status\":\"CREATED\"}", "application/json");
            } else if ("DELETE".equalsIgnoreCase(method)) {
                sendResponse(exchange, 204, "", "application/json");
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}", "application/json");
            }
        }
    }

    private class TargetCustomerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("X-Gateway-Provider", "Gravitee-APIM-4.2");
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if (scenario == MockScenario.LATENCY_SPIKE) {
                try {
                    Thread.sleep(800);
                } catch (InterruptedException ignored) {}
            }

            switch (scenario) {
                case STATUS_MISMATCH:
                    sendResponse(exchange, 404, "{\"error\":\"Resource not found in target gateway\"}", "application/json");
                    return;

                case SERVER_ERROR:
                    sendResponse(exchange, 500, "{\"error\":\"Internal Server Error in target gateway policy engine\"}", "application/json");
                    return;

                case HEADER_MISMATCH:
                    exchange.getResponseHeaders().set("X-Gateway-Provider", "Gravitee-DIFFERENT-HEADER");
                    sendResponse(exchange, 200, "{\"id\":\"C-101\",\"status\":\"ACTIVE\",\"tier\":\"STANDARD\",\"requestId\":\"req-tgt-456\"}", "text/plain");
                    return;

                case VALUE_MISMATCH:
                    String mismatchJson = "{\"id\":\"C-101\",\"status\":\"INACTIVE\",\"tier\":\"STANDARD\",\"requestId\":\"req-tgt-456\"}";
                    sendResponse(exchange, 200, mismatchJson, "application/json; charset=utf-8");
                    return;

                case MISSING_FIELD:
                    // Omits "tier"
                    String missingJson = "{\"id\":\"C-101\",\"status\":\"ACTIVE\",\"requestId\":\"req-tgt-456\"}";
                    sendResponse(exchange, 200, missingJson, "application/json; charset=utf-8");
                    return;

                case ADDITIONAL_FIELD:
                    // Extra "loyaltyPoints"
                    String extraJson = "{\"id\":\"C-101\",\"status\":\"ACTIVE\",\"tier\":\"STANDARD\",\"loyaltyPoints\":450,\"requestId\":\"req-tgt-456\"}";
                    sendResponse(exchange, 200, extraJson, "application/json; charset=utf-8");
                    return;

                case MATCHING:
                default:
                    if ("GET".equalsIgnoreCase(method)) {
                        if (path.endsWith("/C-101")) {
                            String json = "{\"id\":\"C-101\",\"status\":\"ACTIVE\",\"tier\":\"STANDARD\",\"name\":\"Acme Corp\",\"requestId\":\"req-tgt-789\"}";
                            sendResponse(exchange, 200, json, "application/json; charset=utf-8");
                        } else if (path.equals("/api/customers") || path.equals("/api/customers/")) {
                            String json = "[{\"id\":\"C-101\",\"status\":\"ACTIVE\",\"tier\":\"STANDARD\"},{\"id\":\"C-102\",\"status\":\"PENDING\",\"tier\":\"PREMIUM\"}]";
                            sendResponse(exchange, 200, json, "application/json; charset=utf-8");
                        } else {
                            sendResponse(exchange, 404, "{\"error\":\"Customer not found\"}", "application/json");
                        }
                    } else if ("POST".equalsIgnoreCase(method)) {
                        sendResponse(exchange, 201, "{\"id\":\"C-999\",\"status\":\"CREATED\"}", "application/json");
                    } else if ("DELETE".equalsIgnoreCase(method)) {
                        sendResponse(exchange, 204, "", "application/json");
                    } else {
                        sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}", "application/json");
                    }
                    return;
            }
        }
    }
}
