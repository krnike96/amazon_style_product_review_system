package com.niket.productreviewsystem.service;

import com.niket.productreviewsystem.model.*;
import com.niket.productreviewsystem.repository.ProductRepository;
import com.niket.productreviewsystem.repository.ReviewRepository;
import com.niket.productreviewsystem.repository.ReviewVoteRepository;
import com.niket.productreviewsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // NEW IMPORT
import org.springframework.data.domain.PageRequest; // NEW IMPORT
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    // Dependencies injected via @Autowired fields
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewVoteRepository voteRepository;

    // The root path where files are physically saved, initialized via constructor
    private final Path uploadRootPath;

    /**
     * Constructor for ReviewService, used to initialize the upload directory path.
     * The path string is injected using @Value from application.properties.
     */
    public ReviewService(@Value("${review.upload.dir}") String uploadDirStr) {
        this.uploadRootPath = Paths.get(uploadDirStr).normalize();

        try {
            Files.createDirectories(this.uploadRootPath);
            logger.info("Upload directory initialized successfully at: {}", this.uploadRootPath.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Could not initialize upload directory: {}", this.uploadRootPath.toAbsolutePath(), e);
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

        // Check if the user has already reviewed this product
        if (reviewRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new RuntimeException("You have already submitted a review for this product.");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setReviewDate(LocalDateTime.now());
        review.setVerifiedPurchase(true);

        // 3. Handle file upload (local storage)
        MultipartFile imageFile = dto.getImageFile();
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Generate unique file name
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }

                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

                // Save the file locally to the path resolved against the CWD
                Path filePath = this.uploadRootPath.resolve(uniqueFileName);
                Files.copy(imageFile.getInputStream(), filePath);
                logger.info("Saved file successfully to: {}", filePath.toAbsolutePath());

                // Store the public URL path in the database.
                review.setImagePath("/uploads/" + uniqueFileName);
            } catch (IOException e) {
                logger.error("Failed to upload image for review.", e);
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }

        // 4. Save the review entity
        reviewRepository.save(review);
    }

    // --- NEW METHOD FOR PAGINATION (Replaces getReviewsByProductId) ---
    /**
     * Retrieves paginated and sorted reviews for a given product ID.
     */
    public Page<Review> getPaginatedReviewsByProductId(Long productId, String sortOrder, Pageable pageable) {

        Sort customSort;
        // 1. Determine the Sort object based on the custom 'sortOrder' string
        switch (sortOrder.toLowerCase()) {
            case "highest":
                customSort = Sort.by("rating").descending();
                break;
            case "lowest":
                customSort = Sort.by("rating").ascending();
                break;
            case "helpful":
                customSort = Sort.by("helpfulVotes").descending();
                break;
            case "newest":
            default:
                // Default to newest first
                customSort = Sort.by("reviewDate").descending();
                break;
        }

        // 2. Create a new Pageable instance that includes the custom sorting.
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                customSort
        );

        // 3. Call the repository method (requires you to define a standard JpaRepository method:
        // Page<Review> findByProductId(Long productId, Pageable pageable); in ReviewRepository)
        return reviewRepository.findByProductId(productId, sortedPageable);
    }

    // NOTE: The old 'getReviewsByProductId' method is removed as it's no longer used
    // and its logic is now inside 'getPaginatedReviewsByProductId'.


    /**
     * Calculates the average rating for a product.
     */
    public double getAverageRating(Long productId) {
        // Use the simple fetch method to get all reviews for calculation (no pagination needed here)
        List<Review> reviews = reviewRepository.findByProductId(productId);

        if (reviews.isEmpty()) {
            return 0.0;
        }

        double sum = reviews.stream().mapToInt(Review::getRating).sum();
        // Return rounded average to one decimal place
        return Math.round((sum / reviews.size()) * 10.0) / 10.0;
    }

    /**
     * Calculates the count of reviews for a product.
     */
    public long getReviewCount(Long productId) {
        return reviewRepository.countByProductId(productId);
    }

    // --- REPLACING addHelpfulVote with the new, robust addVote method ---
    @Transactional
    public void addVote(Long reviewId, String type) {
        // 1. Get current authenticated user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found or not logged in."));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found for voting."));

        Optional<ReviewVote> existingVote = voteRepository.findByUserIdAndReviewId(user.getId(), reviewId);

        if (type.equalsIgnoreCase("UP")) {
            if (existingVote.isPresent()) {
                throw new RuntimeException("You have already marked this review as helpful.");
            }
            // 2. ACTION: Record the vote and update the count
            ReviewVote vote = new ReviewVote();
            vote.setUser(user);
            vote.setReview(review);
            voteRepository.save(vote);

            // Atomically increment the vote count on the Review entity
            review.setHelpfulVotes(review.getHelpfulVotes() + 1);
            reviewRepository.save(review);
        } else if (type.equalsIgnoreCase("DOWN")) {
            if (existingVote.isEmpty()) {
                throw new RuntimeException("You have not marked this review as helpful to un-vote.");
            }
            // 2. ACTION: Remove the vote and decrement the count
            voteRepository.delete(existingVote.get());

            // Atomically decrement the vote count on the Review entity
            if (review.getHelpfulVotes() > 0) {
                review.setHelpfulVotes(review.getHelpfulVotes() - 1);
                reviewRepository.save(review);
            }
        } else {
            throw new IllegalArgumentException("Invalid vote type provided: " + type);
        }
    }
}