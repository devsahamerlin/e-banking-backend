package com.merlin.digitalbanking.ebankingbackend.dto;

public record PerformanceMetricsDTO(
        Double customerSatisfactionScore,
        Double systemUptime,
        Double transactionSuccessRate,
        Integer averageResponseTime,
        Long dailyActiveUsers
) {}
