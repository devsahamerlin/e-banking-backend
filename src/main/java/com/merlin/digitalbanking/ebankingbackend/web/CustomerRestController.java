package com.merlin.digitalbanking.ebankingbackend.web;

import com.merlin.digitalbanking.ebankingbackend.dto.CustomerDTO;
import com.merlin.digitalbanking.ebankingbackend.exceptions.CustomerNotFoundException;
import com.merlin.digitalbanking.ebankingbackend.services.BankAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200")
public class CustomerRestController {
    private final BankAccountService bankAccountService;

    @GetMapping("/customers")
    public List<CustomerDTO> getCustomers() {
        return bankAccountService.listCustomers();
    }

    @GetMapping("/customers/search")
    public List<CustomerDTO> searchCustomers(@RequestParam(name = "keyword",defaultValue = "") String keyword){
        return bankAccountService.searchCustomers("%"+keyword+"%");
    }

    @GetMapping("/customers/{customerId}")
    public CustomerDTO getCustomer(@PathVariable Long customerId) throws CustomerNotFoundException {
        return bankAccountService.getCustomer(customerId);
    }

    @PostMapping("/customers")
    public CustomerDTO getCustomer(@RequestBody CustomerDTO customerDTO) {
        return bankAccountService.saveCustomer(customerDTO);
    }

    @PutMapping("/customers/{customerId}")
    public CustomerDTO updateCustomers(@PathVariable Long customerId, @RequestBody CustomerDTO customerDTO) {
        return bankAccountService
                .updateCustomer(new CustomerDTO(customerId,
                        customerDTO.name(), customerDTO.email()));
    }

    @DeleteMapping("/customers/{customerId}")
    public void deleteCustomer(@PathVariable Long customerId) {
        bankAccountService.deleteCustomer(customerId);
    }
}
