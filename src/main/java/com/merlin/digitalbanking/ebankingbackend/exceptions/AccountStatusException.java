package com.merlin.digitalbanking.ebankingbackend.exceptions;

import com.merlin.digitalbanking.ebankingbackend.enums.AccountStatus;

public class AccountStatusException extends Exception {
    private final String accountId;

    public AccountStatusException(String message) {
        super(message);
        this.accountId = null;
    }

    public AccountStatusException(String message, String accountId) {
        super(message);
        this.accountId = accountId;
    }

    public AccountStatusException(String accountId, AccountStatus status) {
        super("Account " + accountId + " is currently " + status);
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }
}
