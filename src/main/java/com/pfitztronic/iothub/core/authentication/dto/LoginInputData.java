package com.pfitztronic.iothub.core.authentication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginInputData(
        @NotNull
        @Pattern(
                regexp = "^\\+\\d{11,14}$",
                message = "Phone should start with + and be between 11 and 14 digits long"
        )
        @JsonProperty("phone_number")
        String phoneNumber,
        @NotNull
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) { }
