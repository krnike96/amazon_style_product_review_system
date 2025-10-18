package com.niket.productreviewsystem.service;

import com.niket.productreviewsystem.model.Product;
import com.niket.productreviewsystem.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductAdminService {

    @Autowired
    private ProductRepository productRepository;

    // Define the base path for product image storage
    private final Path productUploadDir = Paths.get("./product-assets");

    public ProductAdminService() {
        try {
            // Ensure the product image directory exists
            Files.createDirectories(productUploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize product image upload directory", e);
        }
    }

    // --- CRUD Operations ---

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public Product saveProduct(Product product, MultipartFile imageFile) {
        // Handle image upload logic
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Generate unique file name
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

                // Delete old image if it exists and we are updating
                if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                    Path oldFilePath = Paths.get("src/main/resources/static" + product.getImagePath());
                    Files.deleteIfExists(oldFilePath);
                }

                // Save the new file locally
                Path filePath = this.productUploadDir.resolve(uniqueFileName);
                Files.copy(imageFile.getInputStream(), filePath);

                // Store the relative path for Thymeleaf
                product.setImagePath("/" + uniqueFileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload product image: " + e.getMessage());
            }
        }

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Clean up image file before deleting the entity
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            try {
                Path filePath = productUploadDir.resolve(product.getImagePath().substring(1));
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log but do not prevent deletion of the entity if file deletion fails
                System.err.println("Warning: Could not delete product image file: " + e.getMessage());
            }
        }

        productRepository.deleteById(id);
    }
}