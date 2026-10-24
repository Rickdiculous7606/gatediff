package io.gatediff.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalMockLabTest {

    private LocalMockLab lab;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        lab = new LocalMockLab(18081, 18082, MockScenario.MATCHING);
        lab.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        if (lab != null) {
            lab.stop();
        }
    }

    @Test
    @DisplayName("Mock lab starts and serves health checks on localhost")
    void testMockLabHealth() throws Exception {
        HttpRequest srcReq = HttpRequest.newBuilder().uri(URI.create("http://localhost:18081/health")).GET().build();
        HttpResponse<String> srcRes = client.send(srcReq, HttpResponse.BodyHandlers.ofString());
        assertThat(srcRes.statusCode()).isEqualTo(200);
        assertThat(srcRes.body()).contains("source-kong");

        HttpRequest tgtReq = HttpRequest.newBuilder().uri(URI.create("http://localhost:18082/health")).GET().build();
        HttpResponse<String> tgtRes = client.send(tgtReq, HttpResponse.BodyHandlers.ofString());
        assertThat(tgtRes.statusCode()).isEqualTo(200);
        assertThat(tgtRes.body()).contains("target-gravitee");
    }

    @Test
    @DisplayName("Matching scenario returns matching customer payload")
    void testMatchingScenario() throws Exception {
        lab.setScenario(MockScenario.MATCHING);

        HttpRequest srcReq = HttpRequest.newBuilder().uri(URI.create("http://localhost:18081/api/customers/C-101")).GET().build();
        HttpResponse<String> srcRes = client.send(srcReq, HttpResponse.BodyHandlers.ofString());

        HttpRequest tgtReq = HttpRequest.newBuilder().uri(URI.create("http://localhost:18082/api/customers/C-101")).GET().build();
        HttpResponse<String> tgtRes = client.send(tgtReq, HttpResponse.BodyHandlers.ofString());

        assertThat(srcRes.statusCode()).isEqualTo(200);
        assertThat(tgtRes.statusCode()).isEqualTo(200);
        assertThat(srcRes.body()).contains("\"status\":\"ACTIVE\"");
        assertThat(tgtRes.body()).contains("\"status\":\"ACTIVE\"");
    }

    @Test
    @DisplayName("Status mismatch scenario returns 404 on target")
    void testStatusMismatchScenario() throws Exception {
        lab.setScenario(MockScenario.STATUS_MISMATCH);

        HttpRequest tgtReq = HttpRequest.newBuilder().uri(URI.create("http://localhost:18082/api/customers/C-101")).GET().build();
        HttpResponse<String> tgtRes = client.send(tgtReq, HttpResponse.BodyHandlers.ofString());

        assertThat(tgtRes.statusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("Server error scenario returns 500 on target")
    void testServerErrorScenario() throws Exception {
        lab.setScenario(MockScenario.SERVER_ERROR);

        HttpRequest tgtReq = HttpRequest.newBuilder().uri(URI.create("http://localhost:18082/api/customers/C-101")).GET().build();
        HttpResponse<String> tgtRes = client.send(tgtReq, HttpResponse.BodyHandlers.ofString());

        assertThat(tgtRes.statusCode()).isEqualTo(500);
    }
}
