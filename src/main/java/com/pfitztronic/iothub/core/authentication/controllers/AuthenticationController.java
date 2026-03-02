package com.pfitztronic.iothub.core.authentication.controllers;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @PostMapping("/login")
    public String login() {
        return "User logged in!";
    }

    @PostMapping("/logout")
    public String logout() {
        return "User logged out!";
    }

}
