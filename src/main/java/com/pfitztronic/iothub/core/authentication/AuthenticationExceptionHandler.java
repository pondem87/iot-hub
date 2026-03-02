package com.pfitztronic.iothub.core.authentication;

import com.pfitztronic.iothub.core.authentication.exceptions.UserAgentCredentialsInvalidException;
import com.pfitztronic.iothub.core.authentication.exceptions.UserAgentSessionExpiredException;
import com.pfitztronic.iothub.core.authentication.exceptions.UserAgentSessionNotFoundException;
import com.pfitztronic.iothub.core.authentication.exceptions.UserAgentSessionRevokedException;
import com.pfitztronic.iothub.config.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class AuthenticationExceptionHandler {

    @ExceptionHandler({
            UserAgentSessionNotFoundException.class,
            UserAgentSessionExpiredException.class,
            UserAgentSessionRevokedException.class,
            UserAgentCredentialsInvalidException.class,
    })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleSessionExceptions(RuntimeException ex) {
        Map<String, String> errors = new HashMap<>();

        errors.put("message", ex.getMessage());

        return new ApiErrorResponse("AUTHENTICATION_ERROR", errors);
    }

}
