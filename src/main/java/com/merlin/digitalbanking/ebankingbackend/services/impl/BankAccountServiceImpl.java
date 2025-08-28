package com.merlin.digitalbanking.ebankingbackend.services.impl;

import com.merlin.digitalbanking.ebankingbackend.dto.*;
import com.merlin.digitalbanking.ebankingbackend.entities.*;
import com.merlin.digitalbanking.ebankingbackend.enums.AccountStatus;
import com.merlin.digitalbanking.ebankingbackend.enums.OperationType;
import com.merlin.digitalbanking.ebankingbackend.exceptions.AccountStatusException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.CustomerNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.mappers.BankAccountMapper;
import com.merlin.digitalbanking.ebankingbackend.mappers.OperationsMapper;
import com.merlin.digitalbanking.ebankingbackend.repositories.AccountOperationRepository;
import com.merlin.digitalbanking.ebankingbackend.repositories.BankAccountRepository;
import com.merlin.digitalbanking.ebankingbackend.repositories.CustomerRepository;
import com.merlin.digitalbanking.ebankingbackend.services.BankAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.merlin.digitalbanking.ebankingbackend.repositories.UserRepository;
import com.merlin.digitalbanking.ebankingbackend.services.UserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {
    private static final String DEFAULT_ACCOUNT = "CURRENT";
    private final CustomerRepository customerRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountOperationRepository accountOperationRepository;
    private final BankAccountMapper bankAccountMapper;
    private final OperationsMapper operationsMapper;
    private final UserRepository userRepository;
    private final UserService userService;


    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {

        log.info("Save customer with user tracking: {}", customerDTO.name());

        UserDTO currentUser = userService.getCurrentUser();
        Customer customer = bankAccountMapper.fromCustomerDTO(customerDTO);
        customer.setCreatedBy(bankAccountMapper.fromUserDTO(currentUser));

        Customer savedCustomer = customerRepository.save(customer);

        return bankAccountMapper.fromCustomer(savedCustomer);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {

        log.info("Update customer with user tracking: {}", customerDTO.name());

        UserDTO currentUser = userService.getCurrentUser();
        Customer customer = bankAccountMapper.fromCustomerDTO(customerDTO);
        customer.setUpdatedBy(bankAccountMapper.fromUserDTO(currentUser));

        Customer updatedCustomer = customerRepository.save(customer);
        return bankAccountMapper.fromCustomer(updatedCustomer);
    }

    @Override
    public void deleteCustomer(Long customerId) {
        log.info("Delete customer : {}", customerId);
        customerRepository
                .deleteById(customerId);
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
        return bankAccounts.stream().map(bankAccount -> {
            if (bankAccount instanceof SavingAccount savingAccount) {
                return bankAccountMapper.fromSavingBankAccount(savingAccount);
            } else {
                CurrentAccount currentAccount = (CurrentAccount) bankAccount;
                return bankAccountMapper.fromCurrentBankAccount(currentAccount);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException{
        log.info("Getting bank account: {}", accountId);
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Account not found: " + accountId));

        return bankAccountMapper.bankAccountToBankAccountDTO(bankAccount);
    }

    @Override
    public void debit(String accountId, BigDecimal amount, String description, LocalDateTime dateTime) throws BankAccountNotFoundException, BalanceNotSufficientException, AccountStatusException {
        log.info("Debit amount : {}, from {}", amount, accountId);
        saveOperation(OperationType.DEBIT, accountId, bankAccountMapper.roundAmount(amount), description, dateTime);
    }

    @Override
    public void credit(String accountId, BigDecimal amount, String description,  LocalDateTime dateTime) throws BankAccountNotFoundException, BalanceNotSufficientException, AccountStatusException {
        log.info("Credit amount : {}, to {}", amount, accountId);
        saveOperation(OperationType.CREDIT, accountId, bankAccountMapper.roundAmount(amount), description, dateTime);
    }

    @Override
    public void transfer(String fromAccountId, String toAccountId, BigDecimal amount, String description, LocalDateTime dateTime) throws BankAccountNotFoundException, BalanceNotSufficientException, AccountStatusException {
        log.info("Transfer amount : {} from {}, to {}", amount, fromAccountId, toAccountId);
        debit(fromAccountId, amount, description, dateTime);
        credit(toAccountId, amount, description, dateTime);
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

    @Override
    public BankAccountDTO createAccount(CreateAccountDTO createAccountDTO) throws CustomerNotFoundException {
        log.info("Creating {} account for customer: {}", createAccountDTO.accountType(), createAccountDTO.customerId());

        log.info("Create account DTO: {}", createAccountDTO);

        UserDTO currentUser = userService.getCurrentUser();
        Customer customer = findCustomerById(createAccountDTO.customerId());

        BankAccount account;
        if (DEFAULT_ACCOUNT.equals(createAccountDTO.accountType())) {
            CurrentAccount currentAccount = new CurrentAccount();
            currentAccount.setOverDraft(createAccountDTO.overDraft());
            account = currentAccount;
        } else {
            SavingAccount savingAccount = new SavingAccount();
            savingAccount.setInterestRate(createAccountDTO.interestRate());
            account = savingAccount;
        }

        account.setId(UUID.randomUUID().toString());
        account.setBalance(createAccountDTO.initialBalance());
        account.setCreatedAt(LocalDateTime.now());
        account.setCustomer(customer);
        account.setCreatedBy(bankAccountMapper.fromUserDTO(currentUser));
        account.setStatus(AccountStatus.CREATED);

        BankAccount savedAccount = bankAccountRepository.save(account);
        return bankAccountMapper.bankAccountToBankAccountDTO(savedAccount);
    }

    @Override
    public List<BankAccountDTO> searchAccounts(String keyword) {
        List<BankAccount> accounts = bankAccountRepository.searchAccounts(keyword);
        return accounts.stream()
                .map(bankAccountMapper::bankAccountToBankAccountDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateAccountStatus(String accountId, AccountStatus status) throws BankAccountNotFoundException {
        BankAccount account = findBankAccountById(accountId);
        account.setStatus(status);
        bankAccountRepository.save(account);
        log.info("Account {} has been {}", accountId, status);
    }

    @Override
    public List<AccountOperationDTO> getUserOperations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<AccountOperation> operations = accountOperationRepository.findByPerformedBy(user);
        return operations.stream()
                .map(operationsMapper::mapToOperationDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BankAccountDTO> getAccounts() {
        log.info("List all Accounts");
        List<BankAccount> accounts = bankAccountRepository.findAll();
        return accounts.stream().map(bankAccountMapper::fromBankAccount)
                .collect(Collectors.toList());
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


    private void saveOperation(OperationType operationType, String accountId, BigDecimal amount, String description, LocalDateTime dateTime) throws BalanceNotSufficientException,
            BankAccountNotFoundException, AccountStatusException {

        BankAccount bankAccount = findBankAccountById(accountId);

        if (bankAccount.getStatus() == AccountStatus.SUSPENDED ||
                bankAccount.getStatus() == AccountStatus.CLOSED ||
                bankAccount.getStatus() == AccountStatus.BLOCKED) {
            throw new AccountStatusException("Operation not allowed. Account " + accountId + " is currently " + bankAccount.getStatus());
        }

        UserDTO currentUser = userService.getCurrentUser();

        if (operationType.equals(OperationType.DEBIT)) {
            if (bankAccount.getBalance().compareTo(amount) < 0) {
                throw new BalanceNotSufficientException("Balance not sufficient");
            }
        }

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(operationType);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(dateTime);
        accountOperation.setBankAccount(bankAccount);
        accountOperation.setPerformedBy(bankAccountMapper.fromUserDTO(currentUser));

        accountOperationRepository.save(accountOperation);

        if (operationType.equals(OperationType.DEBIT)) {
            bankAccount.setBalance(bankAccount.getBalance().subtract(amount));
        } else {
            bankAccount.setBalance(bankAccount.getBalance().add(amount));
        }

        bankAccountRepository.save(bankAccount);
    }

}
