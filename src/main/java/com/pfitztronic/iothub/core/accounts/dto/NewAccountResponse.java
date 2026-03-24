package com.pfitztronic.iothub.core.accounts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NewAccountResponse(
        String accountId,
        String accountName,
        String adminId
) {
}
