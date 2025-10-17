package com.niket.productreviewsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SecurityController {

    // Handles the GET request for the custom login page, defined in SecurityConfig
    @GetMapping("/login")
    public String login() {
        // This tells Spring to look for the template file: src/main/resources/templates/login.html
        return "login";
    }
}