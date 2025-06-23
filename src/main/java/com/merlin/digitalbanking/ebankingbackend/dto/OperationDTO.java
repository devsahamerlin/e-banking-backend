package com.merlin.digitalbanking.ebankingbackend.dto;

import java.math.BigDecimal;

public record OperationDTO(String accountId, BigDecimal amount, String description) {
}
