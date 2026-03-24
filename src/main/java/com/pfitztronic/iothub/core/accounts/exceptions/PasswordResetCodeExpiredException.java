package com.pfitztronic.iothub.core.accounts.exceptions;

public class PasswordResetCodeExpiredException extends RuntimeException {
    public PasswordResetCodeExpiredException(String message) {
        super(message);
    }
}
