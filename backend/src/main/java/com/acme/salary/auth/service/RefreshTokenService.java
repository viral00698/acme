package com.acme.salary.auth.service;

import com.acme.salary.auth.entity.RefreshToken;
import com.acme.salary.auth.entity.UserAccount;
import com.acme.salary.auth.repository.RefreshTokenRepository;
import com.acme.salary.auth.repository.UserAccountRepository;
import com.acme.salary.common.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserAccountRepository userAccountRepository;
    private final long refreshExpirationMs;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserAccountRepository userAccountRepository,
            @Value("${app.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userAccountRepository = userAccountRepository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Transactional
    public String createRefreshToken(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        String rawToken = generateSecureRandomToken();
        String tokenHash = hashToken(rawToken);

        OffsetDateTime expiryDate = OffsetDateTime.now().plusSeconds(refreshExpirationMs / 1000);

        RefreshToken refreshToken = new RefreshToken(user, tokenHash, expiryDate);
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public RefreshTokenResult verifyAndRotate(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (token.isRevoked() || token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new UnauthorizedException("Refresh token is expired or revoked. Please log in again.");
        }

        // Revoke the used token (token rotation)
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        // Issue new refresh token
        UserAccount user = token.getUser();
        String newRawToken = generateSecureRandomToken();
        String newTokenHash = hashToken(newRawToken);

        OffsetDateTime newExpiryDate = OffsetDateTime.now().plusSeconds(refreshExpirationMs / 1000);
        RefreshToken newToken = new RefreshToken(user, newTokenHash, newExpiryDate);
        refreshTokenRepository.save(newToken);

        return new RefreshTokenResult(user, newRawToken);
    }

    @Transactional
    public void revokeToken(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private String generateSecureRandomToken() {
        byte[] randomBytes = new byte[48];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }

    public record RefreshTokenResult(UserAccount user, String newRefreshToken) {}
}
