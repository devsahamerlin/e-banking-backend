package com.merlin.digitalbanking.ebankingbackend.exceptions;

import java.math.BigDecimal;

public class BalanceNotSufficientException extends Exception {
    private final BigDecimal availableBalance;
    private final BigDecimal requestedAmount;

    public BalanceNotSufficientException(String message) {
        super(message);
        this.availableBalance = null;
        this.requestedAmount = null;
    }

    public BalanceNotSufficientException(String message, BigDecimal availableBalance, BigDecimal requestedAmount) {
        super(message);
        this.availableBalance = availableBalance;
        this.requestedAmount = requestedAmount;
    }

    public BalanceNotSufficientException(BigDecimal availableBalance, BigDecimal requestedAmount) {
        super("Insufficient balance. Available: " + availableBalance + ", Requested: " + requestedAmount);
        this.availableBalance = availableBalance;
        this.requestedAmount = requestedAmount;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
}
