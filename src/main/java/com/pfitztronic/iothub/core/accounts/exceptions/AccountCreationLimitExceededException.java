package com.pfitztronic.iothub.core.accounts.exceptions;

public class AccountCreationLimitExceededException extends RuntimeException {
    public AccountCreationLimitExceededException(String message) {
        super(message);
    }
}
