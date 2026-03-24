package com.pfitztronic.iothub.core.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyUserAccountData(
        @NotBlank
        @Size(min = 4, max = 10, message = "Verification code must be between 4 and 10 digits long")
        String code
) {}
