package com.merlin.digitalbanking.ebankingbackend.dto;

import java.math.BigDecimal;

public record AccountTypeStatsDTO(
        String accountType,
        Long count,
        BigDecimal totalBalance,
        Double percentage
) {}