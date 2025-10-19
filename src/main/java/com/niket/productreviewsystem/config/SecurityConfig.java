package com.niket.productreviewsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // 1. Password Encoder Bean (Required for security)
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt is the industry standard for secure password hashing
        return new BCryptPasswordEncoder();
    }

    // 2. Security Filter Chain Configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests
                        // === NEW PUBLIC ACCESS RULES ===
                        // 1. Allow unauthenticated users to access the registration page and form POST
                        .requestMatchers("/register").permitAll()

                        // 2. Allow unauthenticated users to view the login page
                        // (Note: it is also allowed by .formLogin().permitAll() but good to be explicit)
                        .requestMatchers("/login").permitAll()

                        // 3. Allow unauthenticated users to view public product listings (assuming this is your main public view)
                        .requestMatchers("/", "/products", "/product-detail/**").permitAll()

                        // 4. Permit public access to static resources (CSS, images, etc.)
                        .requestMatchers("/css/**", "/uploads/**", "/images/**").permitAll()

                        // === RESTRICTED ACCESS RULES ===
                        // Define access rules by role (Admin only)
                        .requestMatchers("/admin/**", "/analytics/**").hasRole("ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // Use the default Spring Security login page (http://localhost:8080/login)
                        .loginPage("/login")
                        .permitAll() // Ensure login form is accessible
                        .defaultSuccessUrl("/products", true) // Redirect to product list on success
                )
                .logout(logout -> logout
                        .permitAll()
                );

        return http.build();
    }
}
