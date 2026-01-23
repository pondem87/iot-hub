package com.pfitztronic.iothub.accounts.models;

public record Password(String password) {

    public Password(String password) {
        this.password = validatePassword(password);
    }

    private static String validatePassword(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter.");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit.");
        }
        if (!password.matches(".*[!@#$%^&*()].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character (!@#$%^&*()).");
        }
        return password;
    }

}
