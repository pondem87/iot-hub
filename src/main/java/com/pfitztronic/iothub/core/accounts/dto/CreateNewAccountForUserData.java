package com.pfitztronic.iothub.core.accounts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateNewAccountForUserData(
        @NotNull
        @Pattern(
                regexp = "^\\+\\d{11,14}$",
                message = "Phone should start with + and be between 11 and 14 digits long"
        )
        @JsonProperty("user_id")
        String userId,
        @NotNull
        @Size(min = 3, max = 100, message = "Account name must be between 3 and 100 characters long")
        @JsonProperty("account_name")
        String accountName
) {
}
