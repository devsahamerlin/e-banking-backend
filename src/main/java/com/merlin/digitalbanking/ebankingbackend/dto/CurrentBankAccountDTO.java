package com.merlin.digitalbanking.ebankingbackend.dto;

import com.merlin.digitalbanking.ebankingbackend.enums.AccountStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CurrentBankAccountDTO(
        String id,
        BigDecimal balance,
        LocalDateTime createdAt,
        AccountStatus status,
        CustomerDTO customerDTO,
        BigDecimal overdraft,
        String type) implements BankAccountDTO{}
