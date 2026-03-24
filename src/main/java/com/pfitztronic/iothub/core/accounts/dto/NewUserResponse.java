package com.pfitztronic.iothub.core.accounts.dto;

public record NewUserResponse(
        String userId,
        String userName,
        boolean verified,
        String status
) {}
