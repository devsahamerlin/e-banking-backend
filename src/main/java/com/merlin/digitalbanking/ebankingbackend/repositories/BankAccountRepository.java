package com.merlin.digitalbanking.ebankingbackend.repositories;

import com.merlin.digitalbanking.ebankingbackend.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import com.merlin.digitalbanking.ebankingbackend.entities.User;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {

    List<BankAccount> findByCustomerId(Long customerId);
    List<BankAccount> findByCreatedBy(User user);

    @Query("SELECT ba FROM BankAccount ba WHERE ba.id LIKE %:keyword% OR ba.customer.name LIKE %:keyword%")
    List<BankAccount> searchAccounts(@Param("keyword") String keyword);
}
