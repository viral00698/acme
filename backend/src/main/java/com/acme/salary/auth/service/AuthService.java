package com.acme.salary.auth.service;

import com.acme.salary.auth.dto.*;
import com.acme.salary.auth.entity.Role;
import com.acme.salary.auth.entity.UserAccount;
import com.acme.salary.auth.repository.UserAccountRepository;
import com.acme.salary.auth.security.JwtTokenProvider;
import com.acme.salary.auth.security.LoginRateLimitService;
import com.acme.salary.auth.security.UserPrincipal;
import com.acme.salary.common.exception.ResourceNotFoundException;
import com.acme.salary.common.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserAccountRepository userAccountRepository;
    private final LoginRateLimitService rateLimitService;
    private final long jwtExpirationMs;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider,
            RefreshTokenService refreshTokenService,
            UserAccountRepository userAccountRepository,
            LoginRateLimitService rateLimitService,
            @Value("${app.jwt.expiration-ms:900000}") long jwtExpirationMs) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.userAccountRepository = userAccountRepository;
        this.rateLimitService = rateLimitService;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String clientIp) {
        String rateLimitKey = clientIp + ":" + request.username();
        rateLimitService.checkRateLimit(rateLimitKey);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (AuthenticationException ex) {
            rateLimitService.recordFailedAttempt(rateLimitKey);
            throw new BadCredentialsException("Invalid username or password");
        }

        rateLimitService.recordSuccessfulAttempt(rateLimitKey);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = tokenProvider.generateToken(principal);
        String refreshToken = refreshTokenService.createRefreshToken(principal.getId());

        List<String> roles = principal.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList());

        UserProfileDto userProfile = new UserProfileDto(
                principal.getId(),
                principal.getUsername(),
                principal.getEmail(),
                roles
        );

        return AuthResponse.of(accessToken, refreshToken, jwtExpirationMs / 1000, userProfile);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshTokenService.RefreshTokenResult result = refreshTokenService.verifyAndRotate(request.refreshToken());
        UserAccount user = result.user();

        UserPrincipal principal = UserPrincipal.create(user);
        String newAccessToken = tokenProvider.generateToken(principal);

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        UserProfileDto userProfile = new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        );

        return AuthResponse.of(newAccessToken, result.newRefreshToken(), jwtExpirationMs / 1000, userProfile);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        if (request != null && request.refreshToken() != null) {
            refreshTokenService.revokeToken(request.refreshToken());
        }
        SecurityContextHolder.clearContext();
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUser(String username) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return new UserProfileDto(user.getId(), user.getUsername(), user.getEmail(), roles);
    }
}
