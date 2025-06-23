package com.merlin.digitalbanking.ebankingbackend.services;

import com.merlin.digitalbanking.ebankingbackend.dto.*;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.CustomerNotFoundException;
import java.math.BigDecimal;
import java.util.List;

public interface BankAccountService {
    CustomerDTO saveCustomer(CustomerDTO customerDTO);
    CustomerDTO updateCustomer(CustomerDTO customerDTO);
    void deleteCustomer(Long customerId);

    CurrentBankAccountDTO saveCurrentBankAccount(BigDecimal initialBalance, BigDecimal overDraft, Long customerId) throws CustomerNotFoundException;
    SavingBankAccountDTO saveSavingBankAccount(BigDecimal initialBalance, BigDecimal interestRate, Long customerId) throws CustomerNotFoundException;
    List<CustomerDTO> listCustomers();
    BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException;
    void debit(String accountId, BigDecimal amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException;
    void credit(String accountId, BigDecimal amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException;
    void transfer(String fromAccountId, String toAccountId, BigDecimal amount, String description) throws BalanceNotSufficientException, BankAccountNotFoundException;
    List<BankAccountDTO> listCustomerBankAccounts(Long customerId) throws CustomerNotFoundException;
    CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException;
    List<AccountOperationDTO> accountHistory(String accoundId);
    AccountHistoryDTO getPageableAccountHistory(String accountNumber, int page, int size) throws BankAccountNotFoundException;
}
