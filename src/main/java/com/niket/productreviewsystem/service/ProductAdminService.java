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

    // Define the base path for product image storage (physical disk location: project-root/product-assets)
    private final Path productUploadDir = Paths.get("./product-assets").toAbsolutePath().normalize();
    // Constant for the URL prefix used in the browser and stored in the database
    private static final String URL_PREFIX = "/product-assets/";

    public ProductAdminService() {
        try {
            // Ensure the product image directory exists
            Files.createDirectories(productUploadDir);
            System.out.println("Product image upload directory initialized at: " + productUploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize product image upload directory", e);
        }
    }

    // --- Utility method to safely get filename from the stored URL path ---
    private String getFileNameFromUrlPath(String urlPath) {
        if (urlPath != null && urlPath.startsWith(URL_PREFIX)) {
            // Returns only the unique file name (e.g., 'a1b2c3d4.jpg')
            return urlPath.substring(URL_PREFIX.length());
        }
        return null;
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
                String fileExtension = "";
                if (originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

                // 1. Correctly delete old image if it exists and we are updating
                if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                    String oldFileName = getFileNameFromUrlPath(product.getImagePath());
                    if (oldFileName != null) {
                        Path oldFilePath = this.productUploadDir.resolve(oldFileName);
                        Files.deleteIfExists(oldFilePath);
                    }
                }

                // 2. Save the new file locally
                Path filePath = this.productUploadDir.resolve(uniqueFileName);
                Files.copy(imageFile.getInputStream(), filePath);

                // 3. Store the correct public URL path in the database.
                // It MUST start with /product-assets/ to match the new WebMvcConfig mapping.
                product.setImagePath(URL_PREFIX + uniqueFileName);

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
                // Strip the URL prefix to get the filename for physical deletion
                String fileName = getFileNameFromUrlPath(product.getImagePath());
                if (fileName != null) {
                    Path filePath = productUploadDir.resolve(fileName);
                    Files.deleteIfExists(filePath);
                }
            } catch (Exception e) {
                // Log but do not prevent deletion of the entity if file deletion fails
                System.err.println("Warning: Could not delete product image file: " + e.getMessage());
            }
        }

        productRepository.deleteById(id);
    }
}