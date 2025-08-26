package com.merlin.digitalbanking.ebankingbackend.repositories;

import com.merlin.digitalbanking.ebankingbackend.entities.Customer;
import com.merlin.digitalbanking.ebankingbackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:keyword% OR c.email LIKE %:keyword%")
    List<Customer> searchCustomer(@Param("keyword") String keyword);

    List<Customer> findByCreatedBy(User user);
}
