package com.niket.productreviewsystem.service;

import com.niket.productreviewsystem.model.Product;
import com.niket.productreviewsystem.model.Role;
import com.niket.productreviewsystem.model.User;
import com.niket.productreviewsystem.repository.ProductRepository;
import com.niket.productreviewsystem.repository.RoleRepository;
import com.niket.productreviewsystem.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataLoader {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Executes after the application context is initialized
    @PostConstruct
    public void init() {
        // 1. Create Roles if they don't exist
        if (roleRepository.findByName(Role.RoleName.ROLE_ADMIN).isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName(Role.RoleName.ROLE_ADMIN);
            roleRepository.save(adminRole);
        }

        if (roleRepository.findByName(Role.RoleName.ROLE_USER).isEmpty()) {
            Role userRole = new Role();
            userRole.setName(Role.RoleName.ROLE_USER);
            roleRepository.save(userRole);
        }

        // Retrieve the roles immediately after saving (or if they existed)
        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN).get();
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER).get();

        // 2. Create Admin User if they don't exist
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            // Hashing the password is crucial for security
            admin.setPassword(passwordEncoder.encode("adminpass"));
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            admin.setRoles(adminRoles);
            userRepository.save(admin);
        }

        // 3. Create Regular User if they don't exist
        if (userRepository.findByUsername("testuser").isEmpty()) {
            User user = new User();
            user.setUsername("testuser");
            user.setEmail("user@example.com");
            user.setPassword(passwordEncoder.encode("userpass"));
            Set<Role> userRoles = new HashSet<>();
            userRoles.add(userRole);
            user.setRoles(userRoles);
            userRepository.save(user);
        }

        // 4. Create Sample Product if none exist
        if (productRepository.count() == 0) {
            Product product = new Product();
            product.setName("Echo Dot (5th Gen)");
            product.setDescription("Our best-sounding Echo Dot yet, with deeper bass, clearer vocals, and vibrant sound in any room.");
            // imagePath is currently null, we'll handle actual image saving later
            productRepository.save(product);
        }
    }
}