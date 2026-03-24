package com.pfitztronic.iothub.core.accounts;

import com.pfitztronic.iothub.core.accounts.publishers.impl.*;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.*;
import com.pfitztronic.iothub.core.accounts.repositories.impl.AccountRepository;
import com.pfitztronic.iothub.core.accounts.repositories.impl.AccountUserRepository;
import com.pfitztronic.iothub.core.accounts.repositories.impl.PasswordResetCodeRepository;
import com.pfitztronic.iothub.core.accounts.repositories.impl.UserRepository;
import com.pfitztronic.iothub.core.accounts.repositories.impl.VerificationCodeRepository;
import com.pfitztronic.iothub.core.accounts.repositories.interfaces.IAccountRepository;
import com.pfitztronic.iothub.core.accounts.repositories.interfaces.IAccountUserRepository;
import com.pfitztronic.iothub.core.accounts.repositories.interfaces.IPasswordResetCodeRepository;
import com.pfitztronic.iothub.core.accounts.repositories.interfaces.IUserRepository;
import com.pfitztronic.iothub.core.accounts.repositories.interfaces.IVerificationCodeRepository;
import com.pfitztronic.iothub.core.accounts.services.AccountsConfigProperties;
import com.pfitztronic.iothub.core.accounts.services.impl.*;
import com.pfitztronic.iothub.core.accounts.services.interfaces.IUserPermissionsService;
import com.pfitztronic.iothub.core.accounts.util.CodeGenerator;
import com.pfitztronic.iothub.core.accounts.util.PasswordEncoderProxy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@EnableConfigurationProperties({AccountsConfigProperties.class})
@Configuration
public class AccountsModuleConfig {

    @Bean
    public IUserNotificationPublisher userNotificationPublisher(
            List<IUserNotificationHandler> handlers
    ) {
        return new UserNotificationPublisher(handlers);
    }

    @Bean
    public IAccountCreatedEventPublisher accountCreatedEventPublisher(
            List<IAccountCreatedEventHandler> handlers
    ) {
        return new AccountCreatedEventPublisher(handlers);
    }

    @Bean
    public IAccountDeletedEventPublisher accountDeletedEventPublisher(
            List<IAccountDeletedEventHandler> handlers
    ) {
        return new AccountDeletedEventPublisher(handlers);
    }

    @Bean
    public IAuditedEventPublisher auditedEventPublisher() {
        return new AuditedEventPublisher();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public VerificationCodeService verificationCodeService(
            IVerificationCodeRepository baseVerificationCodeRepository,
            PasswordEncoder passwordEncoder,
            IUserNotificationPublisher userNotificationPublisher,
            AccountsConfigProperties accountsConfigProps
    )
    {
        return new VerificationCodeService(
                new VerificationCodeRepository(baseVerificationCodeRepository),
                new PasswordEncoderProxy(passwordEncoder),
                new CodeGenerator(),
                userNotificationPublisher,
                accountsConfigProps
        );
    }

    @Bean
    public PasswordResetCodeService passwordResetCodeService(
            IPasswordResetCodeRepository basePasswordResetCodeRepository,
            PasswordEncoder passwordEncoder,
            IUserNotificationPublisher userNotificationPublisher,
            AccountsConfigProperties accountsConfigProps
    )
    {
        return new PasswordResetCodeService(
                new PasswordResetCodeRepository(basePasswordResetCodeRepository),
                new PasswordEncoderProxy(passwordEncoder),
                new CodeGenerator(),
                userNotificationPublisher,
                accountsConfigProps
        );
    }

    @Bean
    public UserManagementService userManagementService(
            PasswordEncoder passwordEncoder,
            IUserRepository baseUserRepository,
            VerificationCodeService verificationCodeService,
            PasswordResetCodeService passwordResetCodeService,
            IUserNotificationPublisher userNotificationPublisher
    ) {
        return new UserManagementService(
                new UserRepository(baseUserRepository),
                verificationCodeService,
                passwordResetCodeService,
                new PasswordEncoderProxy(passwordEncoder),
                auditedEventPublisher(),
                userNotificationPublisher,
                new UserCreatedEventPublisher()
        );
    }

    @Bean
    public AccountManagementService accountsManagementService(
            IAccountRepository accountRepository,
            UserManagementService userManagementService,
            IUserNotificationPublisher userNotificationPublisher,
            IAccountCreatedEventPublisher accountCreatedEventPublisher,
            IAccountDeletedEventPublisher accountDeletedEventPublisher,
            AccountsConfigProperties accountsConfigProps
    ) {
        return new AccountManagementService(
                new AccountRepository(accountRepository),
                userManagementService,
                userNotificationPublisher,
                accountCreatedEventPublisher,
                new AccountStatusChangedEventPublisher(),
                auditedEventPublisher(),
                accountDeletedEventPublisher,
                accountsConfigProps
        );
    }

    @Bean
    public AccountUserManagementService accountsUserManagementService(
            IAccountUserRepository accountUserRepository
    ) {
        return new AccountUserManagementService(
                new AccountUserRepository(accountUserRepository)
        );
    }

    @Bean
    public UserDetailsService userDetailsService(
            UserManagementService userManagementService,
            IUserPermissionsService userPermissionsService,
            AccountUserManagementService accountUserManagementService
    ) {
        return new UserDetailsServiceImpl(
                userManagementService,
                userPermissionsService,
                accountUserManagementService
        );
    }
}
