package com.merlin.digitalbanking.ebankingbackend.dto;

public sealed interface BankAccountDTO
        permits CurrentBankAccountDTO, SavingBankAccountDTO {
    String id();
}
