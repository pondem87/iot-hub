package com.pfitztronic.iothub.core.accounts.exceptions;

public class InvalidOldPasswordException extends RuntimeException {
    public InvalidOldPasswordException(String message) {
        super(message);
    }
}
