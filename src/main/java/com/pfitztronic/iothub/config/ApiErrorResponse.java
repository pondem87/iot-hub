package com.pfitztronic.iothub.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record ApiErrorResponse(
        @JsonProperty("text_code")
        String textCode,
        Map<String, String> errors
) {
}
