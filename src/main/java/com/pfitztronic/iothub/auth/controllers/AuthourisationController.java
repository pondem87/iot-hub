package com.pfitztronic.iothub.auth.controllers;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthourisationController {

    @PostMapping("/login")
    public String login() {
        return "User logged in!";
    }

    @PostMapping("/logout")
    public String logout() {
        return "User logged out!";
    }

}
