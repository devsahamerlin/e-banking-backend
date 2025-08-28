package com.merlin.digitalbanking.ebankingbackend.repositories;

import com.merlin.digitalbanking.ebankingbackend.dto.AccountTypeStatsDTO;
import com.merlin.digitalbanking.ebankingbackend.dto.MonthlyStatsDTO;
import com.merlin.digitalbanking.ebankingbackend.dto.TopCustomersDTO;
import com.merlin.digitalbanking.ebankingbackend.dto.UserActivityDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DashboardRepository {

    @Query("SELECT COUNT(c) FROM Customer c")
    Long getTotalCustomers();

    @Query("SELECT COUNT(ba) FROM BankAccount ba")
    Long getTotalAccounts();

    @Query("SELECT COUNT(u) FROM User u")
    Long getTotalUsers();

    @Query("SELECT SUM(ba.balance) FROM BankAccount ba")
    BigDecimal getTotalBalance();

    @Query("SELECT SUM(ao.amount) FROM AccountOperation ao WHERE ao.type = 'CREDIT' AND ao.operationDate >= :startDate")
    BigDecimal getTotalDeposits(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(ao.amount) FROM AccountOperation ao WHERE ao.type = 'DEBIT' AND ao.operationDate >= :startDate")
    BigDecimal getTotalWithdrawals(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(ao) FROM AccountOperation ao WHERE ao.operationDate >= :startDate")
    Long getTotalTransactions(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT AVG(ba.balance) FROM BankAccount ba")
    Double getAverageAccountBalance();

    @Query("SELECT COUNT(ba) FROM BankAccount ba WHERE ba.status = 'CREATED'")
    Integer getActiveAccountsCount();

    @Query("SELECT COUNT(ba) FROM BankAccount ba WHERE ba.status = 'SUSPENDED'")
    Integer getSuspendedAccountsCount();

    @Query("SELECT new com.example.dto.MonthlyStatsDTO(" +
            "FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m'), " +
            "COUNT(c), 0L, 0, 0, 0L, 0) " +
            "FROM Customer c " +
            "WHERE c.createdAt >= :startDate " +
            "GROUP BY FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m')")
    List<MonthlyStatsDTO> getMonthlyCustomerStats(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT new com.example.dto.AccountTypeStatsDTO(" +
            "TYPE(ba), " +
            "COUNT(ba), " +
            "SUM(ba.balance), " +
            "0.0) " +
            "FROM BankAccount ba " +
            "GROUP BY TYPE(ba)")
    List<AccountTypeStatsDTO> getAccountTypeStats();

    @Query("SELECT new com.example.dto.UserActivityDTO(" +
            "u.id, u.username, CONCAT(u.firstName, ' ', u.lastName), " +
            "COUNT(ao), SUM(ao.amount), MAX(ao.operationDate)) " +
            "FROM User u " +
            "LEFT JOIN AccountOperation ao ON ao.performedBy = u " +
            "WHERE ao.operationDate >= :startDate " +
            "GROUP BY u.id, u.username, u.firstName, u.lastName " +
            "ORDER BY COUNT(ao) DESC")
    List<UserActivityDTO> getUserActivityStats(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT new com.example.dto.TopCustomersDTO(" +
            "c.id, c.name, c.email, " +
            "COUNT(ba), SUM(ba.balance), " +
            "SIZE(ba.accountOperations)) " +
            "FROM Customer c " +
            "LEFT JOIN c.bankAccounts ba " +
            "GROUP BY c.id, c.name, c.email " +
            "ORDER BY SUM(ba.balance) DESC")
    List<TopCustomersDTO> getTopCustomers(Pageable pageable);
}