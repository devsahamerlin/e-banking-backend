package com.merlin.digitalbanking.ebankingbackend.dto;

import java.math.BigDecimal;
import java.util.List;

public record RiskAnalysisDTO(
        String riskLevel,
        Long accountsCount,
        BigDecimal exposureAmount,
        List<String> riskFactors
) {}