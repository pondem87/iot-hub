package com.pfitztronic.iothub.core.accounts.models;

public record AccountName(String value) {
    public AccountName(String value) {
        this.value = value.toLowerCase();
    }
}
