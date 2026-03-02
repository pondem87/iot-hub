package com.pfitztronic.iothub.core.accounts.models;

import com.pfitztronic.iothub.core.accounts.exceptions.InvalidPasswordFormatException;
import com.pfitztronic.iothub.core.accounts.models.Password;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordTest {

    @Test
    public void setValidPassword() {
        String validPassword = "StrongP@ssw0rd!";
        Password password = new Password(validPassword);
        assertEquals(validPassword, password.password());
    }

    @Test
    public void setInvalidPassword_TooShort() {
        String invalidPassword = "Shrt1!";
        Exception exception = assertThrows(InvalidPasswordFormatException.class, () -> {
            new Password(invalidPassword);
        });
        assertEquals("Password must be at least 8 characters long.", exception.getMessage());
    }

    @Test
    public void setInvalidPassword_NoUppercase() {
        String invalidPassword = "weakp@ss1!";
        Exception exception = assertThrows(InvalidPasswordFormatException.class, () -> {
            new Password(invalidPassword);
        });
        assertEquals("Password must contain at least one uppercase letter.", exception.getMessage());
    }

    @Test
    public void setInvalidPassword_NoLowercase() {
        String invalidPassword = "WEAKP@SS1!";
        Exception exception = assertThrows(InvalidPasswordFormatException.class, () -> {
            new Password(invalidPassword);
        });
        assertEquals("Password must contain at least one lowercase letter.", exception.getMessage());
    }

    @Test
    public void setInvalidPassword_NoDigit() {
        String invalidPassword = "WeakP@ss!";
        Exception exception = assertThrows(InvalidPasswordFormatException.class, () -> {
            new Password(invalidPassword);
        });
        assertEquals("Password must contain at least one digit.", exception.getMessage());
    }

    @Test
    public void setInvalidPassword_NoSpecialCharacter() {
        String invalidPassword = "WeakPass1";
        Exception exception = assertThrows(InvalidPasswordFormatException.class, () -> {
            new Password(invalidPassword);
        });
        assertEquals("Password must contain at least one special character (!@#$%^&*()).", exception.getMessage());
    }
}