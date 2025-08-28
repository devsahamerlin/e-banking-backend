package com.merlin.digitalbanking.ebankingbackend.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.merlin.digitalbanking.ebankingbackend.dto.*;
import com.merlin.digitalbanking.ebankingbackend.entities.AccountOperation;
import com.merlin.digitalbanking.ebankingbackend.entities.BankAccount;
import com.merlin.digitalbanking.ebankingbackend.entities.CurrentAccount;
import com.merlin.digitalbanking.ebankingbackend.enums.AccountStatus;
import com.merlin.digitalbanking.ebankingbackend.enums.OperationType;
import com.merlin.digitalbanking.ebankingbackend.repositories.AccountOperationRepository;
import com.merlin.digitalbanking.ebankingbackend.repositories.BankAccountRepository;
import com.merlin.digitalbanking.ebankingbackend.repositories.CustomerRepository;
import com.merlin.digitalbanking.ebankingbackend.repositories.UserRepository;
import com.merlin.digitalbanking.ebankingbackend.services.DashboardService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final CustomerRepository customerRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountOperationRepository operationRepository;
    private final UserRepository userRepository;

    @Override
    //@Cacheable(value = "dashboard-overview", key = "'overview'")
    public DashboardOverviewDTO getOverview() {
        log.info("Génération vue d'ensemble dashboard - analyse complète des données");

        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);

        Long totalCustomers = customerRepository.count();
        Long totalAccounts = bankAccountRepository.count();
        Long totalUsers = userRepository.count();

        // Calcul sécurisé du solde total
        BigDecimal totalBalance = bankAccountRepository.findAll()
                .stream()
                .map(BankAccount::getBalance)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Agrégation des opérations récentes
        List<AccountOperation> recentOperations = operationRepository.findAll()
                .stream()
                .filter(op -> op.getOperationDate().isAfter(lastMonth))
                .toList();

        BigDecimal totalDeposits = recentOperations.stream()
                .filter(op -> op.getType() == OperationType.CREDIT)
                .map(AccountOperation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithdrawals = recentOperations.stream()
                .filter(op -> op.getType() == OperationType.DEBIT)
                .map(AccountOperation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Double averageBalance = totalAccounts > 0 ?
                totalBalance.divide(BigDecimal.valueOf(totalAccounts), 2, RoundingMode.HALF_UP).doubleValue() : 0.0;

        // Comptage des comptes par statut
        long createdAccounts = bankAccountRepository.findAll().stream()
                .filter(acc -> acc.getStatus() == AccountStatus.CREATED)
                .count();

        long suspendedAccounts = bankAccountRepository.findAll().stream()
                .filter(acc -> acc.getStatus() == AccountStatus.SUSPENDED)
                .count();

        long activeAccounts = bankAccountRepository.findAll().stream()
                .filter(acc -> acc.getStatus() == AccountStatus.ACTIVATED)
                .count();

        long closedAccounts = bankAccountRepository.findAll().stream()
                .filter(acc -> acc.getStatus() == AccountStatus.CLOSED)
                .count();

        long blockedAccounts = bankAccountRepository.findAll().stream()
                .filter(acc -> acc.getStatus() == AccountStatus.BLOCKED)
                .count();

        return new DashboardOverviewDTO(
                totalCustomers,
                totalAccounts,
                totalUsers,
                totalBalance,
                totalDeposits,
                totalWithdrawals,
                (long) recentOperations.size(),
                averageBalance,
                (int) activeAccounts,
                (int) suspendedAccounts,
                (int) createdAccounts,
                (int) closedAccounts,
                (int) blockedAccounts
        );
    }

    @Override
    @Cacheable(value = "dashboard-monthly", key = "#months")
    public List<MonthlyStatsDTO> getMonthlyStats(int months) {
        log.info("Analyse statistiques mensuelles - {} derniers mois", months);

        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        Map<String, MonthlyStatsDTO> monthlyData = new LinkedHashMap<>();

        // Initialiser les mois avec des valeurs par défaut
        for (int i = months - 1; i >= 0; i--) {
            LocalDateTime month = LocalDateTime.now().minusMonths(i);
            String monthKey = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            monthlyData.put(monthKey, new MonthlyStatsDTO(
                    monthKey, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO, 0L, BigDecimal.ZERO
            ));
        }

        // Agrégation des nouveaux clients par mois
        customerRepository.findAll().stream()
                .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(startDate))
                .collect(Collectors.groupingBy(
                        c -> c.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.counting()
                ))
                .forEach((month, count) -> {
                    MonthlyStatsDTO existing = monthlyData.get(month);
                    if (existing != null) {
                        monthlyData.put(month, new MonthlyStatsDTO(
                                month, count, existing.newAccounts(),
                                existing.totalDeposits(), existing.totalWithdrawals(),
                                existing.transactionCount(), existing.netFlow()
                        ));
                    }
                });

        // Agrégation des opérations par mois
        operationRepository.findAll().stream()
                .filter(op -> op.getOperationDate().isAfter(startDate))
                .collect(Collectors.groupingBy(
                        op -> op.getOperationDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))
                ))
                .forEach((month, operations) -> {
                    BigDecimal deposits = operations.stream()
                            .filter(op -> op.getType() == OperationType.CREDIT)
                            .map(AccountOperation::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal withdrawals = operations.stream()
                            .filter(op -> op.getType() == OperationType.DEBIT)
                            .map(AccountOperation::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    MonthlyStatsDTO existing = monthlyData.get(month);
                    if (existing != null) {
                        monthlyData.put(month, new MonthlyStatsDTO(
                                month, existing.newCustomers(), existing.newAccounts(),
                                deposits, withdrawals, (long) operations.size(),
                                deposits.subtract(withdrawals)
                        ));
                    }
                });

        return new ArrayList<>(monthlyData.values());
    }

    @Override
    @Cacheable(value = "account-types", key = "'accounttypes'")
    public List<AccountTypeStatsDTO> getAccountTypeDistribution() {
        log.info("Analyse distribution types de comptes");

        List<BankAccount> allAccounts = bankAccountRepository.findAll();
        long totalAccounts = allAccounts.size();

        Map<String, List<BankAccount>> accountsByType = allAccounts.stream()
                .collect(Collectors.groupingBy(acc ->
                        acc instanceof CurrentAccount ? "CourantAccount" : "SavingAccount"
                ));

        return accountsByType.entrySet().stream()
                .map(entry -> {
                    String type = entry.getKey();
                    List<BankAccount> accounts = entry.getValue();
                    long count = accounts.size();
                    BigDecimal totalBalance = accounts.stream()
                            .map(BankAccount::getBalance)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    double percentage = totalAccounts > 0 ? (count * 100.0) / totalAccounts : 0.0;

                    return new AccountTypeStatsDTO(type, count, totalBalance, percentage);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "top-customers", key = "#limit")
    public List<TopCustomersDTO> getTopCustomers(int limit) {
        log.info("Analyse top {} clients", limit);

        return customerRepository.findAll().stream()
                .map(customer -> {
                    List<BankAccount> accounts = bankAccountRepository.findByCustomer(customer);
                    long accountsCount = accounts.size();

                    BigDecimal totalBalance = accounts.stream()
                            .map(BankAccount::getBalance)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    long transactionsCount = accounts.stream()
                            .mapToLong(acc -> operationRepository.findByBankAccountId(acc.getId()).size())
                            .sum();

                    return new TopCustomersDTO(
                            customer.getId(),
                            customer.getName(),
                            customer.getEmail(),
                            accountsCount,
                            totalBalance,
                            transactionsCount
                    );
                })
                .sorted((a, b) -> b.totalBalance().compareTo(a.totalBalance()))
                .limit(limit)
                .collect(Collectors.toList());
    }

   @Override
    @Cacheable(value = "alerts", key = "'alerts'")
    public List<AlertDTO> getSystemAlerts() {
        log.info("Génération alertes système");

        List<AlertDTO> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        // Alerte comptes avec solde négatif
        long negativeBalanceAccounts = bankAccountRepository.findAll().stream()
                .filter(acc -> acc.getBalance().compareTo(BigDecimal.ZERO) < 0)
                .count();

        if (negativeBalanceAccounts > 0) {
            alerts.add(new AlertDTO(
                    "SOLDE_NEGATIF",
                    negativeBalanceAccounts + " comptes avec solde négatif détectés",
                    "HIGH",
                    now.format(formatter), // Conversion explicite
                    "Contacter les clients concernés"
            ));
        }

        // Alerte utilisateurs inactifs
        long inactiveUsers = userRepository.findAll().stream()
                .filter(user -> user.getLastLogin() == null ||
                        user.getLastLogin().isBefore(now.minusDays(30)))
                .count();

        if (inactiveUsers > 0) {
            alerts.add(new AlertDTO(
                    "UTILISATEURS_INACTIFS",
                    inactiveUsers + " utilisateurs inactifs depuis 30+ jours",
                    "MEDIUM",
                    now.format(formatter), // Conversion explicite
                    "Révision des accès utilisateurs"
            ));
        }

        // Alerte transactions suspectes (montants élevés)
        BigDecimal suspiciousThreshold = new BigDecimal("10000");
        long suspiciousTransactions = operationRepository.findAll().stream()
                .filter(op -> op.getOperationDate().isAfter(now.minusDays(1)))
                .filter(op -> op.getAmount().compareTo(suspiciousThreshold) > 0)
                .count();

        if (suspiciousTransactions > 0) {
            alerts.add(new AlertDTO(
                    "TRANSACTIONS_SUSPECTES",
                    suspiciousTransactions + " transactions de montant élevé détectées (supérieur à " + suspiciousThreshold + ")",
                    "HIGH",
                    now.format(formatter), // Conversion explicite
                    "Vérification manuelle requise"
            ));
        }

        return alerts;
    }

    @Override
    @Cacheable(value = "risk-analysis", key = "'risk'")
    public RiskAnalysisDTO getRiskAnalysis() {
        log.info("Analyse des risques système");

        List<BankAccount> allAccounts = bankAccountRepository.findAll();
        List<String> riskFactors = new ArrayList<>();

        // Analyse des comptes à découvert
        long overdraftAccounts = allAccounts.stream()
                .filter(acc -> acc.getBalance().compareTo(BigDecimal.ZERO) < 0)
                .count();

        if (overdraftAccounts > 0) {
            riskFactors.add("Comptes à découvert détectés");
        }

        // Concentration des risques
        BigDecimal totalBalance = allAccounts.stream()
                .map(BankAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long highValueAccounts = allAccounts.stream()
                .filter(acc -> acc.getBalance().compareTo(new BigDecimal("50000")) > 0)
                .count();

        if (highValueAccounts > allAccounts.size() * 0.1) {
            riskFactors.add("Concentration élevée de capitaux");
        }

        String riskLevel = riskFactors.isEmpty() ? "FAIBLE" :
                riskFactors.size() <= 2 ? "MOYEN" : "ÉLEVÉ";

        return new RiskAnalysisDTO(
                riskLevel,
                (long) allAccounts.size(),
                totalBalance,
                riskFactors
        );
    }

    @Override
    @Cacheable(value = "performance", key = "'performance'")
    public PerformanceMetricsDTO getPerformanceMetrics() {
        log.info("Calcul métriques performance");

        // Simulation des métriques (à adapter selon vos besoins)
        return new PerformanceMetricsDTO(
                95.5, // Score satisfaction client
                99.8, // Temps de fonctionnement système
                99.2, // Taux de succès transactions
                250,  // Temps de réponse moyen (ms)
                (long) userRepository.findAll().size() // Utilisateurs actifs quotidiens
        );
    }

    @Override
    @Cacheable(value = "complete-dashboard", key = "'dashboard'")
    public Map<String, Object> getComprehensiveDashboard() {
        log.info("Génération dashboard complet - toutes métriques");

        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("overview", getOverview());
        dashboard.put("monthlyStats", getMonthlyStats(12));
        dashboard.put("accountTypeDistribution", getAccountTypeDistribution());
        dashboard.put("topCustomers", getTopCustomers(10));
        dashboard.put("systemAlerts", getSystemAlerts());
        dashboard.put("riskAnalysis", getRiskAnalysis());
        dashboard.put("performanceMetrics", getPerformanceMetrics());

        return dashboard;
    }
}
