package com.jexis.jexis_backend.common.ratelimit;

import java.net.URI;
import java.net.http.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"management.health.rabbit.enabled=false", "server.forward-headers-strategy=none"})
class AuthRateLimitHttpTest {
    @Value("${local.server.port}") int port;
    @MockitoBean RateLimitService limits;

    private HttpResponse<String> send(String method, String path) throws Exception {
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(
                URI.create("http://localhost:" + port + "/api" + path))
                .header("X-Forwarded-For", "attacker-chosen-ip")
                .method(method, HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test void limitsApplyBeforeValidationAndUseActualClientAddress() throws Exception {
        doThrow(new RateLimitException(12)).when(limits).check(eq("login-ip"), anyString());
        var response = send("POST", "/auth/login");
        assertThat(response.statusCode()).isEqualTo(429);
        assertThat(response.headers().firstValue("Retry-After")).contains("12");
        assertThat(response.body()).contains("RATE_LIMIT_EXCEEDED");
        verify(limits).check(eq("login-ip"), argThat(ip -> !ip.equals("attacker-chosen-ip")));
    }

    @Test void loginIdentityLimitReturns429BeforePasswordLookup() throws Exception {
        doThrow(new RateLimitException(12)).when(limits).check(eq("login-identity"), anyString());
        var response = HttpClient.newHttpClient().send(HttpRequest.newBuilder(
                URI.create("http://localhost:" + port + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"email\":\"USER@example.com\",\"password\":\"password123\"}"))
                .build(), HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(429);
        assertThat(response.headers().firstValue("Retry-After")).contains("12");
        verify(limits).check(eq("login-identity"), endsWith("|user@example.com"));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({
        "POST,/auth/signup,signup-ip", "POST,/auth/refresh,refresh-ip",
        "POST,/users/reset-password/confirm,confirm-ip", "GET,/user/activate,activate-ip"
    })
    void remainingRoutesAreLimited(String method, String path, String policy) throws Exception {
        doThrow(new RateLimitException(10)).when(limits).check(eq(policy), anyString());
        assertThat(send(method, path).statusCode()).isEqualTo(429);
    }

    @Test void bothResetRoutesShareIpPolicyIncludingAuthenticatedRoute() throws Exception {
        doThrow(new RateLimitException(20)).when(limits).check(eq("reset-ip"), anyString());
        assertThat(send("POST", "/auth/forgot-password").statusCode()).isEqualTo(429);
        assertThat(send("POST", "/users/change-password").statusCode()).isEqualTo(429);
        verify(limits, times(2)).check(eq("reset-ip"), anyString());
    }

    @Test void unavailableStorageReturns503() throws Exception {
        doThrow(new RateLimitException(new IllegalStateException("private redis details")))
                .when(limits).check(eq("signup-ip"), anyString());
        var response = send("POST", "/auth/signup");
        assertThat(response.statusCode()).isEqualTo(503);
        assertThat(response.body()).doesNotContain("private redis details");
        assertThat(response.headers().firstValue("Retry-After")).contains("5");
    }

    @Test void healthAndWebhooksDoNotConsumeAuthAllowance() throws Exception {
        assertThat(send("GET", "/actuator/health").statusCode()).isEqualTo(200);
        send("POST", "/webhooks/authorizations");
        verifyNoInteractions(limits);
    }
}
