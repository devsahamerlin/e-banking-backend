package com.merlin.digitalbanking.ebankingbackend.dto;

import java.math.BigDecimal;

public record MonthlyStatsDTO(
        String month,
        Long newCustomers,
        Long newAccounts,
        BigDecimal totalDeposits,
        BigDecimal totalWithdrawals,
        Long transactionCount,
        BigDecimal netFlow
) {}