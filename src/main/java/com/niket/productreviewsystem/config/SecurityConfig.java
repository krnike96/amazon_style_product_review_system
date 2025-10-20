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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests
                        // Public access for registration, login, and product viewing
                        .requestMatchers("/", "/register", "/login", "/products", "/product-detail/**").permitAll()

                        // IMPORTANT: Permit public access to static resources including the custom product assets folder
                        .requestMatchers("/css/**", "/uploads/**", "/images/**", "/product-assets/**").permitAll()

                        // Admin-only paths
                        .requestMatchers("/admin/**", "/analytics/**").hasRole("ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/products", true)
                )
                .logout(logout -> logout
                        .permitAll()
                );

        return http.build();
    }
}