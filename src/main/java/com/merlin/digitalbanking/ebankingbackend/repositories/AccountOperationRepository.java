package com.merlin.digitalbanking.ebankingbackend.repositories;

import com.merlin.digitalbanking.ebankingbackend.entities.AccountOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.merlin.digitalbanking.ebankingbackend.entities.User;
import java.util.List;

@Repository
public interface AccountOperationRepository extends JpaRepository<AccountOperation, Long> {
    List<AccountOperation> findByBankAccountId(String accountId);
    Page<AccountOperation> findByBankAccountId(String accountId, Pageable pageable);

    List<AccountOperation> findByPerformedBy(User user);

    @Query("SELECT ao FROM AccountOperation ao WHERE ao.bankAccount.id = :accountId AND ao.performedBy = :user")
    List<AccountOperation> findByAccountIdAndPerformedBy(@Param("accountId") String accountId, @Param("user") User user);
}
