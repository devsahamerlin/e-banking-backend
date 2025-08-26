package com.merlin.digitalbanking.ebankingbackend.dto;

import java.math.BigDecimal;

public record CreateAccountDTO(
        BigDecimal initialBalance,
        Long customerId,
        String accountType,
        BigDecimal overDraft,
        BigDecimal interestRate
) {}
