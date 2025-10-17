package com.niket.productreviewsystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "roles")
@Data // Lombok: Generates getters, setters, toString, equals, and hashCode
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoleName name;

    // We can use an Enum for cleaner role management
    public enum RoleName {
        ROLE_USER,
        ROLE_ADMIN
    }
}