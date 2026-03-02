package com.pfitztronic.iothub.core.accounts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record CreateNewAccountData(
        @NotNull
        @Pattern(
                regexp = "^\\+\\d{11,14}$",
                message = "Phone should start with + and be between 11 and 14 digits long"
        )
        @JsonProperty("phone_number")
        String phoneNumber,
        @NotNull
        @Size(min = 3, max = 50, message = "User name must be between 3 and 50 characters long")
        @JsonProperty("user_name")
        String userName,
        @NotNull
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,
        @NotNull
        @Size(min = 3, max = 100, message = "Account name must be between 3 and 100 characters long")
        @JsonProperty("account_name")
        String accountName
) {
}
