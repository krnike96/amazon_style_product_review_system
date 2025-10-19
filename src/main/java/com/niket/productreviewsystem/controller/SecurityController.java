package com.niket.productreviewsystem.controller;

import com.niket.productreviewsystem.model.User;
import com.niket.productreviewsystem.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

@Controller
public class SecurityController {

    @Autowired
    private CustomUserDetailsService userService;

    @GetMapping("/login")
    public String login(Model model) {
        // Attributes for the welcome message on the login page
        model.addAttribute("appName", "Product Review System");
        model.addAttribute("projectGoal", "Share authentic, helpful, and verified customer feedback.");
        return "login";
    }

    // ==========================================================
    // NEW: User Registration Endpoints
    // ==========================================================

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Pass a new User object to the form for binding
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") @Valid User user,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {

        // Check if the username already exists using the service method
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            // Use the correct field name 'username' for the error
            result.rejectValue("username", "duplicateUsername", "An account already exists with this username.");
        }

        // We REMOVE the manual password length check because the @Size annotation on the User model handles it.

        if (result.hasErrors()) {
            // If there are validation errors, return to the form
            return "register";
        }

        // Save the new user with the default 'ROLE_USER'
        userService.saveNewUser(user);

        redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in with your new account.");
        return "redirect:/login";
    }
}