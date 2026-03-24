package com.pfitztronic.iothub.core.accounts.exceptions;

public class InvalidPasswordResetCodeException extends RuntimeException {
    public InvalidPasswordResetCodeException(String message) {
        super(message);
    }
}
