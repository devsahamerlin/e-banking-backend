package com.merlin.digitalbanking.ebankingbackend.web;

import com.merlin.digitalbanking.ebankingbackend.dto.CustomerDTO;
import com.merlin.digitalbanking.ebankingbackend.exceptions.CustomerNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.services.BankAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CustomerRestController {
    private final BankAccountService bankAccountService;

    @GetMapping("/customers")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_MANAGER')")
    public ResponseEntity<List<CustomerDTO>> getCustomers() {
        return ResponseEntity.ok(bankAccountService.listCustomers());
    }

    @GetMapping("/customers/search")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_MANAGER')")
    public ResponseEntity<List<CustomerDTO>> searchCustomers(@RequestParam(name = "keyword",defaultValue = "") String keyword){
        return ResponseEntity.ok(bankAccountService.searchCustomers("%"+keyword+"%"));
    }

    @GetMapping("/customers/{customerId}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER') or hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_MANAGER')")
    public ResponseEntity<CustomerDTO> getCustomer(@PathVariable Long customerId) throws CustomerNotFoundException {
        return ResponseEntity.ok(bankAccountService.getCustomer(customerId));
    }

    @PostMapping("/customers")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_MANAGER')")
    public ResponseEntity<CustomerDTO> saveCustomer(@RequestBody CustomerDTO customerDTO) {
        return ResponseEntity.ok(bankAccountService.saveCustomer(customerDTO));
    }

    @PutMapping("/customers/{customerId}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN') or hasAuthority('SCOPE_ROLE_MANAGER')")
    public ResponseEntity<CustomerDTO> updateCustomers(@PathVariable Long customerId, @RequestBody CustomerDTO customerDTO) {
        return ResponseEntity.ok(bankAccountService
                .updateCustomer(new CustomerDTO(customerId,
                        customerDTO.name(), customerDTO.email(), null, null, null, null)));
    }

    @DeleteMapping("/customers/{customerId}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long customerId) {
        bankAccountService.deleteCustomer(customerId);
        return ResponseEntity.ok().build();
    }
}
