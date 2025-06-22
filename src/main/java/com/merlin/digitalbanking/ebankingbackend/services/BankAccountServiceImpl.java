package com.merlin.digitalbanking.ebankingbackend.services;

import com.merlin.digitalbanking.ebankingbackend.entities.*;
import com.merlin.digitalbanking.ebankingbackend.enums.AccountStatus;
import com.merlin.digitalbanking.ebankingbackend.enums.OperationType;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.CustomerNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.repositories.AccountOperationRepository;
import com.merlin.digitalbanking.ebankingbackend.repositories.BankAccountRepository;
import com.merlin.digitalbanking.ebankingbackend.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {

    private final CustomerRepository customerRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountOperationRepository accountOperationRepository;

    @Override
    public Customer saveCustomer(Customer customer) {
        log.info("Save customer : {}", customer.getName());
        return customerRepository.save(customer);
    }

    @Override
    public CurrentAccount saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException {

        Customer customer = findCustomerById(customerId);

        CurrentAccount currentAccount = new CurrentAccount();

        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setBalance(initialBalance);
        currentAccount.setCreatedAt(new Date());
        currentAccount.setCustomer(customer);
        currentAccount.setStatus(AccountStatus.CREATED);
        currentAccount.setOverDraft(overDraft);
        return bankAccountRepository.save(currentAccount);
    }

    @Override
    public SavingAccount saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException {

        Customer customer = findCustomerById(customerId);

        SavingAccount savingAccount = new SavingAccount();

        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setBalance(initialBalance);
        savingAccount.setCreatedAt(new Date());
        savingAccount.setCustomer(customer);
        savingAccount.setStatus(AccountStatus.CREATED);
        savingAccount.setInterestRate(interestRate);
        return bankAccountRepository.save(savingAccount);
    }

    @Override
    public List<Customer> listCustomers() {
        log.info("List all customers");
        return customerRepository.findAll();
    }

    @Override
    public BankAccount getBankAccount(String accountId) throws BankAccountNotFoundException{
        return findBankAccountById(accountId);
    }

    @Override
    public void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        log.info("Debit amount : {}", amount);
        saveOperation(OperationType.DEBIT, accountId, roundAmount(amount), description);
    }

    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException{
        log.info("Credit amount : {}", amount);
        saveOperation(OperationType.CREDIT, accountId, roundAmount(amount), description);
    }

    @Override
    public void transfer(String fromAccountId, String toAccountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        log.info("Transfer amount : {} from {}, to {}", amount, fromAccountId, toAccountId);
        debit(fromAccountId, roundAmount(amount), description);
        credit(toAccountId,  roundAmount(amount), description);
    }

    @Override
    public List<BankAccount> listCustomerBankAccounts(Long customerId) throws CustomerNotFoundException {
        Customer customer = findCustomerById(customerId);
        return bankAccountRepository.findByCustomerId(customer.getId());
    }

    private Customer findCustomerById(Long customerId) throws CustomerNotFoundException {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.error("Customer not found, {}", customerId);
                    return new CustomerNotFoundException("Customer not found");
                });
    }

    private BankAccount findBankAccountById(String bankAccountId) throws BankAccountNotFoundException {
        log.info("Get bank account : {}", bankAccountId);
        return bankAccountRepository.findById(bankAccountId).orElseThrow(() -> {
            log.error("Bank Account not found, {}", bankAccountId);
            return new BankAccountNotFoundException("Bank Account not found");
        });
    }

    private void saveOperation(OperationType operationType, String accountId, double amount, String description) throws BalanceNotSufficientException, BankAccountNotFoundException {
        BankAccount bankAccount = findBankAccountById(accountId);

        if (operationType.equals(OperationType.DEBIT)) {
            if (bankAccount.getBalance() < amount) {
                throw new BalanceNotSufficientException("Balance not sufficient");
            }
        }

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(operationType);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);

        accountOperationRepository.save(accountOperation);

        if (operationType.equals(OperationType.DEBIT)) {
            bankAccount.setBalance(bankAccount.getBalance() - amount);
        } else {
            bankAccount.setBalance(bankAccount.getBalance() + amount);
        }

        bankAccountRepository.save(bankAccount);
    }

    private double roundAmount(double amount) {
        return Math.round((amount) * 100.0) / 100.0;
    }
}
