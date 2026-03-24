package com.pfitztronic.iothub.core.accounts;

import com.pfitztronic.iothub.config.ApiErrorResponse;
import com.pfitztronic.iothub.core.accounts.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class AccountsExceptionHandler {
    private static final String PASSWORD_VALIDATION_ERROR_STRING = "PASSWORD_VALIDATION_ERROR";
    private static final String ACCOUNT_NAME_DUPLICATION_ERROR_STRING = "ACCOUNT_NAME_DUPLICATION_ERROR";
    private static final String ACCOUNT_POLICY_VIOLATION_ERROR_STRING = "ACCOUNT_POLICY_VIOLATION_ERROR";

    @ExceptionHandler(InvalidPasswordFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handlePasswordException(InvalidPasswordFormatException ex) {

        Map<String, String> errors = new HashMap<>();

        errors.put("password", ex.getMessage());

        return new ApiErrorResponse(PASSWORD_VALIDATION_ERROR_STRING, errors);
    }

    @ExceptionHandler(AccountNameAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleDuplicateAccountNameException(AccountNameAlreadyExistsException ex) {

        Map<String, String> errors = new HashMap<>();

        errors.put("account_name", ex.getMessage());

        return new ApiErrorResponse(ACCOUNT_NAME_DUPLICATION_ERROR_STRING, errors);
    }

    @ExceptionHandler({
            AccountCreationLimitExceededException.class,
            UserAlreadyExistsException.class,
            InvalidUserIdentityException.class,
            UserAccountNotFoundException.class,
            AccountNotActiveException.class,
            UserAccountNotVerifiedException.class,
            IllegalUserAccountStateException.class,
            InvalidOldPasswordException.class,
            PasswordResetCodeNotFoundException.class,
            PasswordResetCodeExpiredException.class,
            InvalidPasswordResetCodeException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleAccountsPolicyException(RuntimeException ex) {

        Map<String, String> errors = new HashMap<>();

        errors.put("message", ex.getMessage());

        return new ApiErrorResponse(ACCOUNT_POLICY_VIOLATION_ERROR_STRING, errors);
    }
}
