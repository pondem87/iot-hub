package com.pfitztronic.iothub.core.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserAccountData(
        @NotBlank
        @Pattern(
                regexp = "^\\+\\d{11,14}$",
                message = "Phone should start with + and be between 11 and 14 digits long"
        )
        String phoneNumber,
        @NotBlank
        @Size(min = 1, max = 100)
        String userName,
        @NotBlank
        @Size(min = 8, message = "Password must be at least 8 characters long and less than 50", max = 50)
        String password,
        @NotBlank
        String invitationId
) {}
