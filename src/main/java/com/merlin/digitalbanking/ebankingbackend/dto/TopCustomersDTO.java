package com.merlin.digitalbanking.ebankingbackend.dto;

import java.math.BigDecimal;

public record TopCustomersDTO(
        Long customerId,
        String customerName,
        String email,
        Long accountsCount,
        BigDecimal totalBalance,
        Long transactionsCount
) {}
