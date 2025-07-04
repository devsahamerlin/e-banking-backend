package com.merlin.digitalbanking.ebankingbackend.web;

import com.merlin.digitalbanking.ebankingbackend.dto.*;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.CustomerNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.services.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BankAccountRestController {
    private final BankAccountService bankAccountService;

    @GetMapping("/accounts/{accountNumber}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public BankAccountDTO getBankAccount(@PathVariable String accountNumber) throws BankAccountNotFoundException {
        return bankAccountService.getBankAccount(accountNumber);
    }

    @GetMapping("/customers/{customerId}/accounts")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public List<BankAccountDTO> getCustomerAccounts(@PathVariable Long customerId) throws CustomerNotFoundException {
        return bankAccountService.listCustomerBankAccounts(customerId);
    }

    @GetMapping("/accounts/{accountNumber}/operations")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public List<AccountOperationDTO> getAccountHistory(@PathVariable String accountNumber) {
        return bankAccountService.accountHistory(accountNumber);
    }

    @GetMapping("/accounts/{accountNumber}/pageOperations")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public AccountHistoryDTO getPageableAccountHistory(
            @PathVariable String accountNumber,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) throws BankAccountNotFoundException {
        return bankAccountService.getPageableAccountHistory(accountNumber, page, size);
    }

    @PostMapping("/accounts/credit")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public void credit(@RequestBody OperationDTO operationDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        bankAccountService.credit(operationDTO.accountId(), operationDTO.amount(), operationDTO.description());
    }

    @PostMapping("/accounts/debit")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public void debit(@RequestBody OperationDTO operationDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        bankAccountService.debit(operationDTO.accountId(), operationDTO.amount(), operationDTO.description());
    }

    @PostMapping("/accounts/transfer")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN')")
    public void transfert(@RequestBody TransferDTO transferDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        bankAccountService.transfer(transferDTO.fromAccountId(), transferDTO.toAccountId(), transferDTO.amount(), transferDTO.description());
    }
}
