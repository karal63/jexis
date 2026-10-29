package com.jexis.jexis_backend.common.ratelimit;

import com.jexis.jexis_backend.auth.infrastructure.security.JwtUtil;
import com.jexis.jexis_backend.auth.application.useCases.RefreshTokensUseCase;
import com.jexis.jexis_backend.auth.domain.exception.InvalidRtException;
import com.jexis.jexis_backend.auth.application.security.AuthSessionService;
import java.util.UUID;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshRateLimitTest {
    private final JwtUtil jwt = new JwtUtil("test-access-secret-at-least-32-characters-long",
            "test-refresh-secret-at-least-32-characters-long");
    private final RateLimitService limits = mock(RateLimitService.class);
    private final AuthSessionService sessions = mock(AuthSessionService.class);
    private final RefreshTokensUseCase refresh = new RefreshTokensUseCase(jwt, sessions, limits);

    @Test void verifiedRefreshIdentityIsLimitedBeforeDatabaseWork() {
        var id = UUID.randomUUID();
        var token = jwt.generateTokens(id, "Test", "test@example.com", true, List.of(), UUID.randomUUID()).getRefreshToken();
        doThrow(new RateLimitException(2)).when(limits).check("refresh-user", id.toString());
        assertThatThrownBy(() -> refresh.execute(token)).isInstanceOf(RateLimitException.class);
        verifyNoInteractions(sessions);
    }

    @Test void invalidTokensCannotSelectAUserBucket() {
        assertThatThrownBy(() -> refresh.execute("invalid-token")).isInstanceOf(InvalidRtException.class);
        verifyNoInteractions(limits, sessions);
    }
}
