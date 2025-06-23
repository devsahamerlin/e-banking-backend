package com.merlin.digitalbanking.ebankingbackend.dto;

import com.merlin.digitalbanking.ebankingbackend.enums.AccountStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SavingBankAccountDTO (
        String id,
        BigDecimal balance,
        LocalDateTime createdAt,
        AccountStatus status,
        CustomerDTO customerDTO,
        BigDecimal interestRate,
        String type) implements BankAccountDTO{}
