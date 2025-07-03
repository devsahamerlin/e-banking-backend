package com.merlin.digitalbanking.ebankingbackend.services;

import com.merlin.digitalbanking.ebankingbackend.dto.*;
import com.merlin.digitalbanking.ebankingbackend.entities.*;
import com.merlin.digitalbanking.ebankingbackend.enums.AccountStatus;
import com.merlin.digitalbanking.ebankingbackend.enums.OperationType;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.CustomerNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.mappers.BankAccountMapper;
import com.merlin.digitalbanking.ebankingbackend.repositories.AccountOperationRepository;
import com.merlin.digitalbanking.ebankingbackend.repositories.BankAccountRepository;
import com.merlin.digitalbanking.ebankingbackend.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {
    private static Logger logger = Logger.getLogger(BankAccountServiceImpl.class.getName());
    private final CustomerRepository customerRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountOperationRepository accountOperationRepository;
    private final BankAccountMapper bankAccountMapper;

    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("Save customer : {}", customerDTO.name());
        Customer customer = customerRepository
                .save(bankAccountMapper.fromCustomerDTO(customerDTO));
        return bankAccountMapper.fromCustomer(customer);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        log.info("Update customer : {}", customerDTO.name());
        Customer customer = customerRepository
                .save(bankAccountMapper.fromCustomerDTO(customerDTO));
        return bankAccountMapper.fromCustomer(customer);
    }

    @Override
    public void deleteCustomer(Long customerId) {
        log.info("Delete customer : {}", customerId);
        customerRepository
                .deleteById(customerId);
    }

    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(BigDecimal initialBalance, BigDecimal overDraft, Long customerId) throws CustomerNotFoundException {

        Customer customer = findCustomerById(customerId);

        CurrentAccount currentAccount = new CurrentAccount();

        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setBalance(initialBalance);
        currentAccount.setCreatedAt(LocalDateTime.now());
        currentAccount.setCustomer(customer);
        currentAccount.setStatus(AccountStatus.CREATED);
        currentAccount.setOverDraft(overDraft);
        return bankAccountMapper.fromCurrentBankAccount(bankAccountRepository.save(currentAccount));
    }

    @Override
    public SavingBankAccountDTO saveSavingBankAccount(BigDecimal initialBalance, BigDecimal interestRate, Long customerId) throws CustomerNotFoundException {

        Customer customer = findCustomerById(customerId);

        SavingAccount savingAccount = new SavingAccount();

        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setBalance(initialBalance);
        savingAccount.setCreatedAt(LocalDateTime.now());
        savingAccount.setCustomer(customer);
        savingAccount.setStatus(AccountStatus.CREATED);
        savingAccount.setInterestRate(interestRate);
        return bankAccountMapper.fromSavingBankAccount(bankAccountRepository.save(savingAccount));
    }

    @Override
    public List<CustomerDTO> listCustomers() {
        log.info("List all customers");
        List<Customer> customers = customerRepository.findAll();
        return customers.stream().map(bankAccountMapper::fromCustomer)
                .collect(Collectors.toList());
    }

    @Override
    public List<BankAccountDTO> bankAccountList(){
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        List<BankAccountDTO> bankAccountDTOS = bankAccounts.stream().map(bankAccount -> {
            if (bankAccount instanceof SavingAccount) {
                SavingAccount savingAccount = (SavingAccount) bankAccount;
                return bankAccountMapper.fromSavingBankAccount(savingAccount);
            } else {
                CurrentAccount currentAccount = (CurrentAccount) bankAccount;
                return bankAccountMapper.fromCurrentBankAccount(currentAccount);
            }
        }).collect(Collectors.toList());
        return bankAccountDTOS;
    }

    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException{
        log.info("Getting bank account: {}", accountId);

        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Account not found: " + accountId));

        return bankAccountMapper.bankAccountToBankAccountDTO(bankAccount);
    }

    @Override
    public void debit(String accountId, BigDecimal amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        logger.info("Debit amount : "+amount+", from "+accountId);
        log.info("Debit amount : {}", amount);
        saveOperation(OperationType.DEBIT, accountId, bankAccountMapper.roundAmount(amount), description);
    }

    @Override
    public void credit(String accountId, BigDecimal amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException{
        logger.info("Credit amount : "+amount+", to "+accountId);
        log.info("Credit amount : {}", amount);
        saveOperation(OperationType.CREDIT, accountId, bankAccountMapper.roundAmount(amount), description);
    }

    @Override
    public void transfer(String fromAccountId, String toAccountId, BigDecimal amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        logger.info("Transfer amount : "+amount+", from "+fromAccountId+", to "+toAccountId);
        log.info("Transfer amount : {} from {}, to {}", amount, fromAccountId, toAccountId);
        debit(fromAccountId, amount, description);
        credit(toAccountId, amount, description);
    }

    @Override
    public List<BankAccountDTO> listCustomerBankAccounts(Long customerId) throws CustomerNotFoundException {
        Customer customer = findCustomerById(customerId);
        List<BankAccount> bankAccounts = bankAccountRepository.findByCustomerId(customer.getId());
        return bankAccounts
                .stream()
                .map(bankAccountMapper::bankAccountToBankAccountDTO)
                .toList();
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        return bankAccountMapper.fromCustomer(findCustomerById(customerId));
    }

    @Override
    public List<AccountOperationDTO> accountHistory(String accoundId) {
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accoundId);
        return accountOperations
                .stream()
                .map(bankAccountMapper::fromAccountOperation)
                .toList();
    }

    @Override
    public AccountHistoryDTO getPageableAccountHistory(String accountNumber, int page, int size) throws BankAccountNotFoundException {

        BankAccount bankAccount = findBankAccountById(accountNumber);

        Page<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountNumber, PageRequest.of(page, size));
        List<AccountOperationDTO> accountOperationDTOS = accountOperations
                .getContent()
                .stream()
                .map(bankAccountMapper::fromAccountOperation)
                .toList();

        return bankAccountMapper.toAccountHistoryDTO(
                accountNumber,
                bankAccount.getBalance(),
                accountOperationDTOS,
                accountOperations.getTotalPages(),
                page,
                size);
    }

    @Override
    public List<CustomerDTO> searchCustomers(String keyword) {
        List<Customer> customers=customerRepository.searchCustomer(keyword);
        return customers.stream().map(bankAccountMapper::fromCustomer)
                .toList();
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
            return new BankAccountNotFoundException("Bank Account "+bankAccountId+" not found");
        });
    }

    private void saveOperation(OperationType operationType, String accountId, BigDecimal amount, String description) throws BalanceNotSufficientException, BankAccountNotFoundException {
        BankAccount bankAccount = findBankAccountById(accountId);

        if (operationType.equals(OperationType.DEBIT)) {
            if (bankAccount.getBalance().compareTo(amount) < 0) {
                throw new BalanceNotSufficientException("Balance not sufficient");
            }
        }

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(operationType);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(LocalDateTime.now());
        accountOperation.setBankAccount(bankAccount);

        accountOperationRepository.save(accountOperation);

        if (operationType.equals(OperationType.DEBIT)) {
            bankAccount.setBalance(bankAccount.getBalance().subtract(amount));
        } else {
            bankAccount.setBalance(bankAccount.getBalance().add(amount));
        }

        bankAccountRepository.save(bankAccount);
    }

}
