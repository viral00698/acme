package com.acme.salary.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserProfileDto user
) {
    public static AuthResponse of(String accessToken, String refreshToken, long expiresIn, UserProfileDto user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
