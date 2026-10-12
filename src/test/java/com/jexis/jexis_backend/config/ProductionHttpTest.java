package com.jexis.jexis_backend.config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.config.import=classpath:application-prod.properties",
        "management.health.rabbit.enabled=false"})
@Import(ProductionHttpTest.FailureRoute.class)
class ProductionHttpTest {
    @Value("${local.server.port}") int port;

    @RestController
    static class FailureRoute {
        @GetMapping("/auth/test-failure")
        String fail() { throw new IllegalStateException("private-database-details"); }
    }

    private HttpResponse<String> get(String path) throws Exception {
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(
                URI.create("http://localhost:" + port + "/api" + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Test void healthIsPublicWithoutDiagnosticDetails() throws Exception {
        var response = get("/actuator/health");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("UP").doesNotContain("components", "details", "jdbc");
        assertThat(get("/actuator/env").statusCode()).isIn(401, 403, 404);
    }

    @Test void swaggerAndOpenApiAreUnavailable() throws Exception {
        assertThat(get("/v3/api-docs").statusCode()).isEqualTo(404);
        assertThat(get("/swagger-ui/index.html").statusCode()).isEqualTo(404);
    }

    @Test void unexpectedErrorsDoNotExposeInternalDetails() throws Exception {
        var response = get("/auth/test-failure");
        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(response.body()).contains("Internal server error").doesNotContain("private-database-details");
    }
}
