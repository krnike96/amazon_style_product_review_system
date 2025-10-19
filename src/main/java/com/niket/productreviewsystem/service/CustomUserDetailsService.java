package com.niket.productreviewsystem.service;

import com.niket.productreviewsystem.model.Role;
import com.niket.productreviewsystem.model.User;
import com.niket.productreviewsystem.repository.RoleRepository; // NEW IMPORT
import com.niket.productreviewsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder; // NEW IMPORT
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections; // NEW IMPORT
import java.util.Optional; // NEW IMPORT
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    // NEW: Dependency needed for assigning the default role
    @Autowired
    private RoleRepository roleRepository;

    // NEW: Dependency needed for hashing the new user's password
    @Autowired
    private PasswordEncoder passwordEncoder;

    // This method is called by Spring Security to retrieve user details by username
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Convert our custom roles to Spring Security GrantedAuthorities
        Collection<? extends GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        // Return a Spring Security User object
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // The hashed password from the DB
                authorities
        );
    }

    // ==========================================================
    // NEW: Registration Logic
    // ==========================================================

    /**
     * Utility to check if a user exists by username. Used by the controller.
     */
    public Optional<User> findByUsername(String username) {
        // We use the UserRepository's method directly
        return userRepository.findByUsername(username);
    }

    /**
     * Saves a new user after hashing the password and assigning the default 'ROLE_USER'.
     */
    public void saveNewUser(User user) {
        // 1. Hash the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 2. Assign the default 'ROLE_USER' role
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_USER not found. Database must contain initial roles."));

        user.setRoles(Collections.singleton(userRole));

        // 3. Set other defaults (if any) and save the user
        userRepository.save(user);
    }
}