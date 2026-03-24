package com.pfitztronic.iothub.core.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeUserDetailsData(
        @NotBlank
        @Size(min = 1, max = 50)
        String userName
) {}
