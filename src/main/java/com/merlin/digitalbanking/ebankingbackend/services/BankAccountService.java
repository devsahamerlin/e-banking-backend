package com.merlin.digitalbanking.ebankingbackend.services;

import com.merlin.digitalbanking.ebankingbackend.dto.*;
import com.merlin.digitalbanking.ebankingbackend.enums.AccountStatus;
import com.merlin.digitalbanking.ebankingbackend.exceptions.AccountStatusException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.CustomerNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface BankAccountService {
    CustomerDTO saveCustomer(CustomerDTO customerDTO);
    CustomerDTO updateCustomer(CustomerDTO customerDTO);
    void deleteCustomer(Long customerId);

    List<CustomerDTO> listCustomers();
    List<BankAccountDTO> bankAccountList();
    BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException;
    void debit(String accountId, BigDecimal amount, String description, LocalDateTime dateTime) throws BankAccountNotFoundException, BalanceNotSufficientException, AccountStatusException;
    void credit(String accountId, BigDecimal amount, String description, LocalDateTime dateTime) throws BankAccountNotFoundException, BalanceNotSufficientException, AccountStatusException;
    void transfer(String fromAccountId, String toAccountId, BigDecimal amount, String description, LocalDateTime dateTime) throws BalanceNotSufficientException, BankAccountNotFoundException, AccountStatusException;
    List<BankAccountDTO> listCustomerBankAccounts(Long customerId) throws CustomerNotFoundException;
    CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException;
    List<AccountOperationDTO> accountHistory(String accoundId);
    AccountHistoryDTO getPageableAccountHistory(String accountNumber, int page, int size) throws BankAccountNotFoundException;
    List<CustomerDTO> searchCustomers(String keyword);
    BankAccountDTO createAccount(CreateAccountDTO createAccountDTO) throws CustomerNotFoundException;
    List<BankAccountDTO> searchAccounts(String keyword);
    void updateAccountStatus(String accountId, AccountStatus status) throws BankAccountNotFoundException;
    List<AccountOperationDTO> getUserOperations(Long userId);
    List<BankAccountDTO> getAccounts();
}
