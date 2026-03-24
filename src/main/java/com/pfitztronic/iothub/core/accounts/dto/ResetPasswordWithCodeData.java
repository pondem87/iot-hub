package com.pfitztronic.iothub.core.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordWithCodeData(
        @NotBlank
        @Size(min = 4, max = 10, message = "Code must be between 4 and 10 characters long")
        String code,
        @NotBlank
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters long")
        String newPassword
) {}
