package com.pfitztronic.iothub.core.authorisation.services.impl;

import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IAccountCreatedEventHandler;

import java.util.UUID;

public class AccountCreatedEventHandler implements IAccountCreatedEventHandler {
    private final UserRolesService userRolesService;

    public AccountCreatedEventHandler(UserRolesService userRolesService) {
        this.userRolesService = userRolesService;
    }

    @Override
    public void accountCreatedEvent(UUID accountId) {
        this.userRolesService.createDefaultRolesForAccount(accountId);
    }
}
