package com.pfitztronic.iothub.core.accounts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NewAccountResponse(
        @JsonProperty("account_id")
        String accountId,
        @JsonProperty("account_name")
        String accountName,
        @JsonProperty("admin_id")
        String adminId
) {
}
