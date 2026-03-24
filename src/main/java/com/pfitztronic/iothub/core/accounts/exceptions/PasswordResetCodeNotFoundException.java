package com.pfitztronic.iothub.core.accounts.exceptions;

public class PasswordResetCodeNotFoundException extends RuntimeException {
    public PasswordResetCodeNotFoundException(String message) {
        super(message);
    }
}
