package com.niket.productreviewsystem.service;

import com.niket.productreviewsystem.model.Review;
import com.niket.productreviewsystem.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private ReviewRepository reviewRepository;

    /**
     * Gets all reviews from the system (for moderation purposes).
     * In a real system, this would filter for 'PENDING' status.
     * For now, we fetch all reviews.
     */
    public List<Review> getAllReviews() {
        // Find all reviews and sort them by ID (oldest first) for consistent viewing
        return reviewRepository.findAll();
    }

    /**
     * Moderates a review by either approving (no action needed in our current model)
     * or rejecting (deleting) it.
     */
    @Transactional // Ensure atomicity of the operation
    public void moderateReview(Long reviewId, boolean isApproved) {
        Optional<Review> optionalReview = reviewRepository.findById(reviewId);

        if (optionalReview.isEmpty()) {
            throw new RuntimeException("Review not found with ID: " + reviewId);
        }

        if (isApproved) {
            // Future step: If we had a 'status' field (PENDING/APPROVED), we would update it here.
            // For now, approval is a NO-OP since all submitted reviews are immediately displayed.
            // We just ensure the review exists.
        } else {
            // Reject: Delete the review permanently
            reviewRepository.delete(optionalReview.get());
        }
    }
}