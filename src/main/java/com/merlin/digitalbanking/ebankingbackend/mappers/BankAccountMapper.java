package com.merlin.digitalbanking.ebankingbackend.mappers;

import com.merlin.digitalbanking.ebankingbackend.dto.*;
import com.merlin.digitalbanking.ebankingbackend.entities.*;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BankAccountMapper {
    CustomerDTO fromCustomer(Customer customer);
    Customer fromCustomerDTO(CustomerDTO customerDTO);
    SavingBankAccountDTO fromSavingBankAccount(SavingAccount savingAccount);
    SavingAccount fromSavingBankAccountDTO(SavingBankAccountDTO savingBankAccount);
    CurrentBankAccountDTO fromCurrentBankAccount(CurrentAccount currentAccount);
    CurrentAccount fromCurrentBankAccountDTO(CurrentBankAccountDTO currentBankAccount);
    AccountOperationDTO fromAccountOperation(AccountOperation accountOperation);

    default AccountHistoryDTO toAccountHistoryDTO(String accountId,
                                                  BigDecimal balance,
                                                  List<AccountOperationDTO> operations,
                                                  int totalPage,
                                                  int currentPage,
                                                  int pageSize) {
        return new AccountHistoryDTO(
                accountId,
                balance,
                operations,
                currentPage,
                totalPage,
                pageSize
        );
    }

    default BankAccountDTO bankAccountToBankAccountDTO(BankAccount bankAccount) {
        if (bankAccount instanceof CurrentAccount current) {
            return new CurrentBankAccountDTO(
                    current.getId(),
                    current.getBalance(),
                    current.getCreatedAt(),
                    current.getStatus(),
                    fromCustomer(current.getCustomer()),
                    current.getOverDraft(),
                    current.getClass().getSimpleName()
            );
        } else if (bankAccount instanceof SavingAccount saving) {
            return new SavingBankAccountDTO(
                    saving.getId(),
                    saving.getBalance(),
                    saving.getCreatedAt(),
                    saving.getStatus(),
                    fromCustomer(saving.getCustomer()),
                    saving.getInterestRate(),
                    saving.getClass().getSimpleName()
            );
        } else {
            throw new IllegalStateException("Unknown bank account type: " + bankAccount.getClass().getSimpleName());
        }
    }

    default BigDecimal roundAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
