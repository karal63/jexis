package com.jexis.jexis_backend.auth;

import com.jexis.jexis_backend.auth.application.dto.TokenPair;
import com.jexis.jexis_backend.auth.application.security.AuthSessionService;
import com.jexis.jexis_backend.auth.application.useCases.RefreshTokensUseCase;
import com.jexis.jexis_backend.auth.domain.entities.AuthSession;
import com.jexis.jexis_backend.auth.domain.exception.InvalidRtException;
import com.jexis.jexis_backend.auth.infrastructure.AuthSessionRepository;
import com.jexis.jexis_backend.auth.infrastructure.security.JwtUtil;
import com.jexis.jexis_backend.common.hashUtils.HashUtils;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;
import com.jexis.jexis_backend.user.application.useCases.ChangePasswordUseCase;
import com.jexis.jexis_backend.user.application.useCases.DeleteUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.domain.enums.UserRole;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;
import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.time.LocalDateTime;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:auth_sessions;MODE=PostgreSQL;NON_KEYWORDS=KEY,VALUE,INTERVAL;DB_CLOSE_DELAY=-1",
        "management.health.rabbit.enabled=false"})
class AuthSessionIntegrationTest {
    @Autowired AuthSessionService sessions;
    @Autowired AuthSessionRepository repository;
    @Autowired UserRepository users;
    @Autowired MemberRepository members;
    @Autowired com.jexis.jexis_backend.account.infrastructure.AccountRepository accounts;
    @Autowired RefreshTokensUseCase refresh;
    @Autowired ChangePasswordUseCase changePassword;
    @Autowired DeleteUserUseCase deleteUser;
    @Autowired JwtUtil jwt;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    com.jexis.jexis_backend.emailService.infrastructure.messaging.EmailProducer emailProducer;
    @Autowired com.jexis.jexis_backend.passwordResetToken.infrastructure.PasswordResetTokenRepository resetTokens;
    @Value("${local.server.port}") int port;

    @DynamicPropertySource
    static void optionalPostgres(DynamicPropertyRegistry properties) {
        String url = System.getenv("TEST_AUTH_DATABASE_URL");
        if (url == null) return;
        // Supply only a disposable database: this suite uses create-drop.
        properties.add("spring.datasource.url", () -> url);
        properties.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        properties.add("spring.datasource.username", () -> System.getenv("TEST_AUTH_DATABASE_USERNAME"));
        properties.add("spring.datasource.password", () -> System.getenv().getOrDefault("TEST_AUTH_DATABASE_PASSWORD", ""));
    }

    private User user() {
        String unique = UUID.randomUUID().toString();
        return users.saveAndFlush(new User("Session", "Test", unique + "@example.com", unique,
                "unused-hash", null, List.of(UserRole.USER)));
    }

    private AuthSession stored(TokenPair pair) {
        UUID id = UUID.fromString(jwt.extractClaimsRt(pair.getRefreshToken()).get("sid", String.class));
        return repository.findById(id).orElseThrow();
    }

    private HttpResponse<String> request(String method, String path, String cookie, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api" + path));
        if (cookie != null) builder.header("Cookie", cookie);
        if (body != null) builder.header("Content-Type", "application/json");
        return HttpClient.newHttpClient().send(builder.method(method, body == null
                ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private String cookie(TokenPair pair) { return "access_token=" + pair.getAccessToken(); }

    @Test void refreshRotatesUniqueTokensAndStoresOnlyTheirHash() {
        User user = user();
        TokenPair initial = sessions.create(user.getId());
        TokenPair rotated = refresh.execute(initial.getRefreshToken()).tokens();
        assertThat(rotated.getRefreshToken()).isNotEqualTo(initial.getRefreshToken());
        assertThat(rotated.getAccessToken()).isNotEqualTo(initial.getAccessToken());
        assertThat(stored(rotated).getRefreshTokenHash()).isEqualTo(HashUtils.sha256(rotated.getRefreshToken()))
                .isNotEqualTo(rotated.getRefreshToken());
        assertThat(refresh.execute(rotated.getRefreshToken()).tokens().getRefreshToken()).isNotBlank();
    }

    @Test void replayRevokesTheRotatedSessionButNotOtherDevices() throws Exception {
        User user = user();
        TokenPair initial = sessions.create(user.getId());
        TokenPair otherDevice = sessions.create(user.getId());
        TokenPair rotated = refresh.execute(initial.getRefreshToken()).tokens();
        assertThatThrownBy(() -> refresh.execute(initial.getRefreshToken())).isInstanceOf(InvalidRtException.class);
        assertThat(stored(rotated).getRevokedAt()).isNotNull();
        assertThatThrownBy(() -> refresh.execute(rotated.getRefreshToken())).isInstanceOf(InvalidRtException.class);
        assertThat(request("GET", "/accounts/me", cookie(rotated), null).statusCode()).isEqualTo(401);
        assertThat(request("GET", "/accounts/me", cookie(otherDevice), null).statusCode()).isEqualTo(200);
    }

    @Test void concurrentRefreshCannotIssueTwoUsableSuccessors() throws Exception {
        TokenPair initial = sessions.create(user().getId());
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Boolean> attempt = () -> {
            ready.countDown();
            if (!start.await(5, TimeUnit.SECONDS)) throw new AssertionError("Refresh barrier timed out");
            int status = request("POST", "/auth/refresh", cookie(initial)
                    + "; refresh_token=" + initial.getRefreshToken(), null).statusCode();
            assertThat(status).isIn(200, 401);
            return status == 200;
        };
        try {
            Future<Boolean> first = executor.submit(attempt);
            Future<Boolean> second = executor.submit(attempt);
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
            assertThat(stored(initial).getRevokedAt()).isNotNull();
        } finally { executor.shutdownNow(); }
    }

    @Test void logoutRevokesBothTokensAndClearsCookies() throws Exception {
        TokenPair pair = sessions.create(user().getId());
        var response = request("POST", "/auth/logout", cookie(pair) + "; refresh_token=" + pair.getRefreshToken(), null);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().allValues("Set-Cookie")).hasSize(2)
                .allSatisfy(value -> assertThat(value).contains("Max-Age=0", "HttpOnly"));
        assertThat(request("GET", "/accounts/me", cookie(pair), null).statusCode()).isEqualTo(401);
        assertThatThrownBy(() -> refresh.execute(pair.getRefreshToken())).isInstanceOf(InvalidRtException.class);
    }

    @Test void logoutCanUseAccessTokenWhenRefreshCookieIsMissing() throws Exception {
        TokenPair pair = sessions.create(user().getId());
        assertThat(request("GET", "/auth/logout", cookie(pair), null).statusCode()).isEqualTo(200);
        assertThat(stored(pair).getRevokedAt()).isNotNull();
        assertThat(request("POST", "/auth/logout", null, null).statusCode()).isEqualTo(200);
    }

    @Test void passwordChangeRevokesEveryDeviceImmediately() throws Exception {
        User user = user();
        TokenPair first = sessions.create(user.getId());
        TokenPair second = sessions.create(user.getId());
        changePassword.execute(user.getId(), "new-password-123");
        for (TokenPair pair : List.of(first, second)) {
            assertThatThrownBy(() -> refresh.execute(pair.getRefreshToken())).isInstanceOf(InvalidRtException.class);
            assertThat(request("GET", "/accounts/me", cookie(pair), null).statusCode()).isEqualTo(401);
        }
    }

    @Test void failedPasswordTransactionDoesNotLeaveSessionsRevoked() {
        User user = user();
        TokenPair pair = sessions.create(user.getId());
        var transaction = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            changePassword.execute(user.getId(), "new-password-123");
            throw new IllegalStateException("simulated failure before commit");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(users.findById(user.getId()).orElseThrow().getPassword()).isEqualTo("unused-hash");
        assertThat(stored(pair).getRevokedAt()).isNull();
        assertThat(refresh.execute(pair.getRefreshToken()).tokens().getRefreshToken()).isNotBlank();
    }

    @Test void passwordResetRevokesSessionsThroughThePublicEndpoint() throws Exception {
        User user = user();
        TokenPair pair = sessions.create(user.getId());
        String token = UUID.randomUUID().toString();
        resetTokens.saveAndFlush(new com.jexis.jexis_backend.passwordResetToken.domain.entities.PasswordResetToken(
                user, HashUtils.sha256(token), LocalDateTime.now().plusMinutes(30), LocalDateTime.now()));
        var response = request("POST", "/users/reset-password/confirm", null,
                "{\"token\":\"" + token + "\",\"password\":\"new-password-123\"}");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(stored(pair).getRevokedAt()).isNotNull();
        assertThatThrownBy(() -> refresh.execute(pair.getRefreshToken())).isInstanceOf(InvalidRtException.class);
        assertThat(request("GET", "/accounts/me", cookie(pair), null).statusCode()).isEqualTo(401);
    }

    @Test void authenticationUsesCurrentRolesInsteadOfTheJwtSnapshot() throws Exception {
        User user = user();
        user.setRoles(List.of(UserRole.ADMIN));
        users.saveAndFlush(user);
        TokenPair pair = sessions.create(user.getId());
        assertThat(request("GET", "/admin/users", cookie(pair), null).statusCode()).isEqualTo(200);
        user.setRoles(List.of(UserRole.USER));
        users.saveAndFlush(user);
        assertThat(request("GET", "/admin/users", cookie(pair), null).statusCode()).isEqualTo(403);
    }

    @Test void deletionRevokesEveryDeviceAndBlocksNewSessions() throws Exception {
        User user = user();
        TokenPair first = sessions.create(user.getId());
        TokenPair second = sessions.create(user.getId());
        deleteUser.execute(user.getId());
        assertThat(users.findByIdAndIsDeletedFalse(user.getId())).isEmpty();
        for (TokenPair pair : List.of(first, second)) {
            assertThat(stored(pair).getRevokedAt()).isNotNull();
            assertThatThrownBy(() -> refresh.execute(pair.getRefreshToken())).isInstanceOf(InvalidRtException.class);
            assertThat(request("GET", "/accounts/me", cookie(pair), null).statusCode()).isEqualTo(401);
        }
        assertThatThrownBy(() -> sessions.create(user.getId())).isInstanceOf(InvalidRtException.class);
    }

    @Test void deletionRemovesMembershipsByUserIdWithoutDeletingOtherMembers() {
        User target = user();
        User other = user();
        var account = accounts.saveAndFlush(new com.jexis.jexis_backend.account.domain.entities.Account(
                other.getEmail(), "acct_" + UUID.randomUUID(), "link", other));
        var targetMember = members.saveAndFlush(new com.jexis.jexis_backend.member.domain.entities.Member(
                account, target, com.jexis.jexis_backend.member.domain.enums.Role.MEMBER));
        var otherMember = members.saveAndFlush(new com.jexis.jexis_backend.member.domain.entities.Member(
                account, other, com.jexis.jexis_backend.member.domain.enums.Role.OWNER));
        deleteUser.execute(target.getId());
        assertThat(members.findById(targetMember.getId())).isEmpty();
        assertThat(members.findById(otherMember.getId())).isPresent();
    }

    @Test void deletedFlagAloneBlocksAccessAndRefresh() throws Exception {
        User user = user();
        TokenPair pair = sessions.create(user.getId());
        user.setIsDeleted(true);
        users.saveAndFlush(user);
        assertThatThrownBy(() -> refresh.execute(pair.getRefreshToken())).isInstanceOf(InvalidRtException.class);
        assertThat(request("GET", "/accounts/me", cookie(pair), null).statusCode()).isEqualTo(401);
    }

    @Test void missingMalformedAndUnregisteredRefreshTokensAreUnauthorized() throws Exception {
        assertThat(request("POST", "/auth/refresh", null, null).statusCode()).isEqualTo(401);
        assertThat(request("POST", "/auth/refresh", "refresh_token=broken", null).statusCode()).isEqualTo(401);
        User user = user();
        TokenPair unregistered = jwt.generateTokens(user.getId(), user.getFirstName(), user.getEmail(),
                user.getIsActivated(), user.getRoles(), UUID.randomUUID());
        assertThatThrownBy(() -> refresh.execute(unregistered.getRefreshToken())).isInstanceOf(InvalidRtException.class);
        assertThat(request("GET", "/accounts/me", cookie(unregistered), null).statusCode()).isEqualTo(401);
    }

    @Test void legacySignedTokensAreRejected() throws Exception {
        User user = user();
        String legacy = io.jsonwebtoken.Jwts.builder().claim("id", user.getId().toString())
                .setExpiration(Date.from(Instant.now().plusSeconds(600)))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        "test-refresh-secret-at-least-32-characters-long".getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();
        assertThat(request("POST", "/auth/refresh", "refresh_token=" + legacy, null).statusCode()).isEqualTo(401);
    }

    @Test void accessAndRefreshTokensCannotBeInterchangedEvenWithIdenticalSigningKeys() {
        JwtUtil sameKeys = new JwtUtil("a-long-test-secret-of-at-least-32-bytes", "a-long-test-secret-of-at-least-32-bytes");
        TokenPair tokens = sameKeys.generateTokens(UUID.randomUUID(), "Test", "test@example.com", true,
                List.of(UserRole.USER), UUID.randomUUID());
        assertThat(sameKeys.validateAccessToken(tokens.getRefreshToken())).isFalse();
        assertThat(sameKeys.validateRefreshToken(tokens.getAccessToken())).isFalse();
    }

    @Test void expiredStoredSessionCannotRefreshOrAuthenticate() throws Exception {
        TokenPair pair = sessions.create(user().getId());
        AuthSession session = stored(pair);
        session.setExpiresAt(Instant.now().minusSeconds(1));
        repository.saveAndFlush(session);
        assertThatThrownBy(() -> refresh.execute(pair.getRefreshToken())).isInstanceOf(InvalidRtException.class);
        assertThat(request("GET", "/accounts/me", cookie(pair), null).statusCode()).isEqualTo(401);
    }

    @Test void expiredAccessCookieDoesNotPreventValidRefresh() throws Exception {
        User user = user();
        TokenPair pair = sessions.create(user.getId());
        String expiredAccess = io.jsonwebtoken.Jwts.builder().claim("id", user.getId().toString())
                .claim("sid", stored(pair).getId().toString()).claim("token_use", "access")
                .setExpiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        "test-access-secret-at-least-32-characters-long".getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();
        assertThat(request("POST", "/auth/refresh", "access_token=" + expiredAccess
                + "; refresh_token=" + pair.getRefreshToken(), null).statusCode()).isEqualTo(200);
    }

    @Test void signupIssuesARegisteredSession() throws Exception {
        String unique = Long.toString(System.nanoTime());
        String body = "{\"firstName\":\"New\",\"lastName\":\"User\",\"email\":\"" + unique
                + "@example.com\",\"phoneNumber\":\"" + unique + "\",\"password\":\"password-123\"}";
        var response = request("POST", "/auth/signup", null, body);
        assertThat(response.statusCode()).isEqualTo(200);
        String refreshCookie = response.headers().allValues("Set-Cookie").stream()
                .filter(value -> value.startsWith("refresh_token=")).findFirst().orElseThrow().split(";", 2)[0];
        assertThat(request("POST", "/auth/refresh", refreshCookie, null).statusCode()).isEqualTo(200);
    }

    @Test void loginIssuesRegisteredSessionAndRejectsDeletedUsers() throws Exception {
        User user = user();
        user.setPassword(new Argon2PasswordEncoder(16, 32, 1, 60000, 10).encode("password-123"));
        users.saveAndFlush(user);
        String body = "{\"email\":\"" + user.getEmail() + "\",\"password\":\"password-123\"}";
        var response = request("POST", "/auth/login", null, body);
        assertThat(response.statusCode()).isEqualTo(200);
        String refreshCookie = response.headers().allValues("Set-Cookie").stream()
                .filter(value -> value.startsWith("refresh_token=")).findFirst().orElseThrow().split(";", 2)[0];
        assertThat(request("POST", "/auth/refresh", refreshCookie, null).statusCode()).isEqualTo(200);
        deleteUser.execute(user.getId());
        assertThat(request("POST", "/auth/login", null, body).statusCode()).isEqualTo(400);
    }
}
