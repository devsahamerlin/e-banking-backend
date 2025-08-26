package com.merlin.digitalbanking.ebankingbackend;

import com.merlin.digitalbanking.ebankingbackend.dto.BankAccountDTO;
import com.merlin.digitalbanking.ebankingbackend.dto.CreateAccountDTO;
import com.merlin.digitalbanking.ebankingbackend.dto.CustomerDTO;
import com.merlin.digitalbanking.ebankingbackend.dto.UserDTO;
import com.merlin.digitalbanking.ebankingbackend.entities.*;
import com.merlin.digitalbanking.ebankingbackend.enums.AccountStatus;
import com.merlin.digitalbanking.ebankingbackend.enums.OperationType;
import com.merlin.digitalbanking.ebankingbackend.enums.UserRole;
import com.merlin.digitalbanking.ebankingbackend.exceptions.AccountStatusException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.CustomerNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.mappers.BankAccountMapper;
import com.merlin.digitalbanking.ebankingbackend.repositories.AccountOperationRepository;
import com.merlin.digitalbanking.ebankingbackend.repositories.BankAccountRepository;
import com.merlin.digitalbanking.ebankingbackend.repositories.CustomerRepository;
import com.merlin.digitalbanking.ebankingbackend.repositories.UserRepository;
import com.merlin.digitalbanking.ebankingbackend.services.BankAccountService;
import com.merlin.digitalbanking.ebankingbackend.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
@Slf4j
public class EBankingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EBankingBackendApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(BankAccountService bankAccountService, UserRepository userRepository,
                                        PasswordEncoder passwordEncoder, BankAccountMapper bankAccountMapper, UserService userService) {
        return args -> {

            User admin = new User();
            admin.setUsername("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("12345678"));
            admin.setEmail("admin@gmail.com");
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRole(UserRole.ADMIN);
            admin.setIsActive(true);
            User savedUser = userRepository.save(admin);
            log.info("Created default admin user: {}", savedUser.getUsername());

            User manager = new User();
            manager.setUsername("manager@gmail.com");
            manager.setPassword(passwordEncoder.encode("12345678"));
            manager.setEmail("manager@gmail.com");
            manager.setFirstName("Manager");
            manager.setLastName("User");
            manager.setRole(UserRole.MANAGER);
            manager.setIsActive(true);
            User savedManagerUser = userRepository.save(manager);
            log.info("Created default manager user: {}", savedManagerUser.getUsername());

            UserDTO authenticateAdmin = userService.findByUsername("admin@gmail.com");
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("SCOPE_ROLE_ADMIN")
            );

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    authenticateAdmin.username(),
                    null,
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("System authentication established for user: {}", authenticateAdmin.username());

            Stream.of("Merlin","Nata","Eme").forEach(name -> {
                CustomerDTO customer=new CustomerDTO(null,name, name+"@gmail.com", LocalDateTime.now(), null, authenticateAdmin, null);
                bankAccountService.saveCustomer(customer);
            });

            bankAccountService.listCustomers().forEach(customer -> {
                try {
                    bankAccountService.createAccount(new CreateAccountDTO(BigDecimal.valueOf(Math.random()*90000), customer.id(), "CURRENT", BigDecimal.valueOf(9000), null));
                    bankAccountService.createAccount(new CreateAccountDTO(BigDecimal.valueOf(Math.random()*120000), customer.id(), "SAVING", null, BigDecimal.valueOf(5.5)));

                    List<BankAccountDTO> bankAccounts = bankAccountService.listCustomerBankAccounts(customer.id());

                    for (BankAccountDTO bankAccount : bankAccounts) {
                        for (int i = 0; i <10; i++){

                            bankAccountService.credit(bankAccount.id(),BigDecimal.valueOf(10000+Math.random()*120000),"Credit");
                            bankAccountService.debit(bankAccount.id(), BigDecimal.valueOf(1000+Math.random()*9000), "Debit");
                        }
                    }

                } catch (CustomerNotFoundException | BankAccountNotFoundException | BalanceNotSufficientException e) {
                    log.error(e.getMessage());
                    throw new RuntimeException(e.getMessage());
                } catch (AccountStatusException e) {
                    throw new RuntimeException(e);
                }
            });
        };
    }

    //@Bean
    CommandLineRunner start(CustomerRepository customerRepository,
                            BankAccountRepository bankAccountRepository,
                            AccountOperationRepository accountOperationRepository) {
        return args -> {

            Stream.of("Merlin","Murielle","Franck").forEach(name -> {
                Customer customer=new Customer();
                customer.setName(name);
                customer.setEmail(name+"@gmail.com");
                customerRepository.save(customer);
            });

            customerRepository.findAll().forEach(customer -> {
                CurrentAccount currentAccount = new CurrentAccount();

                currentAccount.setId(UUID.randomUUID().toString());
                currentAccount.setCustomer(customer);
                currentAccount.setBalance(BigDecimal.valueOf(Math.random() * 90000));
                currentAccount.setStatus(AccountStatus.CREATED);
                currentAccount.setCreatedAt(LocalDateTime.now());
                currentAccount.setOverDraft(BigDecimal.valueOf(9000));
                bankAccountRepository.save(currentAccount);

                SavingAccount savingAccount = new SavingAccount();
                savingAccount.setId(UUID.randomUUID().toString());
                savingAccount.setCustomer(customer);
                savingAccount.setBalance(BigDecimal.valueOf(Math.random() * 90000));
                savingAccount.setStatus(AccountStatus.CREATED);
                savingAccount.setCreatedAt(LocalDateTime.now());
                savingAccount.setInterestRate(BigDecimal.valueOf(5.5));
                bankAccountRepository.save(savingAccount);
            });

            bankAccountRepository.findAll().forEach(bankAccount -> {
                for (int i = 0; i <10; i++){
                    AccountOperation accountOperation = new AccountOperation();
                    accountOperation.setOperationDate(LocalDateTime.now());
                    accountOperation.setAmount(BigDecimal.valueOf(Math.random() * 12000));
                    accountOperation.setType(Math.random()>0.5? OperationType.DEBIT:OperationType.CREDIT);
                    accountOperation.setBankAccount(bankAccount);
                    accountOperationRepository.save(accountOperation);
                }
            });

//            List<BankAccount> bankAccounts = bankAccountRepository.findAll();
//            BankAccount bankAccount = bankAccounts.get(0);
//
//            if (bankAccount.getId() != null) {
//                System.out.println("*************************************");
//                System.out.println(bankAccount.getId());
//                System.out.println(bankAccount.getStatus());
//                System.out.println(bankAccount.getCreatedAt());
//                System.out.println(bankAccount.getCustomer().getName());
//
//                if (bankAccount instanceof CurrentAccount) {
//                    System.out.println("Over Draft=>" + ((CurrentAccount) bankAccount).getOverDraft());
//                }
//
//                if (bankAccount instanceof SavingAccount) {
//                    System.out.println("Rate=>" + ((SavingAccount) bankAccount).getInterestRate());
//                }
//
//                bankAccount.getAccountOperations().forEach(accountOperation -> {
//                    System.out.println("####################################");
//                    System.out.println(accountOperation.getType() + "\t" + accountOperation.getOperationDate() + "\t" + accountOperation.getAmount());
//                });
//            }

        };
    }
}
