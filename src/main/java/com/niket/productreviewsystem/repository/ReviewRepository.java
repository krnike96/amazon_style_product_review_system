package com.niket.productreviewsystem.repository;

import com.niket.productreviewsystem.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    long countByProductId(Long productId);
}