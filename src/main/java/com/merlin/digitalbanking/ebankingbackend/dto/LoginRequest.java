package com.merlin.digitalbanking.ebankingbackend.dto;

public record LoginRequest(
        String username,
        String password
) {}
