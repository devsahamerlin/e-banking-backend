package com.merlin.digitalbanking.ebankingbackend.dto;

import java.math.BigDecimal;
import java.util.List;

public record AccountHistoryDTO(
        String accountId,
        BigDecimal balance,
        List<AccountOperationDTO> operations,
        int currentPage,
        int totalPage,
        int pageSize
        ) { }
