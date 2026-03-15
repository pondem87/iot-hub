package com.pfitztronic.iothub.core.authorisation.dto;

public record CreateRoleInputData(
        String accountId,
        String roleName,
        String description
) {
}
