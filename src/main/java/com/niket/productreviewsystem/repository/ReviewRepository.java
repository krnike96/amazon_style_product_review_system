package com.niket.productreviewsystem.repository;

import com.niket.productreviewsystem.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    long countByProductId(Long productId);

    List<Review> findByProductIdOrderByHelpfulVotesDesc(Long productId);

    List<Review> findByProductIdOrderByRatingDesc(Long productId);

    List<Review> findByProductIdOrderByRatingAsc(Long productId);

    List<Review> findByProductIdOrderByReviewDateDesc(Long productId);

    List<Review> findByProductId(Long productId);

    // Method used by getPaginatedReviewsByProductId
    Page<Review> findByProductId(Long productId, Pageable pageable);

    // Method to check if a user has reviewed a product before submitting a new review
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}