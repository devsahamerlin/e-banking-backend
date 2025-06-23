package com.merlin.digitalbanking.ebankingbackend.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("CA")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentAccount extends BankAccount{
    private BigDecimal overDraft;
}
