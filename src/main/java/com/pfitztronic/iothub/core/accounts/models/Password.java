package com.pfitztronic.iothub.core.accounts.models;

import com.pfitztronic.iothub.core.accounts.exceptions.InvalidPasswordFormatException;

public record Password(String password) {

    public Password(String password) {
        this.password = validatePassword(password);
    }

    private static String validatePassword(String password) {
        if (password.length() < 8) {
            throw new InvalidPasswordFormatException("Password must be at least 8 characters long.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new InvalidPasswordFormatException("Password must contain at least one uppercase letter.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new InvalidPasswordFormatException("Password must contain at least one lowercase letter.");
        }
        if (!password.matches(".*\\d.*")) {
            throw new InvalidPasswordFormatException("Password must contain at least one digit.");
        }
        if (!password.matches(".*[!@#$%^&*()].*")) {
            throw new InvalidPasswordFormatException("Password must contain at least one special character (!@#$%^&*()).");
        }
        return password;
    }

}
