package com.merlin.digitalbanking.ebankingbackend.dto;

import java.math.BigDecimal;

public record TransferDTO(String fromAccountId, String toAccountId, BigDecimal amount, String description) {
}
