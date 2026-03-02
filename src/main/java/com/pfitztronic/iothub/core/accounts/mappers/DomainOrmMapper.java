package com.pfitztronic.iothub.core.accounts.mappers;

import com.pfitztronic.iothub.core.accounts.models.*;
import com.pfitztronic.iothub.core.accounts.orm_models.AccountEntity;
import com.pfitztronic.iothub.core.accounts.orm_models.UserEntity;
import com.pfitztronic.iothub.core.accounts.orm_models.VerificationCodeEntity;

public class DomainOrmMapper {
    public static UserEntity toUserEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setUserId(user.getUserId().number());
        entity.setName(user.getName());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setStatus(user.getStatus().name());
        entity.setVerified(user.isVerified());
        entity.setCreatedAt(entity.getCreatedAt());
        return entity;
    }

    public static User toUserModel(UserEntity entity) {
        return User.builder()
                        .userId(new PhoneNumber(entity.getUserId()))
                        .name(entity.getName())
                        .passwordHash(entity.getPasswordHash())
                        .status(UserStatus.valueOf(entity.getStatus()))
                        .verified(entity.isVerified())
                        .createdAt(entity.getCreatedAt())
                        .build();
    }

    public static Account toAccountModel(AccountEntity entity) {
        return Account.builder()
                .accountId(entity.getAccountId())
                .accountName(new AccountName(entity.getAccountName()))
                .adminId(new PhoneNumber(entity.getAdminId()))
                .status(AccountStatus.valueOf(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static AccountEntity toAccountEntity(Account account) {
        AccountEntity entity = new AccountEntity();
        entity.setAccountId(account.getAccountId());
        entity.setAccountName(account.getAccountName().value());
        entity.setAdminId(account.getAdminId().number());
        entity.setStatus(account.getStatus().name());
        entity.setCreatedAt(account.getCreatedAt());
        return entity;
    }

    public static VerificationCode toVerificationCode(VerificationCodeEntity entity) {
        VerificationCode code = new VerificationCode();
        code.setId(entity.getId());
        code.setUserId(new PhoneNumber(entity.getUserId()));
        code.setCodeHash(entity.getCodeHash());
        code.setCreatedAt(entity.getCreatedAt());
        return code;
    }

    public static VerificationCodeEntity toVerificationCodeEntity(VerificationCode code) {
        VerificationCodeEntity entity = new VerificationCodeEntity();
        entity.setId(code.getId());
        entity.setUserId(code.getUserId().number());
        entity.setCodeHash(code.getCodeHash());
        entity.setCreatedAt(code.getCreatedAt());
        return entity;
    }
}
