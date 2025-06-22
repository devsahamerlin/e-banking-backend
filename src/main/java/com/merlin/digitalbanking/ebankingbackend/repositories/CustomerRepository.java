package com.merlin.digitalbanking.ebankingbackend.repositories;

import com.merlin.digitalbanking.ebankingbackend.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
