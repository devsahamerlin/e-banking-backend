package com.merlin.digitalbanking.ebankingbackend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserActivityDTO(
        Long userId,
        String username,
        String fullName,
        Long operationsCount,
        BigDecimal totalAmount,
        LocalDateTime lastActivity
) {}
