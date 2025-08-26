package com.merlin.digitalbanking.ebankingbackend.dto;

import com.merlin.digitalbanking.ebankingbackend.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountOperationDTO (
        Long id,
        LocalDateTime operationDate,
        BigDecimal amount,
        OperationType type,
        String description,
        String accountId,
        UserDTO performedBy){}
