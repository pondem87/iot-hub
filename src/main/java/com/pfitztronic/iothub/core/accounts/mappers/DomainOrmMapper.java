package com.pfitztronic.iothub.core.accounts.mappers;

import com.pfitztronic.iothub.core.accounts.models.*;
import com.pfitztronic.iothub.core.accounts.orm_models.*;

public class DomainOrmMapper {

    // user model
    public static UserEntity toUserEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setUserId(user.getUserId().number());
        entity.setName(user.getName());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setStatus(user.getStatus().name());
        entity.setVerified(user.isVerified());
        entity.setCreatedAt(user.getCreatedAt());
        return entity;
    }

    public static User toUser(UserEntity entity) {
        return User.builder()
                .userId(new PhoneNumber(entity.getUserId()))
                .name(entity.getName())
                .passwordHash(entity.getPasswordHash())
                .status(UserStatus.valueOf(entity.getStatus()))
                .verified(entity.isVerified())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // account model
    public static Account toAccount(AccountEntity entity) {
        return Account.builder()
                .accountId(entity.getAccountId())
                .accountName(new AccountName(entity.getAccountName()))
                .adminId(new PhoneNumber(entity.getAdminId()))
                .markedForDeletionAt(entity.getMarkedForDeletionAt())
                .status(AccountStatus.valueOf(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static AccountEntity toAccountEntity(Account account) {
        AccountEntity entity = new AccountEntity();
        entity.setAccountId(account.getAccountId());
        entity.setAccountName(account.getAccountName().value());
        entity.setAdminId(account.getAdminId().number());
        entity.setMarkedForDeletionAt(account.getMarkedForDeletionAt());
        entity.setStatus(account.getStatus().name());
        entity.setCreatedAt(account.getCreatedAt());
        return entity;
    }

    // verification code model
    public static VerificationCode toVerificationCode(VerificationCodeEntity entity) {
        VerificationCode code = new VerificationCode();
        code.setId(entity.getId());
        code.setUserId(new PhoneNumber(entity.getUserId()));
        code.setCodeHash(entity.getCodeHash());
        code.setCreatedAt(entity.getCreatedAt());
        code.setExpiresAt(entity.getExpiresAt());
        return code;
    }

    public static VerificationCodeEntity toVerificationCodeEntity(VerificationCode code) {
        VerificationCodeEntity entity = new VerificationCodeEntity();
        entity.setId(code.getId());
        entity.setUserId(code.getUserId().number());
        entity.setCodeHash(code.getCodeHash());
        entity.setExpiresAt(code.getExpiresAt());
        entity.setCreatedAt(code.getCreatedAt());
        return entity;
    }

    // invitation model
    public static Invitation toInvitation(InvitationEntity entity) {
        return Invitation.builder()
                .invitationId(entity.getInvitationId())
                .accountId(entity.getAccountId())
                .userId(new PhoneNumber(entity.getUserId()))
                .invitationStatus(InvitationStatus.valueOf(entity.getInvitationStatus()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static InvitationEntity toInvitationEntity(Invitation invitation) {
        InvitationEntity entity = new InvitationEntity();
        if (invitation.getInvitationId() != null) {
            entity.setInvitationId(invitation.getInvitationId());
        }
        entity.setAccountId(invitation.getAccountId());
        entity.setUserId(invitation.getUserId().number());
        entity.setInvitationStatus(invitation.getInvitationStatus().name());
        entity.setCreatedAt(invitation.getCreatedAt());
        return entity;
    }

    // accountuser model
    public static AccountUser toAccountUser(AccountUserEntity entity) {
        return AccountUser.builder()
                .account(toAccount(entity.getAccount()))
                .user(toUser(entity.getUser()))
                .roles(
                        entity.getRoles().stream()
                                .map(DomainOrmMapper::toAccountUserRole)
                                .toList()
                )
                .status(AccountUserStatus.valueOf(entity.getStatus()))
                .build();
    }

    public static AccountUserEntity toAccountUserEntity(AccountUser accountUser) {
        AccountUserEntity entity = new AccountUserEntity();
        entity.setAccount(toAccountEntity(accountUser.getAccount()));
        entity.setUser(toUserEntity(accountUser.getUser()));
        entity.setRoles(
                accountUser.getRoles().stream()
                        .map(role -> toAccountUserRolesEntity(role, entity))
                        .toList()
        );
        entity.setStatus(accountUser.getStatus().name());
        return entity;
    }

    // accountuserrole model
    public static AccountUserRole toAccountUserRole(AccountUserRolesEntity entity) {
        return AccountUserRole.builder()
                .roleId(entity.getRoleId())
                .build();
    }

    public static AccountUserRolesEntity toAccountUserRolesEntity(AccountUserRole accountUserRole, AccountUserEntity accountUserEntity) {
        AccountUserRolesEntity entity = new AccountUserRolesEntity();
        if (accountUserRole.getId() != null) {
            entity.setId(accountUserRole.getId());
        }
        entity.setAccountUser(accountUserEntity);
        entity.setRoleId(accountUserRole.getRoleId());
        return entity;
    }

    // reset code model
    public static PasswordResetCode toPasswordResetCode(PasswordResetCodeEntity entity) {
        return PasswordResetCode.builder()
                .id(entity.getId())
                .userId(new PhoneNumber(entity.getUserId()))
                .codeHash(entity.getCodeHash())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static PasswordResetCodeEntity toPasswordResetCodeEntity(PasswordResetCode code) {
        PasswordResetCodeEntity entity = new PasswordResetCodeEntity();
        if (code.getId() != null) {
            entity.setId(code.getId());
        }
        entity.setUserId(code.getUserId().number());
        entity.setCodeHash(code.getCodeHash());
        entity.setExpiresAt(code.getExpiresAt());
        entity.setCreatedAt(code.getCreatedAt());
        return entity;
    }
}
