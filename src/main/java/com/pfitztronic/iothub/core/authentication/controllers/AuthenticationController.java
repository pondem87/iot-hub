package com.pfitztronic.iothub.core.authentication.controllers;
import com.pfitztronic.iothub.core.authentication.dto.LoginInputData;
import com.pfitztronic.iothub.core.authentication.dto.LoginResponse;
import com.pfitztronic.iothub.core.authentication.services.impl.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @RequestHeader("User-Agent") String userAgent,
            @Valid @RequestBody LoginInputData credentials
            ) {
        return this.authenticationService.login(userAgent, credentials);
    }

    @PostMapping("/logout")
    public String logout(
            @RequestHeader("User-Agent") String userAgent
    ) {
        return this.authenticationService.logout(userAgent);
    }

    @PostMapping("/logout-all")
    public String logoutAllSessions() {
        return this.authenticationService.logoutAllSessions();
    }

}
