package com.acme.salary.auth.service;

import com.acme.salary.auth.entity.RefreshToken;
import com.acme.salary.auth.entity.Role;
import com.acme.salary.auth.entity.UserAccount;
import com.acme.salary.auth.repository.RefreshTokenRepository;
import com.acme.salary.auth.repository.UserAccountRepository;
import com.acme.salary.common.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    private RefreshTokenService refreshTokenService;
    private UserAccount testUser;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, userAccountRepository, 604800000L);

        testUser = new UserAccount("admin", "admin@acme.corp", "hash", true);
        testUser.setId(1L);
        testUser.setRoles(Set.of(new Role("ROLE_HR_ADMIN", "Admin")));
    }

    @Test
    @DisplayName("Create refresh token persists SHA-256 hash in database")
    void testCreateRefreshToken() {
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        String rawToken = refreshTokenService.createRefreshToken(1L);

        assertNotNull(rawToken);
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken saved = captor.getValue();
        assertEquals(testUser, saved.getUser());
        assertEquals(RefreshTokenService.hashToken(rawToken), saved.getTokenHash());
        assertFalse(saved.isRevoked());
    }

    @Test
    @DisplayName("Rotating refresh token revokes previous token and issues new token")
    void testVerifyAndRotate() {
        String rawToken = "sample-raw-token";
        String tokenHash = RefreshTokenService.hashToken(rawToken);

        RefreshToken existing = new RefreshToken(testUser, tokenHash, OffsetDateTime.now().plusDays(5));
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(existing));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        RefreshTokenService.RefreshTokenResult result = refreshTokenService.verifyAndRotate(rawToken);

        assertNotNull(result);
        assertEquals(testUser, result.user());
        assertNotEquals(rawToken, result.newRefreshToken());
        assertTrue(existing.isRevoked(), "Old token must be marked as revoked");
    }

    @Test
    @DisplayName("Reject revoked refresh token")
    void testRejectRevokedToken() {
        String rawToken = "revoked-token";
        String tokenHash = RefreshTokenService.hashToken(rawToken);

        RefreshToken revoked = new RefreshToken(testUser, tokenHash, OffsetDateTime.now().plusDays(5));
        revoked.setRevoked(true);
        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(revoked));

        assertThrows(UnauthorizedException.class, () -> refreshTokenService.verifyAndRotate(rawToken));
        verify(refreshTokenRepository).delete(revoked);
    }
}
