package com.pfitztronic.iothub.core.accounts;

import com.pfitztronic.iothub.core.accounts.publishers.impl.*;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IAuditedEventPublisher;
import com.pfitztronic.iothub.core.accounts.publishers.interfaces.IUserNotificationPublisher;
import com.pfitztronic.iothub.core.accounts.repositories.impl.AccountRepository;
import com.pfitztronic.iothub.core.accounts.repositories.impl.UserRepository;
import com.pfitztronic.iothub.core.accounts.repositories.impl.VerificationCodeRepository;
import com.pfitztronic.iothub.core.accounts.repositories.interfaces.IAccountRepository;
import com.pfitztronic.iothub.core.accounts.repositories.interfaces.IUserRepository;
import com.pfitztronic.iothub.core.accounts.repositories.interfaces.IVerificationCodeRepository;
import com.pfitztronic.iothub.core.accounts.services.impl.AccountManagementService;
import com.pfitztronic.iothub.core.accounts.services.impl.UserDetailsServiceImpl;
import com.pfitztronic.iothub.core.accounts.services.impl.UserManagementService;
import com.pfitztronic.iothub.core.accounts.services.interfaces.IUserPermissionsService;
import com.pfitztronic.iothub.core.accounts.util.CodeGenerator;
import com.pfitztronic.iothub.core.accounts.util.PasswordEncoderProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AccountsModuleConfig {

    @Bean
    public IUserNotificationPublisher userNotificationPublisher() {
        return new UserNotificationPublisher();
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
    UserManagementService userManagementService(
            PasswordEncoder passwordEncoder,
            IUserRepository baseUserRepository,
            IVerificationCodeRepository baseVerificationCodeRepository
    ) {
        return new UserManagementService(
                new UserRepository(baseUserRepository),
                new VerificationCodeRepository(baseVerificationCodeRepository),
                new CodeGenerator(),
                new PasswordEncoderProxy(passwordEncoder),
                auditedEventPublisher(),
                userNotificationPublisher(),
                new UserCreatedEventPublisher()
        );
    }

    @Bean
    public AccountManagementService accountsManagementService(
            IAccountRepository accountRepository,
            UserManagementService userManagementService
    ) {
        return new AccountManagementService(
                new AccountRepository(accountRepository),
                userManagementService,
                userNotificationPublisher(),
                new AccountCreatedEventPublisher(),
                new AccountStatusChangedEventPublisher(),
                auditedEventPublisher(),
                new AccountDeletedEventPublisher()
        );
    }

    @Bean
    public UserDetailsService userDetailsService(
            UserManagementService userManagementService,
            IUserPermissionsService userPermissionsService
    ) {
        return new UserDetailsServiceImpl(
                userManagementService,
                userPermissionsService
        );
    }
}
