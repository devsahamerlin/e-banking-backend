package com.merlin.digitalbanking.ebankingbackend.web;

import com.merlin.digitalbanking.ebankingbackend.dto.*;
import com.merlin.digitalbanking.ebankingbackend.services.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardRestController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_MANAGER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<DashboardOverviewDTO> getOverview() {
        log.info("Récupération vue d'ensemble dashboard");
        DashboardOverviewDTO overview = dashboardService.getOverview();
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/monthly-stats/{months}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_MANAGER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<List<MonthlyStatsDTO>> getMonthlyStats(
            @PathVariable int months) {
        log.info("Récupération statistiques mensuelles - {} mois", months);
        List<MonthlyStatsDTO> stats = dashboardService.getMonthlyStats(months);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/account-types")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_MANAGER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<List<AccountTypeStatsDTO>> getAccountTypeDistribution() {
        log.info("Récupération répartition types de comptes");
        List<AccountTypeStatsDTO> distribution = dashboardService.getAccountTypeDistribution();
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/top-customers/{limit}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_MANAGER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<List<TopCustomersDTO>> getTopCustomers(
            @PathVariable int limit) {
        log.info("Récupération top {} clients", limit);
        List<TopCustomersDTO> topCustomers = dashboardService.getTopCustomers(limit);
        return ResponseEntity.ok(topCustomers);
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<List<AlertDTO>> getSystemAlerts() {
        log.info("Récupération alertes système");
        List<AlertDTO> alerts = dashboardService.getSystemAlerts();
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/risk-analysis")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<RiskAnalysisDTO> getRiskAnalysis() {
        log.info("Récupération analyse des risques");
        RiskAnalysisDTO riskAnalysis = dashboardService.getRiskAnalysis();
        return ResponseEntity.ok(riskAnalysis);
    }

    @GetMapping("/performance")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<PerformanceMetricsDTO> getPerformanceMetrics() {
        log.info("Récupération métriques performance");
        PerformanceMetricsDTO metrics = dashboardService.getPerformanceMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/complete")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getCompleteDashboard() {
        log.info("Récupération dashboard complet");
        Map<String, Object> dashboard = dashboardService.getComprehensiveDashboard();
        return ResponseEntity.ok(dashboard);
    }

    // Endpoints spécialisés pour différents rôles
    @GetMapping("/manager-view")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_MANAGER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getManagerDashboard() {
        log.info("Vue gestionnaire dashboard");
        Map<String, Object> managerDashboard = new HashMap<>();

        managerDashboard.put("overview", dashboardService.getOverview());
        managerDashboard.put("monthlyStats", dashboardService.getMonthlyStats(6));
        managerDashboard.put("topCustomers", dashboardService.getTopCustomers(5));
        managerDashboard.put("accountTypes", dashboardService.getAccountTypeDistribution());

        return ResponseEntity.ok(managerDashboard);
    }

    @GetMapping("/user-view")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_MANAGER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserDashboard() {
        log.info("Vue utilisateur dashboard");
        Map<String, Object> userDashboard = new HashMap<>();

        DashboardOverviewDTO overview = dashboardService.getOverview();

        // Vue simplifiée pour utilisateurs standard
        Map<String, Object> basicOverview = new HashMap<>();
        basicOverview.put("totalCustomers", overview.totalCustomers());
        basicOverview.put("totalAccounts", overview.totalAccounts());
        basicOverview.put("totalBalance", overview.totalBalance());

        userDashboard.put("basicOverview", basicOverview);
        userDashboard.put("accountTypes", dashboardService.getAccountTypeDistribution());

        return ResponseEntity.ok(userDashboard);
    }
}
