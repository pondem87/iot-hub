package com.pfitztronic.iothub.accounts.models;
import java.util.regex.Pattern;

public record PhoneNumber(String number) {
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\+\\d{11,14}$");

    public PhoneNumber(String number) {
        this.number = validateNumber(number);
    }

    private static String validateNumber(String number) {
        if (!PHONE_NUMBER_PATTERN.matcher(number).matches()) {
            throw new IllegalArgumentException("Phone should start with + and be between 11 and 14 digits long.");
        }
        return number;
    }
}
