package com.niket.productreviewsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email; // NEW IMPORT
import jakarta.validation.constraints.NotBlank; // NEW IMPORT
import jakarta.validation.constraints.Size; // NEW IMPORT
import lombok.Data;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required") // ADDED VALIDATION
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") // ADDED VALIDATION
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Email is required") // ADDED VALIDATION
    @Email(message = "Email should be valid") // ADDED VALIDATION
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required") // ADDED VALIDATION
    @Size(min = 6, message = "Password must be at least 6 characters") // ADDED VALIDATION
    @Column(nullable = false)
    private String password; // Will be stored as a BCrypt hash

    // Define the relationship between User and Role
    @ManyToMany(fetch = FetchType.EAGER) // Fetch roles immediately
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;
}