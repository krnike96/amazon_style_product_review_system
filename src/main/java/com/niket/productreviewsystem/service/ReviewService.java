package com.niket.productreviewsystem.service;

import com.niket.productreviewsystem.model.Review;
import com.niket.productreviewsystem.model.ReviewFormDTO;
import com.niket.productreviewsystem.model.User;
import com.niket.productreviewsystem.repository.ProductRepository;
import com.niket.productreviewsystem.repository.ReviewRepository;
import com.niket.productreviewsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // Define the base path for local file storage (inside static/uploads)
    private final Path uploadDir = Paths.get("src/main/resources/static/uploads");

    public ReviewService() {
        try {
            // Ensure the upload directory exists upon service initialization
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload directory", e);
        }
    }

    public void saveReview(ReviewFormDTO dto) {
        // 1. Get the current authenticated user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // 2. Get the product
        var product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setReviewDate(LocalDateTime.now());
        // For simplicity, we assume all reviews posted are "verified" in this initial build
        review.setVerifiedPurchase(true);

        // 3. Handle file upload (local storage)
        MultipartFile imageFile = dto.getImageFile();
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Generate unique file name
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

                // Save the file locally
                Path filePath = this.uploadDir.resolve(uniqueFileName);
                Files.copy(imageFile.getInputStream(), filePath);

                // Store the relative path in the database
                review.setImagePath("/uploads/" + uniqueFileName);
            } catch (IOException e) {
                // Good error handling: if file upload fails, log it and throw a user-friendly error
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }

        // 4. Save the review entity
        reviewRepository.save(review);
    }
}