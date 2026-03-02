package com.pfitztronic.iothub.core.accounts.exceptions;

public class AccountNameAlreadyExistsException extends RuntimeException {
    public AccountNameAlreadyExistsException(String message) {
        super(message);
    }
}
