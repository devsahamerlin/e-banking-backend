package com.merlin.digitalbanking.ebankingbackend.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserInfo user
) {}
