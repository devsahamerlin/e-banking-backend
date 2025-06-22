package com.merlin.digitalbanking.ebankingbackend.services;

import com.merlin.digitalbanking.ebankingbackend.entities.BankAccount;
import com.merlin.digitalbanking.ebankingbackend.entities.CurrentAccount;
import com.merlin.digitalbanking.ebankingbackend.entities.Customer;
import com.merlin.digitalbanking.ebankingbackend.entities.SavingAccount;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.CustomerNotFoundException;

import java.util.List;

public interface BankAccountService {
    Customer saveCustomer(Customer customer);
    CurrentAccount saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException;
    SavingAccount saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException;
    List<Customer> listCustomers();
    BankAccount getBankAccount(String accountId) throws BankAccountNotFoundException;
    void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException;
    void credit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException;
    void transfer(String fromAccountId, String toAccountId, double amount, String description) throws BalanceNotSufficientException, BankAccountNotFoundException;
    List<BankAccount> listCustomerBankAccounts(Long customerId) throws CustomerNotFoundException;
}
