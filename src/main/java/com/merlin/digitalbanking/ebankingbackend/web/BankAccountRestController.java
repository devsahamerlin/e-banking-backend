package com.merlin.digitalbanking.ebankingbackend.web;

import com.merlin.digitalbanking.ebankingbackend.dto.*;
import com.merlin.digitalbanking.ebankingbackend.exceptions.AccountStatusException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.CustomerNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.services.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BankAccountRestController {
    private final BankAccountService bankAccountService;

    @GetMapping("/accounts/{accountNumber}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_MANAGER')")
    public ResponseEntity<BankAccountDTO> getBankAccount(@PathVariable String accountNumber) throws BankAccountNotFoundException {
        return ResponseEntity.ok(bankAccountService.getBankAccount(accountNumber));
    }

    @GetMapping("/customers/{customerId}/accounts")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_MANAGER')")
    public ResponseEntity<List<BankAccountDTO>> getCustomerAccounts(@PathVariable Long customerId) throws CustomerNotFoundException {
        return ResponseEntity.ok(bankAccountService.listCustomerBankAccounts(customerId));
    }

    @GetMapping("/accounts/{accountNumber}/operations")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_MANAGER')")
    public ResponseEntity<List<AccountOperationDTO>> getAccountHistory(@PathVariable String accountNumber) {
        return ResponseEntity.ok(bankAccountService.accountHistory(accountNumber));
    }

    @GetMapping("/accounts/{accountNumber}/pageOperations")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_MANAGER')")
    public ResponseEntity<AccountHistoryDTO> getPageableAccountHistory(
            @PathVariable String accountNumber,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) throws BankAccountNotFoundException {
        return ResponseEntity.ok(bankAccountService.getPageableAccountHistory(accountNumber, page, size));
    }

    @PostMapping("/accounts/credit")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_MANAGER')")
    public ResponseEntity<Void> credit(@RequestBody OperationDTO operationDTO) throws BankAccountNotFoundException, BalanceNotSufficientException, AccountStatusException {
        bankAccountService.credit(operationDTO.accountId(), operationDTO.amount(), operationDTO.description(), LocalDateTime.now());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accounts/debit")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_MANAGER')")
    public ResponseEntity<Void> debit(@RequestBody OperationDTO operationDTO) throws BankAccountNotFoundException, BalanceNotSufficientException, AccountStatusException {
        bankAccountService.debit(operationDTO.accountId(), operationDTO.amount(), operationDTO.description(), LocalDateTime.now());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accounts/transfer")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_MANAGER')")
    public ResponseEntity<Void> transfer(@RequestBody TransferDTO transferDTO) throws BankAccountNotFoundException, BalanceNotSufficientException, AccountStatusException {
        bankAccountService.transfer(transferDTO.fromAccountId(), transferDTO.toAccountId(), transferDTO.amount(), transferDTO.description(), LocalDateTime.now());
        return ResponseEntity.ok().build();
    }
}
