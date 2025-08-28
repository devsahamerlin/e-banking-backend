package com.merlin.digitalbanking.ebankingbackend.web;

import com.merlin.digitalbanking.ebankingbackend.dto.AccountOperationDTO;
import com.merlin.digitalbanking.ebankingbackend.dto.BankAccountDTO;
import com.merlin.digitalbanking.ebankingbackend.dto.CreateAccountDTO;
import com.merlin.digitalbanking.ebankingbackend.enums.AccountStatus;
import com.merlin.digitalbanking.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.exceptions.CustomerNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.services.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class BankAccountAdminRestController {

    private final BankAccountService bankAccountService;

    @PostMapping("/accounts")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_MANAGER')")
    public ResponseEntity<BankAccountDTO> createAccount(@RequestBody CreateAccountDTO createAccountDTO) throws CustomerNotFoundException {
        BankAccountDTO account = bankAccountService.createAccount(createAccountDTO);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/accounts")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_MANAGER')")
    public ResponseEntity<List<BankAccountDTO>> getAccounts() throws CustomerNotFoundException {
        List<BankAccountDTO> accounts = bankAccountService.getAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/accounts/search")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_MANAGER')")
    public ResponseEntity<List<BankAccountDTO>> searchAccounts(@RequestParam String keyword) {
        List<BankAccountDTO> accounts = bankAccountService.searchAccounts(keyword);
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/accounts/{accountId}/status/{accountStatus}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<Void> updateAccountStatus(@PathVariable String accountId, @PathVariable AccountStatus accountStatus) throws BankAccountNotFoundException {
        bankAccountService.updateAccountStatus(accountId, accountStatus);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}/operations")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<List<AccountOperationDTO>> getUserOperations(@PathVariable Long userId) {
        List<AccountOperationDTO> operations = bankAccountService.getUserOperations(userId);
        return ResponseEntity.ok(operations);
    }
}
