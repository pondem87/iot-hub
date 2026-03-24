package com.pfitztronic.iothub.core.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordData(
        @NotBlank
        @Size(min = 1, max = 100)
        String oldPassword,
        @NotBlank
        @Size(min = 8, message = "Password must be at least 8 characters long and less than 50", max = 50)
        String newPassword
) {}
