package com.pfitztronic.iothub.core.accounts.models;

import com.pfitztronic.iothub.core.accounts.exceptions.InvalidUserIdentityException;
import com.pfitztronic.iothub.core.accounts.models.PhoneNumber;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberTest {

    @Test
    public void setValidPhoneNumber() {
        String validNumber = "+12345678901";
        PhoneNumber phoneNumber = new PhoneNumber(validNumber);
        assertEquals(validNumber, phoneNumber.number());
    }

    @Test
    public void setInvalidPhoneNumber_NoPlusSign() {
        String invalidNumber = "12345678901";
        Exception exception = assertThrows(InvalidUserIdentityException.class, () -> {
            new PhoneNumber(invalidNumber);
        });
        assertEquals("Phone should start with + and be between 11 and 14 digits long.", exception.getMessage());
    }

    @Test
    public void setInvalidPhoneNumber_TooShort() {
        String invalidNumber = "+1234567";
        Exception exception = assertThrows(InvalidUserIdentityException.class, () -> {
            new PhoneNumber(invalidNumber);
        });
        assertEquals("Phone should start with + and be between 11 and 14 digits long.", exception.getMessage());
    }

    @Test
    public void setInvalidPhoneNumber_TooLong() {
        String invalidNumber = "+12345678901234567";
        Exception exception = assertThrows(InvalidUserIdentityException.class, () -> {
            new PhoneNumber(invalidNumber);
        });
        assertEquals("Phone should start with + and be between 11 and 14 digits long.", exception.getMessage());
    }

    @Test
    public void setInvalidPhoneNumber_NonDigitCharacters() {
        String invalidNumber = "+12345abc901";
        Exception exception = assertThrows(InvalidUserIdentityException.class, () -> {
            new PhoneNumber(invalidNumber);
        });
        assertEquals("Phone should start with + and be between 11 and 14 digits long.", exception.getMessage());
    }

}