package com.merlin.digitalbanking.ebankingbackend.services;

import com.merlin.digitalbanking.ebankingbackend.dto.*;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    DashboardOverviewDTO getOverview();
    List<MonthlyStatsDTO> getMonthlyStats(int months);
    List<AccountTypeStatsDTO> getAccountTypeDistribution();
    List<TopCustomersDTO> getTopCustomers(int limit);
    List<AlertDTO> getSystemAlerts();
    RiskAnalysisDTO getRiskAnalysis();
    PerformanceMetricsDTO getPerformanceMetrics();
    Map<String, Object> getComprehensiveDashboard();
}
