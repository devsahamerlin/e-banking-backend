package com.merlin.digitalbanking.ebankingbackend.dto;

import java.math.BigDecimal;

public record DashboardOverviewDTO(
        Long totalCustomers,
        Long totalAccounts,
        Long totalUsers,
        BigDecimal totalBalance,
        BigDecimal totalDeposits,
        BigDecimal totalWithdrawals,
        Long totalTransactions,
        Double averageAccountBalance,
        Integer activeAccountsCount,
        Integer suspendedAccountsCount,
        Integer createdAccounts,
        Integer closedAccounts,
        Integer blockedAccounts
) {}
