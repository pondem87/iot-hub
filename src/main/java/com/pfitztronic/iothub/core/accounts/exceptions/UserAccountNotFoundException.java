package com.pfitztronic.iothub.core.accounts.exceptions;

public class UserAccountNotFoundException extends RuntimeException {
    public UserAccountNotFoundException(String message) {
        super(message);
    }
}
