package com.merlin.digitalbanking.ebankingbackend.dto;

public record AlertDTO(
        String type,
        String message,
        String severity,
        String timestamp,
        String actionRequired
) {}