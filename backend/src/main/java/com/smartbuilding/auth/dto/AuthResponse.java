package com.smartbuilding.auth.dto;

import com.smartbuilding.user.dto.UserResponse;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
    public static AuthResponse of(String accessToken, long expiresIn, UserResponse user) {
        return new AuthResponse(accessToken, "Bearer", expiresIn, user);
    }
}

