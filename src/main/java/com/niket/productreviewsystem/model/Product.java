package com.niket.productreviewsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Lob // For storing large text blocks
    private String description;

    // Stores the file path to the image on the local file system
    private String imagePath;

    // Define the one-to-many relationship with Reviews
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews; // Not mapped to a column, used for ORM relationships
}