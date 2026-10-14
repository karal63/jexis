package com.jexis.jexis_backend.config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.config.import=classpath:application-dev.properties",
        "management.health.rabbit.enabled=false"})
class DevelopmentHttpTest {
    @Value("${local.server.port}") int port;

    @Test void developerDocumentationIsAvailable() throws Exception {
        var client = HttpClient.newHttpClient();
        for (String path : new String[]{"/v3/api-docs", "/swagger-ui/index.html"}) {
            var response = client.send(HttpRequest.newBuilder(
                    URI.create("http://localhost:" + port + "/api" + path)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).as(path).isEqualTo(200);
        }
    }
}
