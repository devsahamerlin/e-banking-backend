package com.merlin.digitalbanking.ebankingbackend.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("SA")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavingAccount extends BankAccount{
    private BigDecimal interestRate;
}
